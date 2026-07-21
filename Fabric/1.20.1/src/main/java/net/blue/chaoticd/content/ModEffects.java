package net.blue.chaoticd.content;

import net.blue.chaoticd.ChaoticDimensions;
import net.blue.chaoticd.content.effect.SapphiricEffect;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;

/** Registers effects that are shared by the server and all connected clients. */
public final class ModEffects {
    public static final MobEffect SAPPHIRIC = Registry.register(BuiltInRegistries.MOB_EFFECT,
        new ResourceLocation(ChaoticDimensions.MOD_ID, "sapphiric"), new SapphiricEffect());

    private ModEffects() {
    }

    public static void initialize() {
        // Referencing the class performs the registry registration above.
    }
}
