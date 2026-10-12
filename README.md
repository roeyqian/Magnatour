# Magnatour

> **!!!ATTENTION!!!**
> This mod is still undergoing rapid development. Anything may change. Please do not use it in your main game saves.

| [简体中文](#简体中文) | [English](#english) | [日本語](#日本語) | [Español](#español) |
|---|---|---|---|

[![License: GPL v3](https://img.shields.io/badge/License-GPL%20v3-red.svg)](LICENSE)
[![Releases](https://img.shields.io/github/release/roeyqian/Magnatour?style=flat&color=6366f1)](https://github.com/roeyqian/Magnatour/releases)
[![Stars](https://img.shields.io/github/stars/roeyqian/Magnatour?style=flat&color=ffcc66)](https://github.com/roeyqian/Magnatour/stargazers)

> **License:** [LICENSE](LICENSE)

> **Wiki:** <https://github.com/roeyqian/Magnatour/wiki>

> **Releases:** <https://github.com/roeyqian/Magnatour/releases>

> **Development repo:** <https://github.com/roeyqian/MagnatourDev>

---

## 简体中文

### 模组简介

`Magnatour` 伟大旅行是一款基于 Minecraft Fabric API 开发的冒险内容模组，旨在让玩家通过不断探索与挑战逐步成长，最终获得颠覆游戏规则的绝对统治力。

模组主要分为以下三个发展阶段：

- `强化` 阶段：玩家开始伟大旅行的征程。

- `超凡` 阶段：玩家的能力将获得超乎想象的提升，同时遇到与之相匹配的趣味冒险，本模组的夸张 数值 / 机制 将在这里逐步显现。

- `寰宇` 阶段：玩家最终成长为游戏本身都难以承载的恐怖存在，彻底掌控一切。

### 内容总览

- 物品分类
    - 当前分为 `超凡方块`、`超凡物品`、`寰宇方块`、`寰宇物品`。
- 世界生成
    - 目前包含 `丰收大陆`、`矿石大陆` 与 `寰宇元` 三个主要维度。
    - `丰收大陆` 侧重特色地貌与群系，例如小麦平原、西瓜丛林、南瓜峡谷、大湖、湖心岛。
    - `矿石大陆` 侧重矿物资源堆叠与高密度采集。
    - `寰宇元界` 是虚空中的寰宇维度：首次生成时会在原点创建一个 64×64×64 的实体立方体，主体使用死亡方块，并以重生方块加固边缘；生成状态会随存档持久保存。
- 传送门与跨维
    - `寰宇元界` 传送门采用末地传送门式的 5×5 水平结构：点亮 12 个非角落的寰宇方块后，中心会生成 3×3 传送门。
    - 该传送门可在主世界与 `寰宇元界` 间往返；生存模式需在门内停留 80 游戏刻，创造模式可立即传送。传送门会记录配对的落点，并在需要时安全生成返回门。
- 结构与探索
    - 目前代表性结构包括 `黄金钟楼` 与 `钻石城`。
- 生物与战斗
    - 当前已有头目、敌对、友好等多类生物，例如 `幽匿巨兽`、`苍白领主`、`守钟人`、`黑曜石傀儡`、`下界合金傀儡`、`寰宇守卫`。
- 玩法风格
    - 模组强调夸张成长曲线，后期会出现极端装备、远程管理、跨维传送、超高强度战斗与特殊机制。

### 进一步了解

有关方块、物品、维度、生物、配方、快捷键、构建方式和版本说明，请查看 GitHub Wiki：

- <https://github.com/roeyqian/Magnatour/wiki>

### 注意事项

- 项目依赖较多 Mixin 与自定义逻辑，兼容性问题不能完全排除。
- Wiki 以最新版本为准，旧版本内容可能与当前说明不完全一致。
- 项目基于 GPL-v3 协议开源，详细信息请查看 [LICENSE](LICENSE)。

---

## English

### Mod Introduction

`Magnatour` is an adventure content mod built on the Minecraft Fabric API. It aims to let players grow through continual exploration and challenges, ultimately gaining absolute power that overturns the rules of the game.

The mod's progression is divided into three main stages:

- `Enhance` stage: Players begin their Great Journey.

- `Supreme` stage: Players gain unimaginable boosts to their abilities and encounter fun adventures that match their newfound power. The mod's exaggerated stats and mechanics gradually emerge at this stage.

- `Universe` stage: Players ultimately become terrifying beings whose power exceeds what the game itself can handle, taking complete control over everything.

### Content Overview

- Item categories
    - The mod currently separates content into `Supreme Blocks`, `Supreme Items`, `Universe Blocks`, and `Universe Items`.
- World generation
    - The main dimensions are currently `Harvest Continent`, `Ore Continent`, and `Universe Meta`.
    - `Harvest Continent` focuses on themed terrain and biomes such as Wheat Plains, Watermelon Jungle, Pumpkin Canyon, Great Lake, and Lake Isle.
    - `Ore Continent` focuses on concentrated mineral generation and high-density resource collection.
    - `Universe Meta` is a void-based Universe dimension. On its first generation, a solid 64×64×64 cube is created at the origin, primarily from Death Blocks with edges reinforced by Rebirth Blocks; its generation state is saved with the world.
- Portals and cross-dimensional travel
    - The `Universe Meta` portal uses a 5×5 horizontal, End-Portal-style structure: light all 12 non-corner Universe Blocks to create the 3×3 portal center.
    - It supports travel between the Overworld and `Universe Meta`. Survival players must remain in the portal for 80 game ticks, while Creative players teleport instantly. Portals remember paired destinations and safely create a return portal when needed.
- Structures and exploration
    - Major structures currently include `Golden Clock Tower` and `Diamond City`.
- Creatures and combat
    - The mod already includes bosses, enemies, and friendly units such as `Sculk Behemoth`, `Pale Lord`, `Bell Keeper`, `Obsidian Golem`, `Netherite Golem`, and `Universe Guardian`.
- Gameplay style
    - The overall experience pushes exaggerated progression, late-game equipment, remote management, cross-dimensional travel, extreme combat, and special systems.

### Learn More

For full documentation about blocks, items, dimensions, creatures, recipes, controls, build steps, and version notes, see the GitHub Wiki:

- <https://github.com/roeyqian/Magnatour/wiki>

### Notes

- The project relies heavily on Mixins and custom logic, so compatibility issues may still occur.
- The wiki tracks the latest version, so older releases may differ from current documentation.
- The project is licensed under GPL-v3. See [LICENSE](LICENSE) for details.

---

## 日本語

### モッド紹介

`Magnatour` 偉大なる旅 は Minecraft Fabric API を基盤に開発された冒険コンテンツ MOD です。探索と挑戦を重ねて成長し、最終的にはゲームのルールを覆す絶対的な支配力を手に入れることを目指します。

成長は主に次の三つの段階に分かれています。

- `強化` 段階：プレイヤーは偉大なる旅へと踏み出します。

- `至高` 段階：プレイヤーの能力は想像を超えるほど向上し、その力に見合った楽しい冒険に出会います。この段階から、本 MOD の桁外れの数値やメカニクスが徐々に現れ始めます。

- `寰宇` 段階：プレイヤーは最終的にゲームそのものですら支えきれないほどの恐るべき存在となり、すべてを完全に掌握します。

### コンテンツ概要

- アイテム分類
    - 現在の主な分類は `至高のブロック`、`至高のアイテム`、`寰宇のブロック`、`寰宇のアイテム` です。
- ワールド生成
    - 主なディメンションは `豊穣大陸`、`鉱石大陸`、`寰宇元` です。
    - `豊穣大陸` には、`小麦平原`、`メロンの密林`、`カボチャ峡谷`、`大湖`、`湖心島` などの特徴的なバイオームがあります。
    - `鉱石大陸` は鉱石資源の集中生成と大量採掘に重点が置かれています。
    - `寰宇元` は虚無に存在する寰宇ディメンションです。初回生成時、原点に死のブロックを主体とし、再生のブロックで縁を補強した 64×64×64 の中実キューブが生成され、その生成状態はワールドに保存されます。
- ポータルと次元間移動
    - `寰宇元` ポータルは、エンドポータル式の 5×5 水平構造です。角以外の 12 個の `寰宇のブロック` を点灯すると、中央に 3×3 のポータルが生成されます。
    - このポータルでオーバーワールドと `寰宇元` を往復できます。サバイバルでは 80 ゲームティックの滞在が必要ですが、クリエイティブでは即座に転送されます。ポータルは接続先を記録し、必要に応じて安全な帰還用ポータルを生成します。
- 構造物と探索
    - 代表的な構造物は `黄金の時計塔` と `ダイヤモンドの都` です。
- 生物と戦闘
    - `スカルクの巨獣`、`ペイルロード`、`鐘守`、`黒曜石のゴーレム`、`ネザライトのゴーレム`、`寰宇の守護者` など、ボス、敵対、友好の各種生物が実装されています。
- ゲームプレイの方向性
    - 誇張された成長曲線、終盤装備、遠隔管理、次元間移動、極端な戦闘、特殊システムが体験の中心です。

### さらに詳しく

ブロック、アイテム、ディメンション、生物、レシピ、操作方法、ビルド手順、バージョン情報の詳細は GitHub Wiki を参照してください。

- <https://github.com/roeyqian/Magnatour/wiki>

### 注意事項

- このプロジェクトは Mixin と独自ロジックへの依存が大きく、互換性の問題が発生する可能性があります。
- Wiki は最新版基準のため、旧バージョンの内容とは一致しない場合があります。
- このプロジェクトは GPL-v3 ライセンスで公開されています。詳細は [LICENSE](LICENSE) を参照してください。

---

## Español

### Introducción del mod

`Magnatour` es un mod de contenido de aventura desarrollado sobre Minecraft Fabric API. Su objetivo es que los jugadores crezcan mediante la exploración y los desafíos constantes, hasta alcanzar un dominio absoluto capaz de cambiar las reglas del juego.

La progresión del mod se divide en tres etapas principales:

- Etapa `Enhance`: Los jugadores comienzan su Gran Viaje.

- Etapa `Supreme`: Las habilidades de los jugadores mejoran más allá de lo imaginable, y encuentran aventuras divertidas a la altura de su nuevo poder. Las estadísticas y mecánicas exageradas del mod comienzan a manifestarse gradualmente en esta etapa.

- Etapa `Universe`: Los jugadores terminan convirtiéndose en seres temibles cuyo poder supera lo que el propio juego puede soportar, y toman el control absoluto de todo.

### Resumen del contenido

- Categorías de objetos
    - El contenido principal se divide actualmente en `Supreme Blocks`, `Supreme Items`, `Universe Blocks` y `Universe Items`.
- Generación del mundo
    - Las dimensiones principales son `Harvest Continent`, `Ore Continent` y `Universe Meta`.
    - `Harvest Continent` incluye biomas característicos como Wheat Plains, Watermelon Jungle, Pumpkin Canyon, Great Lake y Lake Isle.
    - `Ore Continent` se centra en la generación concentrada de minerales y la recolección masiva de recursos.
    - `Universe Meta` es una dimensión Universe situada en el vacío. Durante su primera generación, se crea en el origen un cubo macizo de 64×64×64, compuesto principalmente de bloques de muerte y con bordes reforzados de bloques de renacimiento; su estado de generación se guarda con el mundo.
- Portales y viaje entre dimensiones
    - El portal de `Universe Meta` usa una estructura horizontal de 5×5 al estilo del portal del End: activa los 12 Universe Blocks que no están en las esquinas para crear el centro de portal de 3×3.
    - Permite viajar entre el Overworld y `Universe Meta`. En Supervivencia debes permanecer en el portal 80 ticks de juego; en Creativo el teletransporte es inmediato. Los portales recuerdan los destinos vinculados y crean de forma segura un portal de regreso cuando hace falta.
- Estructuras y exploración
    - Las estructuras más representativas incluyen `Golden Clock Tower` y `Diamond City`.
- Criaturas y combate
    - Ya existen jefes, enemigos y aliados como `Sculk Behemoth`, `Pale Lord`, `Bell Keeper`, `Obsidian Golem`, `Netherite Golem` y `Universe Guardian`.
- Estilo de juego
    - La experiencia se apoya en una progresión exagerada, equipamiento de final de juego, gestión remota, viaje entre dimensiones, combate extremo y sistemas especiales.

### Más información

Si quieres revisar bloques, objetos, dimensiones, criaturas, recetas, controles, compilación y notas de versión, consulta la GitHub Wiki:

- <https://github.com/roeyqian/Magnatour/wiki>

### Notas

- El proyecto depende bastante de Mixins y lógica personalizada, por lo que todavía pueden aparecer problemas de compatibilidad.
- La Wiki sigue la versión más reciente, así que las versiones antiguas pueden no coincidir con la documentación actual.
- El proyecto se distribuye bajo GPL-v3. Consulta [LICENSE](LICENSE) para más información.
