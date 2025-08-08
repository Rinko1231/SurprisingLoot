package com.rinko1231.surprisingloot;


import com.rinko1231.surprisingloot.component.SpawnComponent;
import com.rinko1231.surprisingloot.datamanager.SurprisingLootData;
import com.rinko1231.surprisingloot.datamanager.SurprisingLootReloadListener;
import com.rinko1231.surprisingloot.mixin.AccessorRandomizableContainerBE;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.event.entity.player.PlayerContainerEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Mod(SurprisingLoot.MOD_ID)
public class SurprisingLoot {
    public static final String MOD_ID = "surprisingloot";

    private static final Map<UUID, ContainerContext> PLAYER_CONTAINER_MAP = new HashMap<>();
    private static final String NBT_KEY_PLAYERS = "SurprisingLootTriggered";

    public SurprisingLoot() {
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::onCommonSetup);
        MinecraftForge.EVENT_BUS.register(this);
        //MinecraftForge.EVENT_BUS.addListener(this::onReload);
        MinecraftForge.EVENT_BUS.addListener(this::onRightClickBlock);
        MinecraftForge.EVENT_BUS.addListener(this::onContainerOpen);
        MinecraftForge.EVENT_BUS.addListener(this::onContainerClose);
    }

    private static ResourceLocation getLootTableSafe(RandomizableContainerBlockEntity be) {
        return ((AccessorRandomizableContainerBE) be).getLootTable();
    }

    private void onCommonSetup(FMLCommonSetupEvent event) {
    }

    @SubscribeEvent
    public void onAddReloadListener(AddReloadListenerEvent event) {
        event.addListener(SurprisingLootReloadListener.INSTANCE);
    }

    private boolean hasPlayerTriggered(RandomizableContainerBlockEntity be, UUID playerUUID) {
        CompoundTag tag = be.getPersistentData();
        if (tag.contains(NBT_KEY_PLAYERS)) {
            ListTag list = tag.getList(NBT_KEY_PLAYERS, Tag.TAG_STRING);
            for (Tag t : list) {
                if (t.getAsString().equals(playerUUID.toString())) {
                    return true;
                }
            }
        }
        return false;
    }

    private void markPlayerTriggered(RandomizableContainerBlockEntity be, UUID playerUUID) {
        CompoundTag tag = be.getPersistentData();
        ListTag list;
        if (tag.contains(NBT_KEY_PLAYERS)) {
            list = tag.getList(NBT_KEY_PLAYERS, Tag.TAG_STRING);
        } else {
            list = new ListTag();
            tag.put(NBT_KEY_PLAYERS, list);
        }
        list.add(StringTag.valueOf(playerUUID.toString()));
    }

    /**
     * 玩家右键容器记录坐标
     **/
    public void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        Level level = event.getLevel();
        if (level.isClientSide) return;

        BlockPos pos = event.getPos();
        BlockEntity be = level.getBlockEntity(pos);
        if (!(be instanceof RandomizableContainerBlockEntity container)) return;
        /*
        if (event.getEntity() instanceof ServerPlayer player1)
            player1.displayClientMessage(Component.literal("be"),false);
        */
        ResourceLocation lootTable = getLootTableSafe(container);
        //event.getEntity().displayClientMessage(Component.literal(lootTable.toString()),false);
        if (lootTable == null) return;

        PLAYER_CONTAINER_MAP.put(event.getEntity().getUUID(), new ContainerContext(level.dimension(), pos, lootTable));
        /*
        Component CC = Component.literal( new ContainerContext(level.dimension(), pos, lootTable).toString());
        event.getEntity().displayClientMessage(CC,false);
        */
    }

    /**
     * 玩家打开容器菜单时真正触发刷怪逻辑
     **/
    public void onContainerOpen(PlayerContainerEvent.Open event) {
        Player player = event.getEntity();
        UUID uuid = player.getUUID();

        ContainerContext ctx = PLAYER_CONTAINER_MAP.remove(uuid);
        if (ctx == null) return;

        Level level = player.level();
        if (!(level.getBlockEntity(ctx.pos()) instanceof RandomizableContainerBlockEntity be)) return;
        if (!level.dimension().equals(ctx.dimension())) return;

        // 关键：检查是否已经触发过
        if (hasPlayerTriggered(be, uuid)) return;
        markPlayerTriggered(be, uuid);

        List<SurprisingLootData> events = SurprisingLootManager.INSTANCE.getEvents(ctx.lootTable());
        if (events.isEmpty()) return;

        for (SurprisingLootData data : events) {
            for (int i = 0; i < data.spawnTimes(); i++) {
                if (level.random.nextFloat() < data.chance()) {
                    SpawnComponent comp = data.getRandomComponent(level.random);
                    BlockPos spawnPos = data.findSpawnPosition(level, ctx.pos().above(), comp.getSpawnType());
                    if (spawnPos != null) {
                        Entity entity = comp.createEntity(level, spawnPos);
                        if (entity != null) {
                            level.addFreshEntity(entity);
                        }
                    }
                }
            }
        }
    }

    public void onContainerClose(PlayerContainerEvent.Close event) {
        PLAYER_CONTAINER_MAP.remove(event.getEntity().getUUID());
    }


    public record ContainerContext(ResourceKey<Level> dimension, BlockPos pos, ResourceLocation lootTable) {
    }

}
