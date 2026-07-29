package net.blue.chaoticd.client.visual;

import net.minecraft.client.renderer.DimensionSpecialEffects;
import net.minecraft.world.phys.Vec3;

/**
 * Shadow-specific visual response.
 *
 * <p>The dimension has no normal sky or clouds and is always treated as foggy,
 * creating the short, oppressive view distance requested for the biome.</p>
 */
public final class ShadowDimensionEffects extends DimensionSpecialEffects {
    public ShadowDimensionEffects() {
        super(Float.NaN, true, SkyType.NONE, false, false);
    }

    @Override
    public Vec3 getBrightnessDependentFogColor(Vec3 fogColor, float brightness) {
        double factor = 0.10D + brightness * 0.16D;
        return fogColor.multiply(factor, factor * 0.78D, factor * 1.08D);
    }

    @Override
    public boolean isFoggyAt(int x, int z) {
        return true;
    }
}
