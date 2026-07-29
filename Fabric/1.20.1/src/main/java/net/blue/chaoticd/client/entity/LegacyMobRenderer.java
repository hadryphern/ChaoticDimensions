package net.blue.chaoticd.client.entity;

import net.blue.chaoticd.content.entity.LegacyAnimatedMob;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.world.entity.Entity;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

/** Shared renderer for every restored GeckoLib legacy creature. */
public final class LegacyMobRenderer<T extends Entity & LegacyAnimatedMob> extends GeoEntityRenderer<T> {
    public LegacyMobRenderer(EntityRendererProvider.Context context) {
        super(context, new LegacyMobModel<>());
        this.shadowRadius = 0.5F;
    }
}
