package net.blue.chaoticd.content;

import net.blue.chaoticd.client.rarity.TooltipRarityRenderer;
import net.fabricmc.api.EnvType;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.network.chat.Component;

/**
 * Backwards-compatible text facade. Animation is delegated only in a physical client environment.
 *
 * @deprecated New client code should use {@link TooltipRarityRenderer}; rarity resolution belongs
 * in {@link net.blue.chaoticd.rarity.RarityResolver}.
 */
@Deprecated(forRemoval = false)
public final class RarityText {
    private static final int[] RAINBOW = {
        0xFF5555, 0xFFAA00, 0xFFFF55, 0x55FF55, 0x55FFFF, 0x5555FF, 0xAA00FF, 0xFF55FF
    };

    private RarityText() {
    }

    public static Component rainbow(String text) {
        return gradient(text, RAINBOW);
    }

    public static Component gradient(String text, int... colors) {
        Component source = Component.literal(text);
        if (colors == null || colors.length == 0) return source;
        if (!isPhysicalClient()) return TooltipRarityRenderer.copyWithColor(source, colors[0]);
        return TooltipRarityRenderer.global().customGradient(source, colors);
    }

    public static Component forRank(String text, ModItemRarities.Rank rank) {
        Component source = Component.literal(text);
        if (!isPhysicalClient()) {
            return TooltipRarityRenderer.copyWithColor(source, rank.color());
        }
        return TooltipRarityRenderer.global().style(source, rank.definition());
    }

    private static boolean isPhysicalClient() {
        return FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT;
    }
}
