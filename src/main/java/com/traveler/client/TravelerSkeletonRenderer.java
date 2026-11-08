package com.traveler.client;

import com.traveler.TravelerMod;
import com.traveler.entity.TravelerSkeletonEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.DefaultedEntityGeoModel;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

/**
 * Loads (modid = traveler, asset = dead_skeleton):
 *   assets/traveler/geo/entity/dead_skeleton.geo.json
 *   assets/traveler/animations/entity/dead_skeleton.animation.json
 *   assets/traveler/textures/entity/dead_skeleton.png
 */
public class TravelerSkeletonRenderer extends GeoEntityRenderer<TravelerSkeletonEntity> {
    public TravelerSkeletonRenderer(EntityRendererProvider.Context context) {
        super(context, new DefaultedEntityGeoModel<>(
                ResourceLocation.fromNamespaceAndPath(TravelerMod.MODID, "dead_skeleton"), true));
    }
}
