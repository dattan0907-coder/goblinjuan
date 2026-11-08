package com.traveler.client;

import com.traveler.TravelerMod;
import com.traveler.registry.ModEntities;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;

@EventBusSubscriber(modid = TravelerMod.MODID, value = Dist.CLIENT)
public class ClientEvents {
    @SubscribeEvent
    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(ModEntities.DEAD_SKELETON.get(), TravelerSkeletonRenderer::new);
        event.registerEntityRenderer(ModEntities.GOBLIN_CHAMPION.get(), GoblinChampionRenderer::new);
    }
}
