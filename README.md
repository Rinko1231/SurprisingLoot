# Surprising Loot / 惊喜战利品

**English**  
**Surprising Loot** is a highly configurable mod that brings unexpected encounters to adventurers!  
When a player first opens a container tied to a specific loot table, there is a configurable chance to **spawn monsters** nearby, making treasure hunts far more thrilling.  
Supports both vanilla and modded loot chests, and is **compatible with [Lootr](https://www.curseforge.com/minecraft/mc-mods/lootr)**.  

You can create datapacks to define your own surprise events. An example datapack is included in  
`data/surprisingloot/loot_events/example.json`:

```json
{
  "loot_table": "minecraft:chests/jungle_temple",
  "check_range": 3,
  "chance": 0.33,
  "spawn_times": 3,
  "entity_pool": [
    {
      "weight": 2,
      "entity_type": "minecraft:zombie",
      "nbt": "{CustomName:'\"Gift\"',CustomNameVisible:1b,Health:1.0f,Attributes:[{Name:\"generic.max_health\",Base:50.0}]}"
    },
    {
      "weight": 1,
      "glowing": true,
      "entity_type": "minecraft:skeleton"
    }
  ]
}
```

**Datapack Field Explanation**:
- `loot_table` — The loot table ID this event applies to. 
- `check_range` — Search radius for valid spawn positions (in blocks).
- `chance` — Chance for **each spawn attempt** to actually spawn a mob (0.0 ~ 1.0).
- `spawn_times` — Number of spawn attempts to make when the event is triggered.
- `entity_pool` — List of possible entities to spawn, with weighted selection.  
  - `weight` — Higher values increase the probability of this entry being chosen.  
  - `entity_type` — Entity ID to spawn.  
  - `nbt` *(optional)* — NBT data string to customize the entity.  
  - `glowing` *(optional)* — If `true`, the entity will spawn with the glowing effect.

---

**中文**  
**惊喜战利品（Surprising Loot）** 是一个高度可配置的模组，可以为冒险家们带来意想不到的遭遇！  
当玩家首次打开与特定战利品表绑定的容器时，有一定几率在附近**生成怪物**，让寻宝过程更加刺激。  
支持原版和自定义战利品表，并且与 **[Lootr](https://www.curseforge.com/minecraft/mc-mods/lootr)** 完全兼容。  

你可以通过数据包自定义自己的“惊喜事件”。模组内置了一个示例数据包，位于  
`data/surprisingloot/loot_events/example.json`：

```json
{
  "loot_table": "minecraft:chests/jungle_temple",
  "check_range": 3,
  "chance": 0.33,
  "spawn_times": 3,
  "entity_pool": [
    {
      "weight": 2,
      "entity_type": "minecraft:zombie",
      "nbt": "{CustomName:'\"礼物\"',CustomNameVisible:1b,Health:1.0f,Attributes:[{Name:\"generic.max_health\",Base:50.0}]}"
    },
    {
      "weight": 1,
      "glowing": true,
      "entity_type": "minecraft:skeleton"
    }
  ]
}
```

**数据包字段说明**：
- `loot_table` — 本事件对应的战利品表 ID。
- `check_range` — 检测可生成位置的搜索半径（单位：方块）。
- `chance` — **每次生成尝试**实际生成怪物的几率（0.0 ~ 1.0）。
- `spawn_times` — 事件触发时进行的生成尝试次数。
- `entity_pool` — 可生成的实体列表，按权重随机选择。  
  - `weight` — 权重，值越高被选中的概率越大。  
  - `entity_type` — 要生成的实体 ID。  
  - `nbt` *(可选)* — 用于自定义实体的 NBT 数据字符串。  
  - `glowing` *(可选)* — 若为 `true`，实体将带有发光效果。
