"""Local integration helper: RCON commands and read-only Anvil/NBT inspection.
Run only against the disposable development server in ./run.
"""
import io
import json
import os
import socket
import struct
import sys
import zlib
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]

def command(text):
    with socket.create_connection(('127.0.0.1', 25586), timeout=120) as sock:
        def exact(size):
            data = b''
            while len(data) < size:
                part = sock.recv(size - len(data))
                if not part:
                    raise EOFError('RCON disconnected')
                data += part
            return data
        def packet(kind, body):
            payload = struct.pack('<ii', 7, kind) + body.encode() + b'\0\0'
            sock.sendall(struct.pack('<i', len(payload)) + payload)
            reply = exact(struct.unpack('<i', exact(4))[0])
            if struct.unpack('<i', reply[:4])[0] == -1:
                raise RuntimeError('RCON authentication failed')
            return reply[8:-2].decode()
        packet(3, os.environ['UC_TEST_RCON_PASSWORD'])
        return packet(2, text)

def nbt(data):
    stream = io.BytesIO(data)
    def number(fmt):
        return struct.unpack('>' + fmt, stream.read(struct.calcsize(fmt)))[0]
    def string():
        return stream.read(number('H')).decode('utf8')
    def value(kind):
        if kind in range(1, 7):
            return number({1: 'b', 2: 'h', 3: 'i', 4: 'q', 5: 'f', 6: 'd'}[kind])
        if kind == 7:
            return stream.read(number('i'))
        if kind == 8:
            return string()
        if kind == 9:
            child, size = number('B'), number('i')
            return [value(child) for _ in range(size)]
        if kind == 10:
            result = {}
            while (child := number('B')):
                name = string()
                result[name] = value(child)
            return result
        if kind in (11, 12):
            return [number('i' if kind == 11 else 'q') for _ in range(number('i'))]
        raise ValueError(kind)
    kind = number('B')
    string()
    return value(kind)

def inspect(world='world'):
    results = []
    for region in (ROOT / 'run' / world).rglob('r.*.*.mca'):
        if region.parent.name != 'region':
            continue
        raw = region.read_bytes()
        for slot in range(1024):
            offset = int.from_bytes(raw[slot * 4:slot * 4 + 3], 'big') * 4096
            if not offset:
                continue
            size = int.from_bytes(raw[offset:offset + 4], 'big')
            if raw[offset + 4] != 2:
                continue
            chunk = nbt(zlib.decompress(raw[offset + 5:offset + 4 + size]))
            start = chunk.get('structures', {}).get('starts', {}).get('ubiquitous_chests:underground_chest')
            if not start or start.get('id') == 'INVALID':
                continue
            children = start['Children']
            assert len(children) == 1, children
            box = children[0]['BB']
            assert box[:3] == box[3:], box
            chest = next((e for e in chunk.get('block_entities', []) if [e['x'], e['y'], e['z']] == box[:3]), None)
            # STRUCTURE_STARTS chunks may not have reached block placement yet.
            if chunk.get('Status') == 'minecraft:full':
                assert chest and chest['id'] == 'minecraft:chest', (region, box, chest)
                assert chest.get('LootTable') == 'ubiquitous_chests:chests/underground_chest' or 'Items' in chest
            depth = None
            heights = chunk.get('Heightmaps', {}).get('OCEAN_FLOOR')
            if heights:
                # 1.20.1 Overworld has 384 vertical blocks: 9-bit padded height entries.
                index = (box[2] & 15) * 16 + (box[0] & 15)
                height = (heights[index // 7] >> ((index % 7) * 9)) & 511
                depth = height - 64 - 1 - box[1]
            results.append({'region': str(region.relative_to(ROOT)), 'pos': box[:3], 'status': chunk.get('Status'), 'depthBelowFinalOceanFloor': depth, 'chest': chest})
    return results

if __name__ == '__main__':
    if sys.argv[1] == 'inspect':
        results = inspect(sys.argv[2] if len(sys.argv) > 2 else 'world')
        print(json.dumps(results, indent=2, ensure_ascii=False))
        assert results, 'No generated chest structures found'
    else:
        for cmd in sys.argv[1:]:
            print(cmd, '=>', command(cmd))
