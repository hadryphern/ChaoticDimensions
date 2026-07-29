package net.blue.chaoticd.client.entity;

import net.blue.chaoticd.ChaoticDimensions;
import net.blue.chaoticd.content.entity.LegacyAnimatedMob;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import software.bernie.geckolib.model.GeoModel;

/** Reads the original GeckoLib geometry, texture and animation resources. */
public final class LegacyMobModel<T extends Entity & LegacyAnimatedMob> extends GeoModel<T> {
    @Override
    public ResourceLocation getModelResource(T animatable) {
        return new ResourceLocation(ChaoticDimensions.MOD_ID, animatable.variant().model());
    }

    @Override
    public ResourceLocation getTextureResource(T animatable) {
        return new ResourceLocation(ChaoticDimensions.MOD_ID, animatable.variant().texture());
    }

    @Override
    public ResourceLocation getAnimationResource(T animatable) {
        return new ResourceLocation(ChaoticDimensions.MOD_ID, animatable.variant().animation());
    }
}
