package com.rinko1231.surprisingloot.datamanager;


import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import com.rinko1231.surprisingloot.component.SpawnComponent;
import com.rinko1231.surprisingloot.component.WeightedSpawnComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;

import java.util.ArrayList;
import java.util.List;


public class SurprisingLootParser {
    public static SurprisingLootData parse(JsonObject json) {
        ResourceLocation lootTable = ResourceLocation.tryParse(GsonHelper.getAsString(json, "loot_table"));
        if (lootTable == null) throw new JsonSyntaxException("Missing loot_table");

        int checkRange = GsonHelper.getAsInt(json, "check_range", 4);
        float chance = GsonHelper.getAsFloat(json, "chance", 1.0f);
        int spawnTimes = GsonHelper.getAsInt(json, "spawn_times", 1);

        List<WeightedSpawnComponent> pool = new ArrayList<>();

        if (json.has("entity_pool")) {
            JsonArray array = GsonHelper.getAsJsonArray(json, "entity_pool");
            for (JsonElement e : array) {
                JsonObject obj = e.getAsJsonObject();
                int weight = GsonHelper.getAsInt(obj, "weight", 1);
                SpawnComponent comp = new SpawnComponent();
                comp.readJson(obj);
                pool.add(new WeightedSpawnComponent(weight, comp));
            }
        } else if (json.has("entity")) {
            // 兼容旧格式
            JsonObject obj = GsonHelper.getAsJsonObject(json, "entity");
            SpawnComponent comp = new SpawnComponent();
            comp.readJson(obj);
            pool.add(new WeightedSpawnComponent(1, comp));
        } else {
            throw new JsonSyntaxException("No entity or entity_pool found");
        }

        return new SurprisingLootData(lootTable, checkRange, chance, spawnTimes, pool);
    }
}