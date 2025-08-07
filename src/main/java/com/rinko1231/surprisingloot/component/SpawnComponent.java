package com.rinko1231.surprisingloot.component;

import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.TagParser;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraftforge.registries.ForgeRegistries;

//modified from Custom-Raid-Reload
public class SpawnComponent {

    public static final String ENTITY_TYPE = "entity_type";
    public static final String ENTITY_NBT = "nbt";
    public static final String GLOWING = "glowing";
    private EntityType<?> entityType;
    private CompoundTag nbt = new CompoundTag();
    private boolean glowing;


    public boolean readJson(JsonObject json) {

        // 实体类型检查
        String entityStr = GsonHelper.getAsString(json, ENTITY_TYPE, "").trim();
        ResourceLocation entityId = ResourceLocation.tryParse(entityStr);
        if (entityId == null || !ForgeRegistries.ENTITY_TYPES.containsKey(entityId)) {
            throw new JsonSyntaxException("Invalid or missing entity_type: " + entityStr);
        }
        this.entityType = ForgeRegistries.ENTITY_TYPES.getValue(entityId);


        /* glowing */
        this.glowing = GsonHelper.getAsBoolean(json, GLOWING, false);

        /* nbt */
        if (json.has(ENTITY_NBT)) {
            try {
                nbt = TagParser.parseTag(GsonHelper.convertToString(json.get(ENTITY_NBT), ENTITY_NBT));
            } catch (CommandSyntaxException e) {
                throw new JsonSyntaxException("Invalid nbt tag: " + e.getMessage());
            }
        }

        return true;
    }


    public boolean glowing() {
        return this.glowing;
    }


    public CompoundTag getNBT() {
        return this.nbt;
    }


    public EntityType<?> getSpawnType() {
        return this.entityType;
    }

    public Entity createEntity(Level level, BlockPos pos) {
        if (this.entityType == null) return null;

        CompoundTag tag = this.nbt.copy();
        tag.putString("id", EntityType.getKey(this.entityType).toString());

        try {
            Entity entity = EntityType.loadEntityRecursive(tag, level, e -> {
                e.moveTo(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, level.random.nextFloat() * 360F, 0F);
                if (glowing && e instanceof LivingEntity le) {
                    le.setGlowingTag(true);
                }
                return e;
            });

            if (entity == null) {
                System.out.println("实体创建失败，loadEntityRecursive 返回 null");
                return null;
            }

            // 尝试添加实体
            if (!level.addFreshEntity(entity)) {
                System.out.println("实体重复 UUID 或添加失败！");
                return null;
            }

            return entity;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}