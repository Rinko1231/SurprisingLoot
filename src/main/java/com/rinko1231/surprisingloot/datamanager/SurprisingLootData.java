package com.rinko1231.surprisingloot.datamanager;

import com.rinko1231.surprisingloot.component.SpawnComponent;
import com.rinko1231.surprisingloot.component.WeightedSpawnComponent;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;

import javax.annotation.Nullable;
import java.util.List;

public record SurprisingLootData(ResourceLocation lootTable,
                                 int checkRange,
                                 float chance,
                                 int spawnTimes,
                                 List<WeightedSpawnComponent> entityPool) {

    public SpawnComponent getRandomComponent(RandomSource random) {
        int total = entityPool.stream().mapToInt(WeightedSpawnComponent::weight).sum();
        int r = random.nextInt(total);
        int acc = 0;
        for (var entry : entityPool) {
            acc += entry.weight();
            if (r < acc) return entry.component();
        }
        return entityPool.get(0).component(); // fallback
    }

    @Nullable
    public BlockPos findSpawnPosition(Level level, BlockPos center, EntityType<?> type) {
        return findSpawnPositionSpawnerStyle(level, center, type, checkRange, 16, spawnTimes == 1);
    }

    @Nullable
    public BlockPos findSpawnPositionSpawnerStyle(Level level, BlockPos center, EntityType<?> type, int range, int tries, boolean preferCenter) {
        RandomSource random = level.getRandom();

        // ✅ 优先中心，仅当 spawn_times == 1 时启用
        if (preferCenter) {
            AABB centerBox = type.getAABB(center.getX() + 0.5, center.getY(), center.getZ() + 0.5);
            if (level.noCollision(centerBox)) {
                return center;
            }
        }

        for (int i = 0; i < tries; i++) {
            double x = center.getX() + 0.5 + (random.nextDouble() - random.nextDouble()) * range;
            double y = center.getY();
            double z = center.getZ() + 0.5 + (random.nextDouble() - random.nextDouble()) * range;

            AABB aabb = type.getAABB(x, y, z);

            if (level.noCollision(aabb)) {
                return BlockPos.containing(x, y, z);
            }
        }

        return null;
    }

}
