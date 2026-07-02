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