package com.rinko1231.surprisingloot;


import com.rinko1231.surprisingloot.component.SpawnComponent;
import com.rinko1231.surprisingloot.config.SurprisingLootConfig;
import com.rinko1231.surprisingloot.datamanager.SurprisingLootData;
import com.rinko1231.surprisingloot.datamanager.SurprisingLootReloadListener;
import com.rinko1231.surprisingloot.mixin.AccessorAbstractMinecartContainer;
import com.rinko1231.surprisingloot.mixin.AccessorRandomizableContainerBE;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.AbstractMinecartContainer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.event.entity.player.PlayerContainerEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static com.mojang.text2speech.Narrator.LOGGER;
import static com.rinko1231.surprisingloot.config.SurprisingLootConfig.maxBlockPosDist;
import static com.rinko1231.surprisingloot.config.SurprisingLootConfig.timeStampIntervalMs;

@Mod(SurprisingLoot.MOD_ID)
public class SurprisingLoot {
    public static final String MOD_ID = "surprisingloot";

    private static final Map<UUID, ContainerContext> PLAYER_CONTAINER_MAP = new HashMap<>();
    private static final Map<UUID, CartContext> PLAYER_CART_MAP = new HashMap<>();
    private static final String NBT_KEY_PLAYERS = "SurprisingLootTriggered";


