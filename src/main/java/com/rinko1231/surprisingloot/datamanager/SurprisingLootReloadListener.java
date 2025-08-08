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
import org.jetbrains.annotations.NotNull;

import java.util.Map;

public class SurprisingLootReloadListener extends SimpleJsonResourceReloadListener {
    public static final Gson GSON = new GsonBuilder().create();
    public static final SurprisingLootReloadListener INSTANCE =
            new SurprisingLootReloadListener();
    public SurprisingLootReloadListener() {
        super(GSON, "loot_events");
    }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> object,
                         @NotNull ResourceManager manager, @NotNull ProfilerFiller profiler) {
        SurprisingLootManager.INSTANCE.clear();
        object.forEach((id, jsonElement) -> {
            try {
                JsonObject json = jsonElement.getAsJsonObject();
                SurprisingLootData data = SurprisingLootParser.parse(json);
                SurprisingLootManager.INSTANCE.register(data);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }
}
