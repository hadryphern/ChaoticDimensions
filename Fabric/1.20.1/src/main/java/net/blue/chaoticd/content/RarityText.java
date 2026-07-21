package net.blue.chaoticd.content;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;

/** Static per-character RGB gradients; deliberately no tick animation so tooltips remain readable. */
public final class RarityText {
    private static final int[] RAINBOW = {0xFF5555, 0xFFAA00, 0xFFFF55, 0x55FF55, 0x55FFFF, 0x5555FF, 0xAA00FF, 0xFF55FF};

    private RarityText() {
    }

    public static Component rainbow(String text) {
        return gradient(text, RAINBOW);
    }

    public static Component gradient(String text, int... colors) {
        MutableComponent result = Component.empty();
        for (int index = 0; index < text.length(); index++) {
            result.append(Component.literal(String.valueOf(text.charAt(index)))
                .setStyle(Style.EMPTY.withColor(colors[index % colors.length])));
        }
        return result;
    }

    public static Component forRank(String text, ModItemRarities.Rank rank) {
        return switch (rank) {
            case LEGENDARY -> rainbow(text);
            case FORBIDDEN -> gradient(text, 0xAAAAAA, 0xFFFFFF);
            case EXTRAVAGANT -> gradient(text, 0xFFD700, 0x55DFFF);
            case GOD -> gradient(text, 0xFFD700, 0xFFFFFF);
            case ENDGAME -> gradient(text, 0xAA00FF, 0xFF55FF, 0x5555FF, 0x111111);
            default -> Component.literal(text).setStyle(Style.EMPTY.withColor(rank.color()));
        };
    }
}