    public SurprisingLoot() {
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::onCommonSetup);
        SurprisingLootConfig.setup();
        MinecraftForge.EVENT_BUS.register(this);
        //MinecraftForge.EVENT_BUS.addListener(this::onReload);
        MinecraftForge.EVENT_BUS.addListener(this::onRightClickBlock);
        MinecraftForge.EVENT_BUS.addListener(this::onContainerOpen);
        MinecraftForge.EVENT_BUS.addListener(this::onContainerClose);
    }

    private static ResourceLocation getLootTableSafe(RandomizableContainerBlockEntity be) {
        return ((AccessorRandomizableContainerBE) be).getLootTable();
    }

    private static ResourceLocation getLootTableSafe(AbstractMinecartContainer cart) {
        return ((AccessorAbstractMinecartContainer) cart).surprisingloot$getLootTable();
    }

    private static boolean hasTriggered(CompoundTag tag, UUID uuid) {
        if (!tag.contains(NBT_KEY_PLAYERS)) return false;
        ListTag list = tag.getList(NBT_KEY_PLAYERS, Tag.TAG_STRING);
        for (Tag t : list) if (t.getAsString().equals(uuid.toString())) return true;
        return false;
    }

    private static void markTriggered(CompoundTag tag, UUID uuid) {
        ListTag list = tag.contains(NBT_KEY_PLAYERS) ? tag.getList(NBT_KEY_PLAYERS, Tag.TAG_STRING) : new ListTag();
        list.add(StringTag.valueOf(uuid.toString()));
        tag.put(NBT_KEY_PLAYERS, list);
    }

    private void onCommonSetup(FMLCommonSetupEvent event) {
    }

    @SubscribeEvent
    public void onAddReloadListener(AddReloadListenerEvent event) {
        event.addListener(SurprisingLootReloadListener.INSTANCE);
    }

    private void triggerSpawnsForLoot(Level level,
                                      ResourceLocation lootTable,
                                      BlockPos center,
                                      Player player,
                                      @Nullable AbstractMinecartContainer cart) {
        // 读事件
        var events = SurprisingLootManager.INSTANCE.getEvents(lootTable);
        if (events.isEmpty()) return;

        // 检查去重 NBT
        CompoundTag tag;
        if (cart != null) {
            tag = cart.getPersistentData();
            if (hasTriggered(tag, player.getUUID())) return;
        } else {
            BlockEntity be = level.getBlockEntity(center.below()); // center 是上方一格
            if (!(be instanceof RandomizableContainerBlockEntity rbe)) return;
            tag = rbe.getPersistentData();
            if (hasTriggered(tag, player.getUUID())) return;
        }

        // 生成
        for (SurprisingLootData data : events) {
            for (int i = 0; i < data.spawnTimes(); i++) {
                if (level.random.nextFloat() < data.chance()) {
                    SpawnComponent comp = data.getRandomComponent(level.random);
                    BlockPos spawnPos = data.findSpawnPosition(level, center, comp.getSpawnType());
                    if (spawnPos != null) {
                        Entity entity = comp.createEntity(level, spawnPos);
                        //if (entity != null) level.addFreshEntity(entity);
                    }
                }
            }
        }

        // 标记该玩家已触发
        markTriggered(tag, player.getUUID());
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

        // 如果是箱子（或其子类），并且被阻塞（上方有方块/猫），就不记录上下文
        if (container instanceof ChestBlockEntity) {
            if (ChestBlock.isChestBlockedAt(level, pos)) {
                return;
            }
        }

        ResourceLocation lootTable = getLootTableSafe(container);

        if (lootTable == null) return;

        PLAYER_CONTAINER_MAP.put(event.getEntity().getUUID(),
                new ContainerContext(
                        level.dimension(),
                        pos,
                        lootTable,
                        System.currentTimeMillis(),
                        event.getEntity().blockPosition()
                )
        );


    }

    /**
     * 玩家打开容器菜单时真正触发刷怪逻辑
     **/
    @SubscribeEvent
    public void onContainerOpen(PlayerContainerEvent.Open event) {
        Player player = event.getEntity();
        UUID pid = player.getUUID();
        Level level = player.level();
        ResourceKey<Level> dim = level.dimension();

        // 1) 方块容器上下文
        ContainerContext blockCtx = PLAYER_CONTAINER_MAP.remove(pid);
        if (blockCtx != null && dim.equals(blockCtx.dimension())) {
            /* 为什么？
               因为不加检测的话，如果你试图打开一个上面有方块挡着的箱子，没成功
               接着用末影之戒之类的远程打开某个容器（如末影箱），
               就会在原来那个箱子那里刷怪
             */
            long deltaTimeB = System.currentTimeMillis() - blockCtx.time();
            double distB = player.blockPosition().distManhattan(blockCtx.playerPos());

            if (deltaTimeB > timeStampIntervalMs.get()) {
                if (SurprisingLootConfig.debugLogs.get()) {
                    LOGGER.info("[SurprisingLoot] Spawn trigger rejected: time={}ms (limit {})",
                            deltaTimeB, timeStampIntervalMs.get());
                }
                return;
            }
            if (distB > maxBlockPosDist.get()) {
                if (SurprisingLootConfig.debugLogs.get()) {
                    LOGGER.info("[SurprisingLoot] Spawn trigger rejected: distance={} (limit {})",
                            distB, maxBlockPosDist.get());
                }
                return;
            }

            triggerSpawnsForLoot(level, blockCtx.lootTable(), blockCtx.pos().above(), player, null);
            return;
        }

        // 2) 矿车容器上下文
        CartContext cartCtx = PLAYER_CART_MAP.remove(pid);
        if (cartCtx != null && dim.equals(cartCtx.dimension())) {
            Entity e = ((ServerLevel) level).getEntity(cartCtx.cartUUID());
            if (e instanceof AbstractMinecartContainer cart) {
                // 生成中心点取矿车当前位置
                long deltaTime = System.currentTimeMillis() - cartCtx.time();
                double dist = player.blockPosition().distManhattan(cartCtx.playerPos());

                if (deltaTime > timeStampIntervalMs.get()) {
                    if (SurprisingLootConfig.debugLogs.get()) {
                        LOGGER.info("[SurprisingLoot] Minecart spawn rejected: time={}ms (limit {})",
                                deltaTime, timeStampIntervalMs.get());
                    }
                    return;
                }
                if (dist > maxBlockPosDist.get()) {
                    if (SurprisingLootConfig.debugLogs.get()) {
                        LOGGER.info("[SurprisingLoot] Minecart spawn rejected: distance={} (limit {})",
                                dist, maxBlockPosDist.get());
                    }
                    return;
                }

                BlockPos center = BlockPos.containing(cart.position());
                triggerSpawnsForLoot(level, cartCtx.lootTable(), center.above(), player, cart);
            }
        }
    }

    public void onContainerClose(PlayerContainerEvent.Close event) {
        UUID uuid = event.getEntity().getUUID();
        PLAYER_CONTAINER_MAP.remove(uuid); // 方块容器
        PLAYER_CART_MAP.remove(uuid);      // 矿车容器
    }

    @SubscribeEvent
    public void onRightClickEntity(PlayerInteractEvent.EntityInteract event) {
        Level level = event.getLevel();
        if (level.isClientSide) return;

        Entity target = event.getTarget();
        if (!(target instanceof AbstractMinecartContainer cart)) return;

        ResourceLocation lootTable = getLootTableSafe(cart);
        if (lootTable == null) return;

        // 记录：玩家 -> 矿车上下文
        PLAYER_CART_MAP.put(event.getEntity().getUUID(),
                new CartContext(level.dimension(), cart.getUUID(), lootTable,
                        System.currentTimeMillis(),
                        event.getEntity().blockPosition()));
    }

    public record ContainerContext(ResourceKey<Level> dimension, BlockPos pos, ResourceLocation lootTable, long time,
                                   BlockPos playerPos) {
    }

    public record CartContext(ResourceKey<Level> dimension, UUID cartUUID, ResourceLocation lootTable, long time,
                              BlockPos playerPos) {
    }


}
