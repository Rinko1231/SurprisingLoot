package com.rinko1231.surprisingloot.config;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.config.ModConfig;

public class SurprisingLootConfig {

    public static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();
    public static ForgeConfigSpec SPEC;


    public static ForgeConfigSpec.IntValue timeStampIntervalMs;
    public static ForgeConfigSpec.DoubleValue maxBlockPosDist;
    public static ForgeConfigSpec.BooleanValue debugLogs;


    static {


        BUILDER.push("Surprising Loot Config");


        timeStampIntervalMs = BUILDER
                .comment("Maximum allowed time (ms) between right-click and container open before spawn trigger is discarded.")
                .defineInRange("timeStampIntervalMs", 500, 1, Integer.MAX_VALUE);

        maxBlockPosDist = BUILDER
                .comment("Maximum allowed Manhattan distance between player position on right-click and container open.")
                .defineInRange("maxBlockPosDist", 1.0, 0.0, Integer.MAX_VALUE);

        debugLogs = BUILDER
                .comment("Enable debug logs for spawn trigger rejection reasons.")
                .define("debugLogs", true);

        BUILDER.pop();

        SPEC = BUILDER.build();
    }
    public static void setup() {
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, SPEC, "SurprisingLootConfig.toml");
    }



}