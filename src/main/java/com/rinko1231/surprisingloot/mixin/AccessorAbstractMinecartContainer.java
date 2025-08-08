package com.rinko1231.surprisingloot.mixin;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.vehicle.AbstractMinecartContainer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(AbstractMinecartContainer.class)
public interface AccessorAbstractMinecartContainer {
    @Accessor("lootTable")
    ResourceLocation surprisingloot$getLootTable();
}
