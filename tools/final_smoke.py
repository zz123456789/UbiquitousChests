import sys, re, json
sys.path.insert(0, 'tools')
from validate_server import command, inspect
response = command('locate structure ubiquitous_chests:underground_chest')
print(response)
m = re.search(r'\[(-?\d+), ~, (-?\d+)\]', response)
assert m, response
x,z = map(int,m.groups())
print(command('save-all flush'))
entries = inspect('world-final')
found = [e for e in entries if e['pos'][0] == x and e['pos'][2] == z]
assert found and found[0]['chest'], (x,z,found)
print('PASS: locate X/Z matches physical chest:', found[0]['pos'])
print('PASS: generated structures:',len(entries))
y=found[0]['pos'][1]
print(command(f'item replace block {x} {y} {z} container.26 with minecraft:stone 1'))
loot=command(f'data get block {x} {y} {z} Items')
print(loot)
assert any(item in loot for item in ['iron_', 'gold_ingot', 'ender_pearl', 'obsidian', 'coal', 'bread'])
for dimension in ['the_nether', 'the_end']:
    result=command(f'execute in minecraft:{dimension} run locate structure ubiquitous_chests:underground_chest')
    print(result)
    assert 'nearest' in result, result
print(command('stop'))
