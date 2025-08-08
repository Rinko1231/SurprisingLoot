package com.rinko1231.surprisingloot.mixin;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(RandomizableContainerBlockEntity.class)
public interface AccessorRandomizableContainerBE {
    /**
     * Mojang经典操作之
     * 没事就弄成 protected/private，
     * 然后偏偏不写 getter，
     * If you want it, then you'll have to take it.jpg
     */
    @Accessor("lootTable")
    ResourceLocation getLootTable();
}
