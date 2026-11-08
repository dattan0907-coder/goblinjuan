package com.traveler;

import com.traveler.entity.GoblinChampionEntity;
import com.traveler.entity.TravelerSkeletonEntity;
import com.traveler.registry.ModEntities;
import com.traveler.registry.ModItems;
import net.minecraft.world.item.CreativeModeTabs;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;

@Mod(TravelerMod.MODID)
public class TravelerMod {
    public static final String MODID = "traveler";

    public TravelerMod(IEventBus modEventBus) {
        ModEntities.ENTITIES.register(modEventBus);
        ModItems.ITEMS.register(modEventBus);

        modEventBus.addListener(this::registerAttributes);
        modEventBus.addListener(this::buildCreativeTabs);
    }

    private void registerAttributes(EntityAttributeCreationEvent event) {
        event.put(ModEntities.DEAD_SKELETON.get(), TravelerSkeletonEntity.createAttributes().build());
        event.put(ModEntities.GOBLIN_CHAMPION.get(), GoblinChampionEntity.createAttributes().build());
    }

    private void buildCreativeTabs(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.SPAWN_EGGS) {
            event.accept(ModItems.DEAD_SKELETON_SPAWN_EGG.get());
            event.accept(ModItems.GOBLIN_CHAMPION_SPAWN_EGG.get());
        }
    }
}
