# Magnatour

| <span style="font-size: 16px;">[简体中文](#简体中文)</span> | <span style="font-size: 16px;">[English](#English)</span> | <span style="font-size: 16px;">[日本語](#日本語)</span> | <span style="font-size: 16px;">[Español](#Español)</span> |
|---|---|---|---|

[![License: GPL v3](https://img.shields.io/badge/License-GPL%20v3-red.svg)](LICENSE)
[![Releases](https://img.shields.io/github/release/roeyqian/Magnatour?style=flat&color=6366f1)](https://github.com/roeyqian/Magnatour/releases)
[![Stars](https://img.shields.io/github/stars/roeyqian/Magnatour?style=flat&color=ffcc66)](https://github.com/roeyqian/Magnatour/stargazers)

---

# 简体中文

| <span style="font-size: 16px;">[内容总览](#内容总览)</span> | <span style="font-size: 16px;">[核心功能方块](#核心功能方块)</span> | <span style="font-size: 16px;">[关键装备与道具](#关键装备与道具)</span> | <span style="font-size: 16px;">[资源与推进](#资源与推进)</span> | <span style="font-size: 16px;">[其他注意事项](#其他注意事项)</span> | <span style="font-size: 16px;">[许可证](#许可证)</span> |
|:---:|:---:|:---:|:---:|:---:|:---:|

**伟大旅行** `Magnatour` 是一个面向 Minecraft Fabric API 的冒险向内容模组，其核心在于极致的超模数值与离谱机制，围绕两条明显分层的内容线展开

- **超凡** 线提供基础资源扩展、功能方块、维度入口与高阶装备。
- **寰宇** 线提供最顶级的超模装备、远程访问、跨维传送、扩展存储与最终配方。

> 注意：本项目当前为个人开发，如果你觉得此模组有浓厚兴趣并想参与开发的话，请前往 https://github.com/roeyqian/MagnatourDev

## 内容总览

### 1. 创造标签页

模组会注册四个独立标签页：

- `超凡方块类`
- `超凡物品类`
- `寰宇方块类`
- `寰宇物品类`

这四个标签页对应模组当前的主要内容分类。

### 2. 维度与世界生成

#### 维度介绍

| 名称 | 维度 id | 当前实现 |
|---|---|---|
| **丰收大陆** | `harvest_continent` | 使用自定义 `noise` 生成器与自定义 `BiomeSource`，是一个农业/地貌主题维度 |
| **矿石大陆** | `ore_continent` | 使用 `flat` 生成器，地层由 `bedrock + netherrack + deepslate + stone` 组成，并配置大量矿物特征 |

#### 丰收大陆 当前群系

| 名称 | 群系 id | 群系特点 |
|---|---|---|
| **小麦平原** | `wheat_plain` | 大平原地形，会铺设小麦与树木特征。|
| **西瓜丛林** | `melon_jungle` | 地形正常起伏，会生成西瓜与树林特征。|
| **南瓜峡谷** | `pumpkin_gorge` | 极度崎岖，会强化南瓜特征。|
| **大湖** | `big_lake` | 大片水域，深处直达虚空。|
| **湖心岛** | `lake_center_island` | 圆盘状小岛，会生成大量花卉，并且是 **黄金钟楼** 的唯一结构群系。 |

#### 矿石大陆 当前特征

- 整个维度只使用一个自定义群系：`ore_continent`
- 会集中刷出煤、铁、铜、金、青金石、红石、绿宝石、钻石，以及深层变种
- 还包含下界金矿、下界石英与远古残骸
- **钻石城** 结构会在该维度生成

### 3. 传送门

当前实现了两种自定义维度门，行为上接近原版下界传送门：

| 传送门 | 框架方块 | 目标维度 |
|---|---|---|
| `harvest_continent_portal` | `supreme_fodder_block` | `harvest_continent` |
| `ore_continent_portal` | `supreme_gem_block` | `ore_continent` |

要点：

- 传送门支持类似原版门框的矩形检测逻辑。
- 当前最小有效门框为“宽 `3`、高 `4`”。
- 可在主世界与对应维度之间双向往返。
- 进入门内需要累计约 `80 tick` 触发传送。
- 创造模式玩家会直接触发传送。
- 目标维度若附近没有现成门，代码会自动落地生成一座门框。

### 4. 结构

| 名称 | 结构 id | 所在位置 | 说明 |
|---|---|---|---|
| **黄金钟楼** | `gold_bell_tower` | 湖心岛 | 丰收大陆 的专属结构 |
| **钻石城** | `diamond_city` | 矿石大陆 | 矿石大陆 的主结构 |

### 5. 生物与召唤

#### 当前注册的主要生物实体

- **怪物类**

  - | `bell_ringer` | 守钟人 | 50攻，500血，20防，战士定位 |
    |---|---|---|

  - |`bell_soul` | 钟灵 | 20攻，50血，0防，蚊子定位 |
    |---|---|---|

  - | `obsidian_golem` | 黑曜石傀儡 | 50攻，2000血，50防，泰坦定位 |
    |---|---|---|

- **未知类**

  - | `the_unnameable_thing` | 不可名状之物 | 0攻，100000血，0防，坚果定位 |
    |---|---|---|

- **头目类**

  - | `sculk_behemoth` | 幽匿巨兽 | 100攻，20000血，100防，头目定位 |
    |---|---|---|

  - | `pale_lord` | 苍白领主 | 1~2147483647攻，20血，10防，头目定位 |
    |---|---|---|

    > `pale_lord_clone` **苍白领主分身** 无法击杀，攻击同上

- **中立类**

  - 暂无

- **友好类**

  - | `netherite_golem` | 下界合金傀儡 | 100攻，8000血，100防，超级泰坦定位 |
    |---|---|---|

  - | `universe_guardian` | 寰宇守卫 | 2147483647攻，2147483647血，2147483647防，无敌定位 |
    |---|---|---|

#### 当前已实现的多方块召唤

`SummonStructureHelper` 已实现至少以下召唤结构：

- `netherite_golem`
  - 头：`supreme_pumpkin_head`
  - 身体：`netherite_block`
  - 形状类似铁傀儡
- `obsidian_golem`
  - 头：`supreme_pumpkin_head`
  - 身体：`crying_obsidian`
  - 形状类似铁傀儡
- `pale_lord`
  - 核心：`creaking_heart`
  - 外层：`supreme_fodder_block`
  - 形状为十字包裹中心
- `sculk_behemoth`
  - 由 `supreme_gem_block` + `sculk` + `sculk_catalyst` 的更大体积结构触发

召唤结构实现位于 [src/main/java/roeyqian/magnatour/block/SummonStructureHelper.java](./src/main/java/roeyqian/magnatour/block/SummonStructureHelper.java)。

## 核心功能方块

### 1. 超凡系功能方块

| 名称 | 方块 id | 当前实现 |
|---|---|---|
| **超凡工作台** | `supreme_worktable` | 自定义工作台，承载 `supreme_crafting` 配方 |
| **超凡熔炉** | `supreme_furnace` | 自定义熔炉，支持 `supreme_cooking`，燃料效率放大 `8x`，烧炼时间极大缩短 |
| **超凡逆向仪** | `supreme_reserver` | 反向配方查看与拆解台，会根据输入结果物推回原配方材料 |
| **超凡箱子** | `supreme_chest` | 自定义大箱子，带组合逻辑与特殊渲染 |
| **红石触发器** | `redstone_trigger` | 可配置频率、开关与模式（持续 / 脉冲）的红石触发器 |
| **物品枢纽** | `item_hub` | 类似漏斗，但具备高速吸入、过滤与定向输出逻辑，同时支持过滤物品 ID |
| **物流光纤** | `logistics_fiber` | 六向连接物流网络，可自动接入容器 |

### 2. 寰宇系功能方块

| 名称 | 方块 id | 当前实现 |
|---|---|---|
| **寰宇工作站** | `universe_workstation` | 自定义工作台，承载 `universe_crafting` 配方 |
| **寰宇烧炼厂** | `universe_refinery` | 自定义熔炉，支持原版 + `supreme_cooking` + `universe_cooking`，燃料效率 `32x`，每 tick 最多处理 `2` 个物品 |
| **寰宇保藏库** | `universe_library` | `252` 格存储的潜影盒类容器 |
| **寰宇虚空池** | `universe_void_pool` | 终极物品复制池，无条件拷贝一切物品 |
| **寰宇传送点** | `universe_teleport_point` | 可维护跨维度坐标列表的传送点 |
| **寰宇方块** | `universe_block` | 可切换虚拟光照状态的特殊方块 |

## 关键装备与道具

### 1. 超凡系

| 物品 | 当前实现 |
| --- | --- |
| `Supreme` 全套工具 / 护甲 | 不可损坏、带强制炫光覆盖 |
| `Supreme Mobile` | 两种模式切换：扫描功能方块 / 远程模拟无方块实体的功能界面 |
| `Strange Potion` 系列 | 会对目标施加随机效果池中的若干效果 |
| `Chunk TNT` | 爆炸后不是普通 TNT，而是直接清空整个区块并对区块内实体造成 `10000` 伤害 |

#### Supreme Mobile

`Supreme Mobile` 的当前逻辑是：

- `模式 0`：远程打开先前扫描过的功能方块 GUI
- `模式 1`：对着功能方块右键扫描并记录其方块 ID

限制也写得很明确：

- 只能模拟有菜单但**没有方块实体存储数据**的方块
- 对普通无 GUI 方块无效

### 2. 寰宇系

`Universe` 线核心装备默认具有以下属性：

- `EPIC` 稀有度
- 不可损坏
- 带炫光
- 带额外伤害抗性标签

| 物品 | 当前实现 |
| --- | --- |
| `Universe Stick` | 伤害与玩家经验等级挂钩，并按经验等级重复触发目标掉落表 |
| `Universe Ultima Sword` | 模式切换：点火 / 绝对处决与火球投射 |
| `Universe Omni Blade` | 模式切换：范围整地 / 最多 `512` 方块的连锁破坏 |
| `Universe Console` | 绑定功能方块并跨区块、跨维度远程打开 |
| `Universe Bucket` | 可饮用清除状态，也可在水 / 岩浆模式间切换为无限流体桶 |
| `Universe Star` | 使用即回血，并且也是超长燃料 |
| `Universe Guardian Spawn Egg` | 可召唤并驯服 `Universe Guardian` |

#### Universe Console

当前行为如下：

- `模式 1` 下可绑定任何有菜单的方块
- 绑定信息会写入物品组件，包含 `坐标 + 维度 + 显示名`
- `模式 0` 下可在界面里远程打开目标方块
- 打开前会强制加载目标区块
- 支持跨维度访问
- 支持删除绑定

远程访问生命周期由 `RemoteAccessManager` 管理。

#### Universe Ultima Sword

当前分成两个模式：

- `模式 0`
  - 右键执行点火逻辑
  - 攻击会对目标附火，并以极高伤害处理目标
- `模式 1`
  - 右键发射 `UniverseFireball`
  - 攻击会触发闪电与强制处决逻辑
  - `Universe Guardian` 也只有在这个模式下才允许被真正伤害

#### Universe Omni Blade

当前分成两个模式：

- `模式 0`
  - 兼具锄地与铲平地表逻辑
  - 支持把 `Ever-Water` 系地表转成专属耕地
- `模式 1`
  - 以起始方块为核心做 BFS
  - 最多连锁破坏 `512` 个同类方块

#### Universe Bucket

当前行为：

- 饮用时按牛奶桶逻辑清除全部状态
- 通过快捷键切换：
  - `模式 0`：无限放水
  - `模式 1`：无限放岩浆
- 还能通过专门的网络包从世界中吸取水源 / 岩浆源并同步切换模式

### 3. Universe 护甲

| 护甲 | 当前实现 |
| --- | --- |
| `Universe Helmet` | 锁定饥饿值与空气值，免疫 `Hunger / Nausea / Darkness / Blindness`，并改善水下与岩浆内视野 |
| `Universe Chestplate` | 消除着火与冻结，清除中毒，并提供飞行能力 |
| `Universe Leggings` | 冲刺加速、提高跨步高度和跳跃强度，飞行时还能继续提速 |
| `Universe Boots` | 可在水 / 岩浆表面行走；潜行状态下通过“按住潜行并双击前进”触发短距离闪现冲刺 |

## 资源与推进

### 1. 当前资源线

根据当前配方与进度资源，推进大体可分成三段：

1. `Supreme` 基础资源与工具
2. 进入 `Harvest Continent / Ore Continent`，获取更进一步的核心材料
3. 通过 `Universe` 宝石、`Universe Light / Dark`、`Universe Primary Block` 与 `Universe Star` 进入终局

这部分是依据当前 `recipe` 与 `advancement` 资源整理出的内容顺序说明。

### 2. 一些关键配方节点

当前非常关键的几个配方 / 熔炼节点：

- `Fruit of All Things -> Harvest Core`（`supreme_cooking`）
- `Supreme Metal -> Ore Core`（`supreme_cooking`）
- `Rainbow Thing -> Supreme Core`（超长时长的 `supreme_cooking`）
- `Universe Gem Red + Yellow + White -> Universe Light`
- `Universe Gem Green + Blue + Black -> Universe Dark`
- `Universe Light + Universe Dark -> Universe Primary Block`
- `Bedrock -> Universe Primary Fragment`（`universe_cooking`）
- `Universe Primary Block -> Universe Star`（`universe_cooking`）

### 3. 特殊方块配方

当前 `Universe` 线已经允许合成：

- `minecraft:command_block`
- `minecraft:structure_block`
- `minecraft:jigsaw`
- `minecraft:barrier`

## 其他注意事项

### 1. 操作与快捷键

当前客户端注册了一个统一快捷键：

- `U`：切换工具模式

适用对象：

- `Supreme Mobile`
- `Universe Ultima Sword`
- `Universe Omni Blade`
- `Universe Console`
- `Universe Bucket`

另外：

- `Universe Boots` 的位移不是额外热键，而是“按住潜行时双击前进”触发。

### 2. 项目构建

#### 环境要求

- JDK `25`
- Gradle Wrapper（仓库已自带）
- 网络可访问 Fabric / Maven Central 依赖源

#### 常用命令

Windows：

```powershell
.\gradlew.bat build
.\gradlew.bat runClient
.\gradlew.bat runServer
```

Linux / macOS：

```bash
./gradlew build
./gradlew runClient
./gradlew runServer
```

#### 格式化与源码整理

`build.gradle` 里额外定义了几组 Java 源码整理任务，并且编译前会自动依赖它们：

```powershell
.\gradlew.bat formatJavaImports
.\gradlew.bat formatJavaAnnotations
.\gradlew.bat formatJavaDeclarations
.\gradlew.bat checkJavaImportGroups
```

这些任务会：

- 规范 import 分组
- 合并注解行
- 统一类成员声明排序
- 自动补齐 SPDX / 版权头

#### CI

仓库自带 GitHub Actions 工作流：

- 文件：`.github/workflows/build.yml`
- 运行环境：`ubuntu-24.04`
- JDK：`Microsoft OpenJDK 25`
- 默认执行：`./gradlew build`

#### 项目结构

```text
.
|- src/main/java/roeyqian/magnatour      # 通用逻辑、注册表、方块、实体、配方、网络
|- src/client/java/roeyqian/magnatour    # 客户端渲染、界面、按键、客户端混入
|- src/main/resources
|  |- assets/magnatour                   # 模型、贴图、语言、GUI
|  |- data/magnatour                     # 配方、维度、群系、结构、进度、标签
|  `- fabric.mod.json                    # 模组元数据
|- gradle/config/import-groups.json      # import 分组格式化规则
`- .github/workflows/build.yml           # CI
```

#### 说明

- 很多终局物品依赖 Mixin、Data Component 和自定义网络同步共同工作。
- README 描述的是当前代码已实现的行为；配方、平衡或维度规则变更后应同步更新文档。

## 许可证

本项目基于 GPL-v3 协议开源，详细信息请查看 `LICENSE`。

---

# English

| <span style="font-size: 16px;">[Overview](#overview)</span> | <span style="font-size: 16px;">[Core Functional Blocks](#core-functional-blocks)</span> | <span style="font-size: 16px;">[Key Equipment and Items](#key-equipment-and-items)</span> | <span style="font-size: 16px;">[Resources and Progression](#resources-and-progression)</span> | <span style="font-size: 16px;">[Additional Notes](#additional-notes)</span> | <span style="font-size: 16px;">[License](#license)</span> |
|:---:|:---:|:---:|:---:|:---:|:---:|

**Magnatour** is an adventure-focused content mod for Minecraft Fabric API. Its core identity is extreme overpowered stats and deliberately absurd mechanics, built around two clearly tiered progression lines:

- The **Supreme** line provides basic resource expansion, utility blocks, dimension access, and high-end equipment.
- The **Universe** line provides the strongest overpowered gear, remote access, cross-dimensional teleportation, expanded storage, and endgame recipes.

> Note: this project is currently developed by a single person. If you are strongly interested in the mod and want to participate in development, visit https://github.com/roeyqian/MagnatourDev

## Overview

### 1. Creative Tabs

The mod currently registers four separate creative tabs:

- `Supreme Blocks`
- `Supreme Items`
- `Universe Blocks`
- `Universe Items`

These tabs reflect the mod's main content categories.

### 2. Dimensions and World Generation

#### Dimension Overview

| Name | Dimension id | Current implementation |
|---|---|---|
| **Harvest Continent** | `harvest_continent` | Uses a custom `noise` generator and a custom `BiomeSource`; it is an agriculture and terrain themed dimension |
| **Ore Continent** | `ore_continent` | Uses a `flat` generator; terrain layers are composed of `bedrock + netherrack + deepslate + stone`, with a large set of ore features configured |

#### Current Biomes in Harvest Continent

| Name | Biome id | Biome characteristics |
|---|---|---|
| **Wheat Plain** | `wheat_plain` | Large plains terrain with wheat and tree features. |
| **Melon Jungle** | `melon_jungle` | Normal terrain variation with melons and woodland features. |
| **Pumpkin Gorge** | `pumpkin_gorge` | Extremely rugged terrain with heavily amplified pumpkin features. |
| **Big Lake** | `big_lake` | Large bodies of water, with deep sections reaching the void. |
| **Lake Center Island** | `lake_center_island` | A circular small island with abundant flowers, and the only structure biome for **Gold Bell Tower**. |

#### Current Features in Ore Continent

- The entire dimension uses only one custom biome: `ore_continent`
- It densely generates coal, iron, copper, gold, lapis, redstone, emerald, diamond, and their deep variants
- It also includes nether gold ore, nether quartz, and ancient debris
- The **Diamond City** structure generates in this dimension

### 3. Portals

Two custom dimension portals are currently implemented, with behavior close to the vanilla Nether portal:

| Portal | Frame block | Target dimension |
|---|---|---|
| `harvest_continent_portal` | `supreme_fodder_block` | `harvest_continent` |
| `ore_continent_portal` | `supreme_gem_block` | `ore_continent` |

Key points:

- The portals support a rectangular frame detection logic similar to vanilla portals.
- The smallest valid frame is `3` blocks wide and `4` blocks tall.
- They allow two-way travel between the Overworld and the corresponding dimension.
- Entering the portal requires about `80 ticks` of accumulation before teleportation triggers.
- Players in creative mode teleport immediately.
- If no nearby portal exists in the target dimension, the code automatically places a new portal frame on arrival.

### 4. Structures

| Name | Structure id | Location | Notes |
|---|---|---|---|
| **Gold Bell Tower** | `gold_bell_tower` | Lake Center Island | Exclusive structure of Harvest Continent |
| **Diamond City** | `diamond_city` | Ore Continent | Primary structure of Ore Continent |

### 5. Entities and Summoning

#### Currently Registered Major Entities

- **Monster**

  - | `bell_ringer` | Bell Ringer | 50 attack, 500 health, 20 armor, warrior role |
    |---|---|---|

  - | `bell_soul` | Bell Soul | 20 attack, 50 health, 0 armor, mosquito role |
    |---|---|---|

  - | `obsidian_golem` | Obsidian Golem | 50 attack, 2000 health, 50 armor, titan role |
    |---|---|---|

- **Unknown**

  - | `the_unnameable_thing` | The Unnameable Thing | 0 attack, 100000 health, 0 armor, walnut role |
    |---|---|---|

- **Boss**

  - | `sculk_behemoth` | Sculk Behemoth | 100 attack, 20000 health, 100 armor, boss role |
    |---|---|---|

  - | `pale_lord` | Pale Lord | 1~2147483647 attack, 20 health, 10 armor, boss role |
    |---|---|---|

    > `pale_lord_clone` **Pale Lord Clone** cannot be killed and has the same attack value.

- **Neutral**

  - None for now

- **Friendly**

  - | `netherite_golem` | Netherite Golem | 100 attack, 8000 health, 100 armor, super titan role |
    |---|---|---|

  - | `universe_guardian` | Universe Guardian | 2147483647 attack, 2147483647 health, 2147483647 armor, invincible role |
    |---|---|---|

#### Currently Implemented Multiblock Summons

`SummonStructureHelper` currently implements at least the following summon structures:

- `netherite_golem`
  - Head: `supreme_pumpkin_head`
  - Body: `netherite_block`
  - Shape similar to an Iron Golem
- `obsidian_golem`
  - Head: `supreme_pumpkin_head`
  - Body: `crying_obsidian`
  - Shape similar to an Iron Golem
- `pale_lord`
  - Core: `creaking_heart`
  - Outer layer: `supreme_fodder_block`
  - Cross-shaped shell around the center
- `sculk_behemoth`
  - Triggered by a larger structure made from `supreme_gem_block` + `sculk` + `sculk_catalyst`

The summon implementation is located at [src/main/java/roeyqian/magnatour/block/SummonStructureHelper.java](./src/main/java/roeyqian/magnatour/block/SummonStructureHelper.java).

## Core Functional Blocks

### 1. Supreme Functional Blocks

| Name | Block id | Current implementation |
|---|---|---|
| **Supreme Worktable** | `supreme_worktable` | Custom crafting table that hosts `supreme_crafting` recipes |
| **Supreme Furnace** | `supreme_furnace` | Custom furnace supporting `supreme_cooking`, with `8x` fuel efficiency and greatly reduced smelting time |
| **Supreme Reserver** | `supreme_reserver` | Reverse recipe viewer and dismantling table that reconstructs ingredient lists from output items |
| **Supreme Chest** | `supreme_chest` | Custom large chest with combination logic and special rendering |
| **Redstone Trigger** | `redstone_trigger` | A configurable redstone trigger with frequency, switch, and mode options (continuous / pulse) |
| **Item Hub** | `item_hub` | Similar to a hopper, but with high-speed intake, filtering, directed output, and item ID based filtering |
| **Logistics Fiber** | `logistics_fiber` | Six-direction logistics network block with automatic container connections |

### 2. Universe Functional Blocks

| Name | Block id | Current implementation |
|---|---|---|
| **Universe Workstation** | `universe_workstation` | Custom crafting station that hosts `universe_crafting` recipes |
| **Universe Refinery** | `universe_refinery` | Custom furnace supporting vanilla + `supreme_cooking` + `universe_cooking`, with `32x` fuel efficiency and up to `2` items processed per tick |
| **Universe Library** | `universe_library` | A shulker-box-like container with `252` slots |
| **Universe Void Pool** | `universe_void_pool` | Ultimate item duplication pool that copies any item without restriction |
| **Universe Teleport Point** | `universe_teleport_point` | Teleport node that maintains a list of cross-dimensional coordinates |
| **Universe Block** | `universe_block` | Special block with switchable virtual lighting behavior |

## Key Equipment and Items

### 1. Supreme Line

| Item | Current implementation |
| --- | --- |
| `Supreme` full tool / armor set | Unbreakable and forcibly rendered with enchanted glint |
| `Supreme Mobile` | Two switchable modes: scan functional blocks / remotely emulate block UIs that have no block entity data |
| `Strange Potion` series | Applies several random effects from a shared effect pool to the target |
| `Chunk TNT` | Instead of behaving like normal TNT, it clears the entire chunk and deals `10000` damage to entities inside it |

#### Supreme Mobile

The current `Supreme Mobile` logic is:

- `Mode 0`: remotely opens the GUI of a previously scanned functional block
- `Mode 1`: right-click a functional block to scan and record its block id

The limitations are also explicit:

- It can only emulate blocks that have menus but **do not store data in block entities**
- It has no effect on ordinary blocks without GUIs

### 2. Universe Line

Core gear in the `Universe` line has the following default properties:

- `EPIC` rarity
- Unbreakable
- Enchanted glint
- Additional damage resistance tags

| Item | Current implementation |
| --- | --- |
| `Universe Stick` | Damage scales with the player's experience level and repeatedly triggers the target's loot table based on that level |
| `Universe Ultima Sword` | Mode switching: ignition / absolute execution and fireball projection |
| `Universe Omni Blade` | Mode switching: area terraforming / chain destruction of up to `512` blocks |
| `Universe Console` | Binds functional blocks and opens them remotely across chunks and dimensions |
| `Universe Bucket` | Can be drunk to clear status effects, and can switch between infinite water and infinite lava modes |
| `Universe Star` | Restores health when used and also serves as a very long-lasting fuel |
| `Universe Guardian Spawn Egg` | Summons and tames `Universe Guardian` |

#### Universe Console

Current behavior:

- In `Mode 1`, it can bind any block that has a menu
- Binding data is written into item components, including `position + dimension + display name`
- In `Mode 0`, the interface can remotely open the target block
- The target chunk is force-loaded before opening
- Cross-dimensional access is supported
- Bound entries can be deleted

Remote access lifecycle management is handled by `RemoteAccessManager`.

#### Universe Ultima Sword

It currently has two modes:

- `Mode 0`
  - Right-click performs ignition logic
  - Attacks set targets on fire and deal extremely high damage
- `Mode 1`
  - Right-click launches `UniverseFireball`
  - Attacks trigger lightning and forced execution logic
  - `Universe Guardian` can only be genuinely damaged in this mode

#### Universe Omni Blade

It currently has two modes:

- `Mode 0`
  - Combines tilling and surface flattening logic
  - Can convert `Ever-Water` surfaces into dedicated farmland
- `Mode 1`
  - Performs a BFS from the starting block
  - Can chain-break up to `512` blocks of the same type

#### Universe Bucket

Current behavior:

- Drinking it clears all status effects using milk-bucket-like logic
- Switching is done through a hotkey:
  - `Mode 0`: infinite water placement
  - `Mode 1`: infinite lava placement
- It can also absorb water sources / lava sources from the world through dedicated network packets and sync the selected mode

### 3. Universe Armor

| Armor | Current implementation |
| --- | --- |
| `Universe Helmet` | Locks hunger and air values, grants immunity to `Hunger / Nausea / Darkness / Blindness`, and improves underwater and lava vision |
| `Universe Chestplate` | Removes fire and freezing, clears poison, and grants flight |
| `Universe Leggings` | Sprint acceleration, higher step height, stronger jumps, and additional speed while flying |
| `Universe Boots` | Allows walking on water and lava; while sneaking, a short-range flash dash is triggered by holding sneak and double-tapping forward |

## Resources and Progression

### 1. Current Resource Route

Based on the current recipes and progression resources, progression is roughly divided into three stages:

1. `Supreme` base resources and tools
2. Enter `Harvest Continent / Ore Continent` to obtain more advanced core materials
3. Reach the endgame through `Universe` gems, `Universe Light / Dark`, `Universe Primary Block`, and `Universe Star`

This ordering is derived from the current `recipe` and `advancement` resources.

### 2. Key Recipe Nodes

Some especially important recipe / smelting nodes are:

- `Fruit of All Things -> Harvest Core` (`supreme_cooking`)
- `Supreme Metal -> Ore Core` (`supreme_cooking`)
- `Rainbow Thing -> Supreme Core` (very long `supreme_cooking`)
- `Universe Gem Red + Yellow + White -> Universe Light`
- `Universe Gem Green + Blue + Black -> Universe Dark`
- `Universe Light + Universe Dark -> Universe Primary Block`
- `Bedrock -> Universe Primary Fragment` (`universe_cooking`)
- `Universe Primary Block -> Universe Star` (`universe_cooking`)

### 3. Special Block Recipes

The current `Universe` line already allows crafting:

- `minecraft:command_block`
- `minecraft:structure_block`
- `minecraft:jigsaw`
- `minecraft:barrier`

## Additional Notes

### 1. Controls and Hotkeys

The client currently registers one unified hotkey:

- `U`: switch tool mode

Applicable items:

- `Supreme Mobile`
- `Universe Ultima Sword`
- `Universe Omni Blade`
- `Universe Console`
- `Universe Bucket`

Additionally:

- `Universe Boots` movement is not a separate hotkey; it is triggered by holding sneak and double-tapping forward.

### 2. Project Build

#### Requirements

- JDK `25`
- Gradle Wrapper (already included in the repository)
- Network access to Fabric / Maven Central dependency sources

#### Common Commands

Windows:

```powershell
.\gradlew.bat build
.\gradlew.bat runClient
.\gradlew.bat runServer
```

Linux / macOS:

```bash
./gradlew build
./gradlew runClient
./gradlew runServer
```

#### Formatting and Source Cleanup

`build.gradle` also defines several Java source cleanup tasks, and compilation depends on them automatically:

```powershell
.\gradlew.bat formatJavaImports
.\gradlew.bat formatJavaAnnotations
.\gradlew.bat formatJavaDeclarations
.\gradlew.bat checkJavaImportGroups
```

These tasks:

- Standardize import groups
- Merge annotation lines
- Normalize member declaration ordering
- Automatically add SPDX / copyright headers

#### CI

The repository includes a GitHub Actions workflow:

- File: `.github/workflows/build.yml`
- Runner: `ubuntu-24.04`
- JDK: `Microsoft OpenJDK 25`
- Default command: `./gradlew build`

#### Project Structure

```text
.
|- src/main/java/roeyqian/magnatour      # Shared logic, registries, blocks, entities, recipes, networking
|- src/client/java/roeyqian/magnatour    # Client rendering, UI, keybindings, client mixins
|- src/main/resources
|  |- assets/magnatour                   # Models, textures, languages, GUI
|  |- data/magnatour                     # Recipes, dimensions, biomes, structures, advancements, tags
|  `- fabric.mod.json                    # Mod metadata
|- gradle/config/import-groups.json      # Import group formatting rules
`- .github/workflows/build.yml           # CI
```

#### Notes

- Many endgame items depend on Mixin, Data Component, and custom networking synchronization working together.
- This README describes behavior already implemented in the current code; when recipes, balance, or dimension rules change, the documentation should be updated accordingly.

## License

This project is open-sourced under the GPL-v3 license. See `LICENSE` for details.

---

# 日本語

| <span style="font-size: 16px;">[コンテンツ概要](#コンテンツ概要)</span> | <span style="font-size: 16px;">[中核機能ブロック](#中核機能ブロック)</span> | <span style="font-size: 16px;">[主要装備とアイテム](#主要装備とアイテム)</span> | <span style="font-size: 16px;">[資源と進行](#資源と進行)</span> | <span style="font-size: 16px;">[その他の注意事項](#その他の注意事項)</span> | <span style="font-size: 16px;">[ライセンス](#ライセンス)</span> |
|:---:|:---:|:---:|:---:|:---:|:---:|

**Magnatour** は Minecraft Fabric API 向けのアドベンチャー系コンテンツ MOD です。極端にインフレした数値と意図的に常識外れなギミックを中核にし、明確に段階分けされた 2 本の進行ラインで構成されています。

- **Supreme** 系統は、基本資源の拡張、機能ブロック、次元への入口、高級装備を提供します。
- **Universe** 系統は、最上位の超性能装備、遠隔アクセス、次元間転送、拡張ストレージ、終盤レシピを提供します。

> 注意: このプロジェクトは現在個人開発です。強い興味があり開発に参加したい場合は https://github.com/roeyqian/MagnatourDev を確認してください。

## コンテンツ概要

### 1. クリエイティブタブ

この MOD は現在、4 つの独立したクリエイティブタブを登録します。

- `超凡方块类`
- `超凡物品类`
- `寰宇方块类`
- `寰宇物品类`

これら 4 つのタブが、現時点での主なコンテンツ分類に対応しています。

### 2. 次元とワールド生成

#### 次元紹介

| 名称 | 次元 id | 現在の実装 |
|---|---|---|
| **Harvest Continent** | `harvest_continent` | カスタム `noise` ジェネレーターとカスタム `BiomeSource` を使用する、農業 / 地形テーマの次元 |
| **Ore Continent** | `ore_continent` | `flat` ジェネレーターを使用し、地層は `bedrock + netherrack + deepslate + stone` で構成され、多数の鉱石特性が設定されています |

#### Harvest Continent の現在のバイオーム

| 名称 | バイオーム id | 特徴 |
|---|---|---|
| **Wheat Plain** | `wheat_plain` | 広い平原地形で、小麦と樹木の特性が配置されます。 |
| **Melon Jungle** | `melon_jungle` | 通常の起伏を持つ地形で、スイカと森林系の特性が生成されます。 |
| **Pumpkin Gorge** | `pumpkin_gorge` | 非常に険しい地形で、カボチャ特性が強化されます。 |
| **Big Lake** | `big_lake` | 広大な水域で、深部は虚空に達します。 |
| **Lake Center Island** | `lake_center_island` | 円盤状の小島で大量の花が生成され、**Gold Bell Tower** が出現する唯一の構造バイオームです。 |

#### Ore Continent の現在の特性

- 次元全体で使用されるカスタムバイオームは `ore_continent` のみです
- 石炭、鉄、銅、金、ラピス、レッドストーン、エメラルド、ダイヤモンド、およびその深層変種が高密度で生成されます
- ネザー金鉱石、ネザークォーツ、古代の残骸も含まれます
- **Diamond City** 構造物がこの次元に生成されます

### 3. ポータル

現在 2 種類のカスタム次元ポータルが実装されており、挙動はバニラのネザーポータルに近いです。

| ポータル | 枠ブロック | 目標次元 |
|---|---|---|
| `harvest_continent_portal` | `supreme_fodder_block` | `harvest_continent` |
| `ore_continent_portal` | `supreme_gem_block` | `ore_continent` |

要点:

- バニラのポータルに近い長方形フレーム検出ロジックを持ちます。
- 最小で有効なポータル枠は「幅 `3`、高さ `4`」です。
- オーバーワールドと対応次元の間を双方向に移動できます。
- ポータル内部に入ってから転送が発動するまでに約 `80 tick` の蓄積が必要です。
- クリエイティブモードのプレイヤーは即座に転送されます。
- 目標次元の近くに既存ポータルがない場合、到着時にコードが自動でポータル枠を生成します。

### 4. 構造物

| 名称 | 構造物 id | 位置 | 説明 |
|---|---|---|---|
| **Gold Bell Tower** | `gold_bell_tower` | Lake Center Island | Harvest Continent 専用の構造物 |
| **Diamond City** | `diamond_city` | Ore Continent | Ore Continent の主要構造物 |

### 5. エンティティと召喚

#### 現在登録されている主なエンティティ

- **モンスター**

  - | `bell_ringer` | Bell Ringer | 50 攻撃、500 体力、20 防御、戦士ポジション |
    |---|---|---|

  - | `bell_soul` | Bell Soul | 20 攻撃、50 体力、0 防御、蚊ポジション |
    |---|---|---|

  - | `obsidian_golem` | Obsidian Golem | 50 攻撃、2000 体力、50 防御、タイタンポジション |
    |---|---|---|

- **未知**

  - | `the_unnameable_thing` | The Unnameable Thing | 0 攻撃、100000 体力、0 防御、木の実ポジション |
    |---|---|---|

- **ボス**

  - | `sculk_behemoth` | Sculk Behemoth | 100 攻撃、20000 体力、100 防御、ボスポジション |
    |---|---|---|

  - | `pale_lord` | Pale Lord | 1~2147483647 攻撃、20 体力、10 防御、ボスポジション |
    |---|---|---|

    > `pale_lord_clone` **Pale Lord Clone** は倒せず、攻撃力は同じです。

- **中立**

  - 現時点ではなし

- **友好**

  - | `netherite_golem` | Netherite Golem | 100 攻撃、8000 体力、100 防御、スーパータイタンポジション |
    |---|---|---|

  - | `universe_guardian` | Universe Guardian | 2147483647 攻撃、2147483647 体力、2147483647 防御、無敵ポジション |
    |---|---|---|

#### 現在実装されているマルチブロック召喚

`SummonStructureHelper` には少なくとも以下の召喚構造が実装されています。

- `netherite_golem`
  - 頭: `supreme_pumpkin_head`
  - 胴体: `netherite_block`
  - 形状はアイアンゴーレムに近い
- `obsidian_golem`
  - 頭: `supreme_pumpkin_head`
  - 胴体: `crying_obsidian`
  - 形状はアイアンゴーレムに近い
- `pale_lord`
  - コア: `creaking_heart`
  - 外層: `supreme_fodder_block`
  - 中心を十字形に包む構造
- `sculk_behemoth`
  - `supreme_gem_block` + `sculk` + `sculk_catalyst` で構成される大きめの構造で発動

召喚実装は [src/main/java/roeyqian/magnatour/block/SummonStructureHelper.java](./src/main/java/roeyqian/magnatour/block/SummonStructureHelper.java) にあります。

## 中核機能ブロック

### 1. Supreme 系機能ブロック

| 名称 | ブロック id | 現在の実装 |
|---|---|---|
| **Supreme Worktable** | `supreme_worktable` | `supreme_crafting` レシピを扱うカスタム作業台 |
| **Supreme Furnace** | `supreme_furnace` | `supreme_cooking` に対応するカスタム炉。燃料効率は `8x`、精錬時間は大幅短縮 |
| **Supreme Reserver** | `supreme_reserver` | 出力アイテムから元レシピ材料を逆算する、逆引きレシピ閲覧 / 分解台 |
| **Supreme Chest** | `supreme_chest` | 結合ロジックと特殊描画を持つカスタム大型チェスト |
| **Redstone Trigger** | `redstone_trigger` | 周期、スイッチ、モード（連続 / パルス）を設定できるレッドストーントリガー |
| **Item Hub** | `item_hub` | ホッパーに近いが、高速吸入、フィルタ、指向性出力、アイテム ID フィルタを備える |
| **Logistics Fiber** | `logistics_fiber` | 六方向接続の物流ネットワークブロックで、コンテナに自動接続可能 |

### 2. Universe 系機能ブロック

| 名称 | ブロック id | 現在の実装 |
|---|---|---|
| **Universe Workstation** | `universe_workstation` | `universe_crafting` レシピを扱うカスタム作業台 |
| **Universe Refinery** | `universe_refinery` | バニラ + `supreme_cooking` + `universe_cooking` に対応するカスタム炉。燃料効率 `32x`、1 tick あたり最大 `2` 個処理 |
| **Universe Library** | `universe_library` | `252` スロットを持つシュルカーボックス系コンテナ |
| **Universe Void Pool** | `universe_void_pool` | 制限なくあらゆるアイテムを複製できる究極のコピー装置 |
| **Universe Teleport Point** | `universe_teleport_point` | 次元をまたぐ座標一覧を保持できる転送ポイント |
| **Universe Block** | `universe_block` | 仮想光源状態を切り替えられる特殊ブロック |

## 主要装備とアイテム

### 1. Supreme 系

| アイテム | 現在の実装 |
| --- | --- |
| `Supreme` 一式ツール / 防具 | 耐久無限で、強制的にエンチャント光沢を表示 |
| `Supreme Mobile` | 2 つのモードを切替: 機能ブロックのスキャン / ブロックエンティティのデータを持たない UI の遠隔再現 |
| `Strange Potion` 系列 | 共通効果プールからランダムな複数効果を対象に付与 |
| `Chunk TNT` | 通常の TNT ではなく、チャンク全体を消去し、内部エンティティに `10000` ダメージを与える |

#### Supreme Mobile

現在の `Supreme Mobile` のロジック:

- `モード 0`: 以前にスキャンした機能ブロックの GUI を遠隔で開く
- `モード 1`: 機能ブロックを右クリックしてスキャンし、ブロック id を記録する

制限:

- メニューはあるが **ブロックエンティティにデータを保存しない** ブロックのみ再現可能
- 通常の GUI を持たないブロックには無効

### 2. Universe 系

`Universe` 系の中核装備は、標準で以下の特性を持ちます。

- `EPIC` レアリティ
- 耐久無限
- エンチャント光沢付き
- 追加ダメージ耐性タグ付き

| アイテム | 現在の実装 |
| --- | --- |
| `Universe Stick` | ダメージがプレイヤー経験値レベルに応じて増加し、そのレベル回数だけ対象のルートテーブルを再実行 |
| `Universe Ultima Sword` | モード切替: 点火 / 絶対処刑 とファイアボール射出 |
| `Universe Omni Blade` | モード切替: 範囲整地 / 最大 `512` ブロックの連鎖破壊 |
| `Universe Console` | 機能ブロックを紐付けし、チャンクや次元を超えて遠隔で開く |
| `Universe Bucket` | 飲むと状態異常を解除し、無限水 / 無限溶岩モードを切り替え可能 |
| `Universe Star` | 使用で回復し、非常に長持ちする燃料にもなる |
| `Universe Guardian Spawn Egg` | `Universe Guardian` を召喚して手懐けられる |

#### Universe Console

現在の挙動:

- `モード 1` では、メニューを持つ任意のブロックを紐付け可能
- 紐付け情報はアイテムコンポーネントに `座標 + 次元 + 表示名` として書き込まれる
- `モード 0` では、UI から対象ブロックを遠隔で開ける
- 開く前に対象チャンクを強制ロードする
- 次元をまたぐアクセスに対応
- 紐付けの削除に対応

遠隔アクセスのライフサイクルは `RemoteAccessManager` が管理します。

#### Universe Ultima Sword

現在 2 つのモードがあります。

- `モード 0`
  - 右クリックで点火ロジックを実行
  - 攻撃時に対象を炎上させ、非常に高いダメージを与える
- `モード 1`
  - 右クリックで `UniverseFireball` を発射
  - 攻撃時に雷と強制処刑ロジックを発動
  - `Universe Guardian` が本当にダメージを受けられるのはこのモードのみ

#### Universe Omni Blade

現在 2 つのモードがあります。

- `モード 0`
  - クワ化と地表整地の両方を行う
  - `Ever-Water` 系の地表を専用農地に変換可能
- `モード 1`
  - 開始ブロックを起点に BFS を実行
  - 同種ブロックを最大 `512` 個まで連鎖破壊可能

#### Universe Bucket

現在の挙動:

- 飲むとミルクバケツ相当の処理で全状態異常を解除
- ホットキーで切替:
  - `モード 0`: 無限に水を配置
  - `モード 1`: 無限に溶岩を配置
- 専用ネットワークパケットでワールドから水源 / 溶岩源を吸収し、モードも同期できる

### 3. Universe 防具

| 防具 | 現在の実装 |
| --- | --- |
| `Universe Helmet` | 空腹値と酸素値を固定し、`Hunger / Nausea / Darkness / Blindness` を無効化し、水中と溶岩内の視界を改善 |
| `Universe Chestplate` | 炎上と凍結を除去し、毒を解除し、飛行能力を付与 |
| `Universe Leggings` | ダッシュ加速、段差上昇量増加、ジャンプ強化、飛行中の追加加速 |
| `Universe Boots` | 水上 / 溶岩上歩行が可能。しゃがみ中に「しゃがみを維持して前進を二度押し」で短距離瞬間移動ダッシュを発動 |

## 資源と進行

### 1. 現在の資源ライン

現在のレシピと進行用リソースに基づくと、進行はおおむね 3 段階に分かれます。

1. `Supreme` の基礎資源とツール
2. `Harvest Continent / Ore Continent` に入り、さらに高度なコア素材を取得
3. `Universe` 宝石、`Universe Light / Dark`、`Universe Primary Block`、`Universe Star` を通じて終盤へ進む

この順序は、現在の `recipe` と `advancement` リソースから整理したものです。

### 2. 重要なレシピ節点

特に重要なレシピ / 精錬節点:

- `Fruit of All Things -> Harvest Core` (`supreme_cooking`)
- `Supreme Metal -> Ore Core` (`supreme_cooking`)
- `Rainbow Thing -> Supreme Core`（非常に長時間の `supreme_cooking`）
- `Universe Gem Red + Yellow + White -> Universe Light`
- `Universe Gem Green + Blue + Black -> Universe Dark`
- `Universe Light + Universe Dark -> Universe Primary Block`
- `Bedrock -> Universe Primary Fragment` (`universe_cooking`)
- `Universe Primary Block -> Universe Star` (`universe_cooking`)

### 3. 特殊ブロックレシピ

現在の `Universe` 系では以下もクラフト可能です。

- `minecraft:command_block`
- `minecraft:structure_block`
- `minecraft:jigsaw`
- `minecraft:barrier`

## その他の注意事項

### 1. 操作とホットキー

現在クライアントには共通ホットキーが 1 つ登録されています。

- `U`: ツールモード切替

対象:

- `Supreme Mobile`
- `Universe Ultima Sword`
- `Universe Omni Blade`
- `Universe Console`
- `Universe Bucket`

補足:

- `Universe Boots` の移動能力は別ホットキーではなく、「しゃがみを維持して前進を二度押し」で発動します。

### 2. プロジェクトのビルド

#### 環境要件

- JDK `25`
- Gradle Wrapper（リポジトリに同梱）
- Fabric / Maven Central の依存先へ接続できるネットワーク

#### よく使うコマンド

Windows:

```powershell
.\gradlew.bat build
.\gradlew.bat runClient
.\gradlew.bat runServer
```

Linux / macOS:

```bash
./gradlew build
./gradlew runClient
./gradlew runServer
```

#### フォーマットとソース整理

`build.gradle` には Java ソース整理タスクも定義されており、コンパイル前に自動で依存します。

```powershell
.\gradlew.bat formatJavaImports
.\gradlew.bat formatJavaAnnotations
.\gradlew.bat formatJavaDeclarations
.\gradlew.bat checkJavaImportGroups
```

これらのタスクは以下を行います。

- import グループの標準化
- アノテーション行の統合
- メンバー宣言順の統一
- SPDX / 著作権ヘッダーの自動補完

#### CI

GitHub Actions ワークフローが同梱されています。

- ファイル: `.github/workflows/build.yml`
- 実行環境: `ubuntu-24.04`
- JDK: `Microsoft OpenJDK 25`
- 標準実行コマンド: `./gradlew build`

#### プロジェクト構成

```text
.
|- src/main/java/roeyqian/magnatour      # 共通ロジック、レジストリ、ブロック、エンティティ、レシピ、ネットワーク
|- src/client/java/roeyqian/magnatour    # クライアント描画、UI、キー入力、クライアント mixin
|- src/main/resources
|  |- assets/magnatour                   # モデル、テクスチャ、言語、GUI
|  |- data/magnatour                     # レシピ、次元、バイオーム、構造物、進捗、タグ
|  `- fabric.mod.json                    # MOD メタデータ
|- gradle/config/import-groups.json      # import グループ整形ルール
`- .github/workflows/build.yml           # CI
```

#### 説明

- 多くの終盤アイテムは、Mixin、Data Component、カスタムネットワーク同期の連携に依存しています。
- この README は現在コードで実装済みの挙動を説明しています。レシピ、バランス、次元ルールが変わった場合は文書も更新してください。

## ライセンス

このプロジェクトは GPL-v3 ライセンスのもとで公開されています。詳細は `LICENSE` を参照してください。

---

# Español

| <span style="font-size: 16px;">[Resumen del contenido](#resumen-del-contenido)</span> | <span style="font-size: 16px;">[Bloques funcionales principales](#bloques-funcionales-principales)</span> | <span style="font-size: 16px;">[Equipamiento y objetos clave](#equipamiento-y-objetos-clave)</span> | <span style="font-size: 16px;">[Recursos y progresión](#recursos-y-progresión)</span> | <span style="font-size: 16px;">[Otras notas](#otras-notas)</span> | <span style="font-size: 16px;">[Licencia](#licencia)</span> |
|:---:|:---:|:---:|:---:|:---:|:---:|

**Magnatour** es un mod de contenido orientado a la aventura para Minecraft Fabric API. Su identidad central se basa en valores exageradamente rotos y mecánicas deliberadamente absurdas, organizadas alrededor de dos líneas de progresión claramente escalonadas:

- La línea **Supreme** ofrece expansión de recursos básicos, bloques funcionales, acceso a dimensiones y equipamiento avanzado.
- La línea **Universe** ofrece el equipo más roto del mod, acceso remoto, teletransporte entre dimensiones, almacenamiento ampliado y recetas de final de juego.

> Nota: este proyecto se desarrolla actualmente de forma individual. Si te interesa mucho el mod y quieres participar en el desarrollo, visita https://github.com/roeyqian/MagnatourDev

## Resumen del contenido

### 1. Pestañas del modo creativo

El mod registra cuatro pestañas independientes:

- `超凡方块类`
- `超凡物品类`
- `寰宇方块类`
- `寰宇物品类`

Estas cuatro pestañas corresponden a las categorías principales del contenido actual del mod.

### 2. Dimensiones y generación del mundo

#### Introducción a las dimensiones

| Nombre | id de dimensión | Implementación actual |
|---|---|---|
| **Harvest Continent** | `harvest_continent` | Usa un generador `noise` personalizado y un `BiomeSource` personalizado; es una dimensión temática de agricultura y relieve |
| **Ore Continent** | `ore_continent` | Usa un generador `flat`; las capas del terreno están formadas por `bedrock + netherrack + deepslate + stone` y se configuran muchas características minerales |

#### Biomas actuales de Harvest Continent

| Nombre | id de bioma | Características del bioma |
|---|---|---|
| **Wheat Plain** | `wheat_plain` | Terreno de gran llanura, con generación de trigo y árboles. |
| **Melon Jungle** | `melon_jungle` | Relieve normal con generación de melones y zonas boscosas. |
| **Pumpkin Gorge** | `pumpkin_gorge` | Terreno extremadamente accidentado, con presencia reforzada de calabazas. |
| **Big Lake** | `big_lake` | Grandes masas de agua cuya parte profunda llega al vacío. |
| **Lake Center Island** | `lake_center_island` | Pequeña isla circular con muchas flores, y el único bioma estructural para **Gold Bell Tower**. |

#### Características actuales de Ore Continent

- Toda la dimensión usa un único bioma personalizado: `ore_continent`
- Genera en gran cantidad carbón, hierro, cobre, oro, lapislázuli, redstone, esmeralda, diamante y sus variantes profundas
- También incluye oro del Nether, cuarzo del Nether y escombros ancestrales
- La estructura **Diamond City** se genera en esta dimensión

### 3. Portales

Actualmente hay dos portales de dimensión personalizados, con un comportamiento parecido al portal del Nether vanilla:

| Portal | Bloque del marco | Dimensión objetivo |
|---|---|---|
| `harvest_continent_portal` | `supreme_fodder_block` | `harvest_continent` |
| `ore_continent_portal` | `supreme_gem_block` | `ore_continent` |

Puntos clave:

- Los portales usan una lógica de detección rectangular similar a la de los portales vanilla.
- El marco válido más pequeño mide `3` bloques de ancho y `4` de alto.
- Permiten viajar en ambos sentidos entre el Overworld y la dimensión correspondiente.
- Entrar al portal requiere acumular unos `80 ticks` antes de activar el teletransporte.
- Los jugadores en modo creativo se teletransportan de inmediato.
- Si no existe un portal cercano en la dimensión de destino, el código genera automáticamente uno al llegar.

### 4. Estructuras

| Nombre | id de estructura | Ubicación | Descripción |
|---|---|---|---|
| **Gold Bell Tower** | `gold_bell_tower` | Lake Center Island | Estructura exclusiva de Harvest Continent |
| **Diamond City** | `diamond_city` | Ore Continent | Estructura principal de Ore Continent |

### 5. Entidades y invocación

#### Entidades principales registradas actualmente

- **Monstruos**

  - | `bell_ringer` | Bell Ringer | 50 de ataque, 500 de vida, 20 de armadura, rol de guerrero |
    |---|---|---|

  - | `bell_soul` | Bell Soul | 20 de ataque, 50 de vida, 0 de armadura, rol de mosquito |
    |---|---|---|

  - | `obsidian_golem` | Obsidian Golem | 50 de ataque, 2000 de vida, 50 de armadura, rol de titán |
    |---|---|---|

- **Desconocidos**

  - | `the_unnameable_thing` | The Unnameable Thing | 0 de ataque, 100000 de vida, 0 de armadura, rol de nuez |
    |---|---|---|

- **Jefes**

  - | `sculk_behemoth` | Sculk Behemoth | 100 de ataque, 20000 de vida, 100 de armadura, rol de jefe |
    |---|---|---|

  - | `pale_lord` | Pale Lord | 1~2147483647 de ataque, 20 de vida, 10 de armadura, rol de jefe |
    |---|---|---|

    > `pale_lord_clone` **Pale Lord Clone** no puede morir y tiene el mismo valor de ataque.

- **Neutrales**

  - Ninguno por ahora

- **Amistosos**

  - | `netherite_golem` | Netherite Golem | 100 de ataque, 8000 de vida, 100 de armadura, rol de supertitán |
    |---|---|---|

  - | `universe_guardian` | Universe Guardian | 2147483647 de ataque, 2147483647 de vida, 2147483647 de armadura, rol invencible |
    |---|---|---|

#### Invocaciones multibloque implementadas actualmente

`SummonStructureHelper` ya implementa al menos las siguientes estructuras de invocación:

- `netherite_golem`
  - Cabeza: `supreme_pumpkin_head`
  - Cuerpo: `netherite_block`
  - Forma similar a la de un gólem de hierro
- `obsidian_golem`
  - Cabeza: `supreme_pumpkin_head`
  - Cuerpo: `crying_obsidian`
  - Forma similar a la de un gólem de hierro
- `pale_lord`
  - Núcleo: `creaking_heart`
  - Capa exterior: `supreme_fodder_block`
  - Estructura en cruz envolviendo el centro
- `sculk_behemoth`
  - Se activa mediante una estructura de mayor tamaño formada por `supreme_gem_block` + `sculk` + `sculk_catalyst`

La implementación de invocación está en [src/main/java/roeyqian/magnatour/block/SummonStructureHelper.java](./src/main/java/roeyqian/magnatour/block/SummonStructureHelper.java).

## Bloques funcionales principales

### 1. Bloques funcionales de Supreme

| Nombre | id de bloque | Implementación actual |
|---|---|---|
| **Supreme Worktable** | `supreme_worktable` | Mesa de trabajo personalizada que contiene recetas `supreme_crafting` |
| **Supreme Furnace** | `supreme_furnace` | Horno personalizado compatible con `supreme_cooking`, con eficiencia de combustible `8x` y tiempo de fundición muy reducido |
| **Supreme Reserver** | `supreme_reserver` | Mesa de desmontaje y consulta inversa de recetas que reconstruye materiales a partir del objeto resultado |
| **Supreme Chest** | `supreme_chest` | Cofre grande personalizado con lógica de combinación y renderizado especial |
| **Redstone Trigger** | `redstone_trigger` | Disparador de redstone configurable con frecuencia, interruptor y modos (continuo / pulso) |
| **Item Hub** | `item_hub` | Similar a una tolva, pero con absorción rápida, filtrado, salida dirigida y filtrado por ID de objeto |
| **Logistics Fiber** | `logistics_fiber` | Bloque de red logística con conexión en seis direcciones y acceso automático a contenedores |

### 2. Bloques funcionales de Universe

| Nombre | id de bloque | Implementación actual |
|---|---|---|
| **Universe Workstation** | `universe_workstation` | Estación de trabajo personalizada que contiene recetas `universe_crafting` |
| **Universe Refinery** | `universe_refinery` | Horno personalizado compatible con vanilla + `supreme_cooking` + `universe_cooking`, con eficiencia de combustible `32x` y procesamiento de hasta `2` objetos por tick |
| **Universe Library** | `universe_library` | Contenedor tipo caja de shulker con `252` espacios |
| **Universe Void Pool** | `universe_void_pool` | Dispositivo definitivo de duplicación que copia cualquier objeto sin restricciones |
| **Universe Teleport Point** | `universe_teleport_point` | Punto de teletransporte que mantiene una lista de coordenadas entre dimensiones |
| **Universe Block** | `universe_block` | Bloque especial con comportamiento de iluminación virtual conmutable |

## Equipamiento y objetos clave

### 1. Línea Supreme

| Objeto | Implementación actual |
| --- | --- |
| Conjunto completo de herramientas / armadura `Supreme` | Irrompible y renderizado forzado con brillo de encantamiento |
| `Supreme Mobile` | Dos modos intercambiables: escanear bloques funcionales / emular a distancia interfaces sin datos en block entity |
| Serie `Strange Potion` | Aplica varios efectos aleatorios tomados de una reserva compartida |
| `Chunk TNT` | En lugar de comportarse como TNT normal, vacía el chunk completo y causa `10000` de daño a las entidades dentro |

#### Supreme Mobile

La lógica actual de `Supreme Mobile` es:

- `Modo 0`: abre a distancia la GUI de un bloque funcional escaneado previamente
- `Modo 1`: clic derecho sobre un bloque funcional para escanearlo y registrar su id

Limitaciones:

- Solo puede emular bloques que tengan menú pero **no guarden datos en block entities**
- No funciona con bloques normales sin GUI

### 2. Línea Universe

El equipamiento principal de la línea `Universe` tiene por defecto las siguientes propiedades:

- Rareza `EPIC`
- Irrompible
- Brillo de encantamiento
- Etiquetas adicionales de resistencia al daño

| Objeto | Implementación actual |
| --- | --- |
| `Universe Stick` | El daño escala con el nivel de experiencia del jugador y vuelve a ejecutar la loot table del objetivo según ese nivel |
| `Universe Ultima Sword` | Cambio de modo: ignición / ejecución absoluta y proyección de bolas de fuego |
| `Universe Omni Blade` | Cambio de modo: terraformación en área / destrucción en cadena de hasta `512` bloques |
| `Universe Console` | Vincula bloques funcionales y los abre a distancia entre chunks y dimensiones |
| `Universe Bucket` | Puede beberse para limpiar efectos y cambiar entre modos de agua infinita y lava infinita |
| `Universe Star` | Restaura vida al usarla y también funciona como combustible de duración extrema |
| `Universe Guardian Spawn Egg` | Invoca y domestica a `Universe Guardian` |

#### Universe Console

Comportamiento actual:

- En `Modo 1` puede vincular cualquier bloque que tenga menú
- La información vinculada se escribe en componentes del objeto, incluyendo `coordenadas + dimensión + nombre mostrado`
- En `Modo 0` la interfaz puede abrir el bloque objetivo a distancia
- El chunk objetivo se fuerza a cargar antes de abrirlo
- Soporta acceso entre dimensiones
- Permite eliminar vínculos

El ciclo de vida del acceso remoto está gestionado por `RemoteAccessManager`.

#### Universe Ultima Sword

Actualmente tiene dos modos:

- `Modo 0`
  - Clic derecho ejecuta la lógica de ignición
  - Los ataques prenden fuego al objetivo y causan daño extremadamente alto
- `Modo 1`
  - Clic derecho lanza `UniverseFireball`
  - Los ataques activan relámpagos y lógica de ejecución forzada
  - `Universe Guardian` solo puede recibir daño real en este modo

#### Universe Omni Blade

Actualmente tiene dos modos:

- `Modo 0`
  - Combina lógica de azada y de nivelado de superficie
  - Puede convertir superficies de `Ever-Water` en tierra de cultivo exclusiva
- `Modo 1`
  - Ejecuta BFS tomando como origen el bloque inicial
  - Puede destruir en cadena hasta `512` bloques del mismo tipo

#### Universe Bucket

Comportamiento actual:

- Al beberlo elimina todos los efectos con lógica similar al cubo de leche
- Se cambia mediante una tecla rápida:
  - `Modo 0`: colocar agua infinita
  - `Modo 1`: colocar lava infinita
- También puede absorber fuentes de agua / lava del mundo mediante paquetes de red dedicados y sincronizar el modo seleccionado

### 3. Armadura Universe

| Armadura | Implementación actual |
| --- | --- |
| `Universe Helmet` | Fija hambre y aire, inmuniza contra `Hunger / Nausea / Darkness / Blindness` y mejora la visión bajo el agua y dentro de la lava |
| `Universe Chestplate` | Elimina fuego y congelación, limpia veneno y otorga vuelo |
| `Universe Leggings` | Aceleración al correr, mayor altura de paso, saltos más fuertes y velocidad adicional al volar |
| `Universe Boots` | Permite caminar sobre agua y lava; al agacharse, mantener agachado y pulsar dos veces hacia delante activa una embestida corta por destello |

## Recursos y progresión

### 1. Ruta actual de recursos

Según las recetas actuales y los recursos de progresión, el avance se divide aproximadamente en tres etapas:

1. Recursos y herramientas base de `Supreme`
2. Entrar a `Harvest Continent / Ore Continent` para obtener materiales núcleo más avanzados
3. Llegar al final del juego mediante gemas `Universe`, `Universe Light / Dark`, `Universe Primary Block` y `Universe Star`

Este orden se resume a partir de los recursos actuales de `recipe` y `advancement`.

### 2. Nodos de receta clave

Algunos nodos de receta / fundición especialmente importantes:

- `Fruit of All Things -> Harvest Core` (`supreme_cooking`)
- `Supreme Metal -> Ore Core` (`supreme_cooking`)
- `Rainbow Thing -> Supreme Core` (`supreme_cooking` de duración muy larga)
- `Universe Gem Red + Yellow + White -> Universe Light`
- `Universe Gem Green + Blue + Black -> Universe Dark`
- `Universe Light + Universe Dark -> Universe Primary Block`
- `Bedrock -> Universe Primary Fragment` (`universe_cooking`)
- `Universe Primary Block -> Universe Star` (`universe_cooking`)

### 3. Recetas de bloques especiales

La línea `Universe` actual ya permite fabricar:

- `minecraft:command_block`
- `minecraft:structure_block`
- `minecraft:jigsaw`
- `minecraft:barrier`

## Otras notas

### 1. Controles y teclas rápidas

Actualmente el cliente registra una única tecla rápida unificada:

- `U`: cambiar el modo de la herramienta

Se aplica a:

- `Supreme Mobile`
- `Universe Ultima Sword`
- `Universe Omni Blade`
- `Universe Console`
- `Universe Bucket`

Además:

- El movimiento especial de `Universe Boots` no usa una tecla aparte; se activa manteniendo agachado y pulsando dos veces hacia delante.

### 2. Compilación del proyecto

#### Requisitos del entorno

- JDK `25`
- Gradle Wrapper (incluido en el repositorio)
- Acceso de red a las dependencias de Fabric / Maven Central

#### Comandos habituales

Windows:

```powershell
.\gradlew.bat build
.\gradlew.bat runClient
.\gradlew.bat runServer
```

Linux / macOS:

```bash
./gradlew build
./gradlew runClient
./gradlew runServer
```

#### Formateo y organización del código fuente

`build.gradle` también define varias tareas de limpieza de código Java, y la compilación depende automáticamente de ellas:

```powershell
.\gradlew.bat formatJavaImports
.\gradlew.bat formatJavaAnnotations
.\gradlew.bat formatJavaDeclarations
.\gradlew.bat checkJavaImportGroups
```

Estas tareas:

- Estandarizan los grupos de imports
- Fusionan líneas de anotaciones
- Unifican el orden de declaración de miembros
- Añaden automáticamente cabeceras SPDX / copyright

#### CI

El repositorio incluye un flujo de GitHub Actions:

- Archivo: `.github/workflows/build.yml`
- Entorno de ejecución: `ubuntu-24.04`
- JDK: `Microsoft OpenJDK 25`
- Comando por defecto: `./gradlew build`

#### Estructura del proyecto

```text
.
|- src/main/java/roeyqian/magnatour      # Lógica compartida, registros, bloques, entidades, recetas y red
|- src/client/java/roeyqian/magnatour    # Renderizado del cliente, interfaz, teclas, mixins del cliente
|- src/main/resources
|  |- assets/magnatour                   # Modelos, texturas, idiomas, GUI
|  |- data/magnatour                     # Recetas, dimensiones, biomas, estructuras, avances, etiquetas
|  `- fabric.mod.json                    # Metadatos del mod
|- gradle/config/import-groups.json      # Reglas de formateo de grupos de imports
`- .github/workflows/build.yml           # CI
```

#### Notas

- Muchos objetos de final de juego dependen de la cooperación entre Mixin, Data Component y sincronización de red personalizada.
- Este README describe el comportamiento ya implementado en el código actual; si cambian recetas, balance o reglas de dimensión, la documentación también debería actualizarse.

## Licencia

Este proyecto se distribuye bajo la licencia GPL-v3. Consulta `LICENSE` para más detalles.
