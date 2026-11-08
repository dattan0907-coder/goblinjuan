package com.traveler.client;

import com.traveler.TravelerMod;
import com.traveler.entity.GoblinChampionEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.DefaultedEntityGeoModel;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

/**
 * Loads (modid = traveler, asset = goblin_champion):
 *   assets/traveler/geo/entity/goblin_champion.geo.json
 *   assets/traveler/animations/entity/goblin_champion.animation.json
 *   assets/traveler/textures/entity/goblin_champion.png
 */
public class GoblinChampionRenderer extends GeoEntityRenderer<GoblinChampionEntity> {
    public GoblinChampionRenderer(EntityRendererProvider.Context context) {
        super(context, new DefaultedEntityGeoModel<>(
                ResourceLocation.fromNamespaceAndPath(TravelerMod.MODID, "goblin_champion"), true));
    }
}
