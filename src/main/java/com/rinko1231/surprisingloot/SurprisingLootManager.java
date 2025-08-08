package com.rinko1231.surprisingloot;


import com.rinko1231.surprisingloot.datamanager.SurprisingLootData;
import net.minecraft.resources.ResourceLocation;

import java.util.*;

public class SurprisingLootManager {
    public static final SurprisingLootManager INSTANCE = new SurprisingLootManager();

    private final Map<ResourceLocation, List<SurprisingLootData>> LOOT_EVENTS = new HashMap<>();

    private SurprisingLootManager() {
    }

    public void clear() {
        LOOT_EVENTS.clear();
    }

    public void register(SurprisingLootData data) {
        LOOT_EVENTS.computeIfAbsent(data.lootTable(), k -> new ArrayList<>()).add(data);
    }

    public List<SurprisingLootData> getEvents(ResourceLocation lootTable) {
        return LOOT_EVENTS.getOrDefault(lootTable, Collections.emptyList());
    }

    public Set<ResourceLocation> getRegisteredLootTables() {
        return LOOT_EVENTS.keySet();
    }
}