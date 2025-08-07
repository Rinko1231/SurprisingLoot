package com.rinko1231.surprisingloot.datamanager;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.rinko1231.surprisingloot.SurprisingLootManager;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;

import java.util.Map;

public class SurprisingLootReloadListener extends SimpleJsonResourceReloadListener {
    public static final Gson GSON = new GsonBuilder().create();

    public SurprisingLootReloadListener() {
        super(GSON, "loot_events");
    }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> object, ResourceManager manager, ProfilerFiller profiler) {
        System.out.println("[SurprisingLoot] Starting apply with " + object.size() + " elements");
        SurprisingLootManager.INSTANCE.clear();
        object.forEach((id, jsonElement) -> {
            try {
                JsonObject json = jsonElement.getAsJsonObject();
                SurprisingLootData data = SurprisingLootParser.parse(json);
                SurprisingLootManager.INSTANCE.register(data);
            } catch (Exception e) {
                // 记录加载失败的内容
            }
        });
    }
}
