package com.traveler.registry;

import com.traveler.TravelerMod;
import com.traveler.entity.GoblinChampionEntity;
import com.traveler.entity.TravelerSkeletonEntity;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModEntities {
    public static final DeferredRegister<EntityType<?>> ENTITIES =
            DeferredRegister.create(Registries.ENTITY_TYPE, TravelerMod.MODID);

    public static final DeferredHolder<EntityType<?>, EntityType<TravelerSkeletonEntity>> DEAD_SKELETON =
            ENTITIES.register("dead_skeleton", () ->
                    EntityType.Builder.of(TravelerSkeletonEntity::new, MobCategory.MONSTER)
                            .sized(0.6F, 1.99F)
                            .clientTrackingRange(8)
                            .build("dead_skeleton"));

    public static final DeferredHolder<EntityType<?>, EntityType<GoblinChampionEntity>> GOBLIN_CHAMPION =
            ENTITIES.register("goblin_champion", () ->
                    EntityType.Builder.of(GoblinChampionEntity::new, MobCategory.MONSTER)
                            .sized(0.6F, 1.95F)
                            .clientTrackingRange(8)
                            .build("goblin_champion"));
}
