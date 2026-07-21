package net.blue.chaoticd.content;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;

/** Static per-character RGB gradients; deliberately no tick animation so tooltips remain readable. */
public final class RarityText {
    private static final int[] RAINBOW = {0xFF5555, 0xFFAA00, 0xFFFF55, 0x55FF55, 0x55FFFF, 0x5555FF, 0xAA00FF, 0xFF55FF};
    private static final long FADE_CYCLE_MILLIS = 3_600L;

    private RarityText() {
    }

    public static Component rainbow(String text) {
        return gradient(text, RAINBOW);
    }

    public static Component gradient(String text, int... colors) {
        MutableComponent result = Component.empty();
        float animation = (System.currentTimeMillis() % FADE_CYCLE_MILLIS) / (float) FADE_CYCLE_MILLIS;
        float characterStep = colors.length / (float) Math.max(text.length(), 1);
        for (int index = 0; index < text.length(); index++) {
            result.append(Component.literal(String.valueOf(text.charAt(index)))
                .setStyle(Style.EMPTY.withColor(interpolate(colors, animation * colors.length + index * characterStep))));
        }
        return result;
    }

    private static int interpolate(int[] colors, float position) {
        int firstIndex = Math.floorMod((int) Math.floor(position), colors.length);
        int secondIndex = (firstIndex + 1) % colors.length;
        float progress = position - (float) Math.floor(position);
        int first = colors[firstIndex];
        int second = colors[secondIndex];
        int red = blend((first >> 16) & 0xFF, (second >> 16) & 0xFF, progress);
        int green = blend((first >> 8) & 0xFF, (second >> 8) & 0xFF, progress);
        int blue = blend(first & 0xFF, second & 0xFF, progress);
        return (red << 16) | (green << 8) | blue;
    }

    private static int blend(int first, int second, float progress) {
        return Math.round(first + (second - first) * progress);
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
