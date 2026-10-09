# 随处可见的宝箱 · Ubiquitous Chests

Minecraft **1.20.1** / **Forge 47.x** / **Java 17**。

[English](publishing/DESCRIPTION.en.md) · [下载](https://github.com/zz123456789/UbiquitousChests/releases) · [反馈问题](https://github.com/zz123456789/UbiquitousChests/issues)

原创项目内容采用 **MIT** 许可，允许在保留许可声明的前提下修改、分发和用于整合包。详见 [LICENSE](LICENSE) 和 [第三方许可说明](LICENSING.md)。

在世界地下随机生成只有 **一个原版战利品箱子** 的结构，不添加建筑、房间或额外方块。使用原版结构系统，可通过 `/locate` 查找；箱子在首次打开时抽取战利品。支持单人和专用服务器。

## 安装

1. 安装 Minecraft 1.20.1 对应的 Forge，建议使用 **47.4.10 或更新的 47.x**。
2. 把 `ubiquitous_chests-1.0.2.jar` 放入游戏或服务器的 `mods` 文件夹。联机时客户端和服务端安装相同版本，升级时删除旧版 JAR。
3. 首次启动会生成 `config/ubiquitous_chests-common.toml`。多人游戏以服务端配置为准。
4. 退出游戏或关闭服务器后修改配置，再重新启动。

世界需要开启“生成结构”。已有世界可以使用，但只在**尚未生成的新区块**出现新宝箱。

## 定位

```mcfunction
/locate structure ubiquitous_chests:underground_chest
```

需要作弊权限或 OP。返回的 X/Z 指向宝箱所在竖直列；原版命令的 Y 显示 `~`，需要向地下探索。注意挖掘安全。

测试战利品可以使用：

```mcfunction
/loot give @s loot ubiquitous_chests:chests/underground_chest
```

## 默认配置

| 配置 | 默认值 | 说明 |
| --- | --- | --- |
| `generation.enabled` | `true` | 是否自然生成 |
| `generation.chancePerChunk` | `0.02` | 每个 16×16 区块有 2% 概率尝试生成，最多一个箱子 |
| `generation.minDepth` | `8` | 地表下最小深度，包含边界 |
| `generation.maxDepth` | `48` | 地表下最大深度，包含边界 |
| `generation.blockedBiomes` | `[]` | 群系黑名单，支持群系 ID 和 `#标签` |
| `generation.blockedDimensions` | `[]` | 维度 ID 黑名单，默认允许所有维度 |
| `loot.minRolls` / `loot.maxRolls` | `3` / `7` | 每箱进行 3–7 次带权重抽取 |
| `loot.items` | 基础资源及铁装备 | 自由增加、删除、调整物品与数量 |

生成使用世界种子进行确定性概率判定。所有群系（包括模组群系）均可参与；同时检查地表群系和箱子位置的地下群系。维度要有支持结构生成的地形生成器。

深度相对于生成器提供的基础地形高度，海洋使用海床，下界使用顶部地形。树木等后续装饰不计入深度。只在深度范围内选择固体地形，避开基础地形中的空气、液体、基岩和世界底部五格；没有合适位置时放弃本区块，因此实际出现率可能低于配置概率。后续洞穴雕刻或其他模组的地形修改可能让宝箱暴露在洞穴中。

最大/最小深度或抽取次数填反时会自动交换。正常玩法中需先挖开箱子上方方块才能开箱。

### 示例：只允许主世界中的非海洋区域

```toml
[generation]
enabled = true
chancePerChunk = 0.05
minDepth = 10
maxDepth = 35
blockedBiomes = ["#minecraft:is_ocean", "minecraft:deep_dark"]
blockedDimensions = ["minecraft:the_nether", "minecraft:the_end"]
```

如果安装了额外维度模组，也可以将它的维度 ID 添加到黑名单。

### 战利品格式

```text
物品ID|权重|最小数量|最大数量
```

```toml
[loot]
minRolls = 3
maxRolls = 7
items = [
  "minecraft:iron_ingot|30|2|8",
  "minecraft:gold_ingot|18|1|5",
  "minecraft:ender_pearl|8|1|3",
  "minecraft:obsidian|12|2|6",
  "minecraft:iron_pickaxe|5|1|1"
]
```

每次抽取的概率为该物品权重除以所有有效条目的总权重，可多次抽中同一物品。权重范围 1–100000，每次数量范围 1–64；不可堆叠装备会按原版规则拆分。支持已安装模组的物品 ID，不支持在条目中填写 NBT。箱子容量为 27 格，过多战利品无法全部放入。

默认还包含煤炭、面包、铁剑和全套铁护甲。未知物品或非法条目会跳过并记录日志，不导致整个模组启动失败。空物品列表或 0 次抽取可生成空箱。

战利品配置在服务器加载战利品表时生效，尚未开过的宝箱使用当前战利品表；已经打开过的箱子保留原物品。生成设置不会删除已有结构，所以更改黑名单后 `/locate` 仍可能找到旧宝箱。调整生成概率后也建议前往新区块测试。

## 构建

在 Java 17 环境执行：

```powershell
.\gradlew.bat build
```

成品位于 `build/libs/ubiquitous_chests-1.0.2.jar`。项目包含 Gradle Wrapper；首次构建需要联网下载 Forge 和编译用的兼容 API。模组本体运行无需额外前置模组。

`tools/validate_server.py` 是本项目一次性开发测试服务器的 RCON / 存档检查工具，不需要安装到游戏中，也不属于模组运行依赖。

## JEI / JER 战利品页面（1.0.1 新增）

箱子战利品页面来自 **Just Enough Resources（JER）**，显示在 **Just Enough Items（JEI）** 界面里。客户端同时安装 Minecraft 1.20.1 Forge 对应的 JEI 15.x 和 JER 1.4.0.x 后，进入世界即可在 JER 的箱子战利品分类中找到“随处可见的宝箱”（英文为 Ubiquitous Chests）。也可以对战利品物品按 JEI 的查看配方键，切换到箱子战利品分类查找。

页面使用 JER 原生样式，展示物品、数量范围、每次抽取的权重概率以及抽取次数。概率是**每次抽取**的概率，不是打开整个箱子后至少获得一次的概率。

客户端显示服务器实际加载的战利品表，联机时不要求玩家手动复制服务器配置。进入服务器及服务器执行 `/reload` 后同步，切换服务器时清除旧数据。按本文要求重启服务器修改配置后，玩家重新连接即可看到更新后的内容。

JEI 和 JER 均为可选客户端模组，服务端不需要安装它们。只装 JEI 没有 JER 时，不会出现箱子战利品分类，但本模组仍正常运行。建议使用 JER **1.4.0.247**；兼容测试使用 JEI **15.20.0.106**。

