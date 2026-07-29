package net.blue.chaoticd.client.visual;

import net.minecraft.client.renderer.DimensionSpecialEffects;
import net.minecraft.world.phys.Vec3;

/** Aurora-specific fog response while retaining Minecraft's stable normal-sky renderer. */
public final class AuroraDimensionEffects extends DimensionSpecialEffects {
    public AuroraDimensionEffects(float cloudHeight) {
        super(cloudHeight, true, SkyType.NORMAL, false, false);
    }

    @Override
    public Vec3 getBrightnessDependentFogColor(Vec3 fogColor, float brightness) {
        double red = brightness * 0.92F + 0.08F;
        double green = brightness * 0.88F + 0.12F;
        double blue = brightness * 0.94F + 0.06F;
        return fogColor.multiply(red, green, blue);
    }

    @Override
    public boolean isFoggyAt(int x, int z) {
        return false;
    }
}
