package net.blue.chaoticd.client;

import net.blue.chaoticd.content.ModEffects;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.Minecraft;

/** Purple screen veil for the local player while the Sapphiric effect is active. */
public final class SapphiricVisuals {
    private SapphiricVisuals() {
    }

    public static void initialize() {
        HudRenderCallback.EVENT.register((graphics, tickDelta) -> {
            var player = Minecraft.getInstance().player;
            if (player == null || !player.hasEffect(ModEffects.SAPPHIRIC)) {
                return;
            }
            int alpha = 68 + (player.tickCount / 5 % 2) * 16;
            graphics.fill(0, 0, graphics.guiWidth(), graphics.guiHeight(), (alpha << 24) | 0x61169E);
        });
    }
}
