package net.blue.chaoticd.client;

import net.blue.chaoticd.content.ModItems;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.ItemStack;

/** Client-only title colors for a small, explicit set of exceptional built-in items. */
public final class SpecialItemNameStyler {
    private static final long CYCLE_NANOS = 3_600_000_000L;

    private static final int PURPLE = 0xAA00FF;
    private static final int PINK = 0xFF55FF;
    private static final int GOLD = 0xFFD700;
    private static final int WHITE = 0xFFFFFF;
    private static final int CYAN = 0x55FFFF;

    private SpecialItemNameStyler() {
    }

    /**
     * Leaves custom names and ordinary items untouched. The returned Component preserves all
     * original text, events and formatting except for the explicit title color.
     */
    public static Component styleBuiltInName(ItemStack stack, Component name) {
        if (stack.hasCustomHoverName()) return name;

        int color = colorFor(stack, System.nanoTime());
        return color < 0 ? name : recolor(name, color);
    }

    private static int colorFor(ItemStack stack, long nowNanos) {
        if (isSapphire(stack)) return hsvToRgb(phase(nowNanos), 2.0F / 3.0F, 1.0F);
        if (isRosalita(stack)) return blend(PURPLE, PINK, pingPong(phase(nowNanos)));
        if (stack.is(ModItems.DEATH_TOTEM)) return blend(GOLD, WHITE, pingPong(phase(nowNanos)));
        if (stack.is(ModItems.DREAM_FLUID_BUCKET)) return blend(GOLD, WHITE, 0.35D);
        if (stack.is(ModItems.CHAOTIC_APPLE)) return PURPLE;
        if (stack.is(ModItems.CRYSTALINE_SEE)) return CYAN;
        return -1;
    }

    private static boolean isSapphire(ItemStack stack) {
        return stack.is(ModItems.SAPPHIRE_GEM)
            || stack.is(ModItems.SAPPHIRE_SWORD)
            || stack.is(ModItems.SAPPHIRE_PICKAXE)
            || stack.is(ModItems.SAPPHIRE_AXE)
            || stack.is(ModItems.SAPPHIRE_SHOVEL)
            || stack.is(ModItems.SAPPHIRE_HOE);
    }

    private static boolean isRosalita(ItemStack stack) {
        return stack.is(ModItems.ROSALITA_GEM)
            || stack.is(ModItems.ROSALITA_SWORD)
            || stack.is(ModItems.ROSALITA_PICKAXE)
            || stack.is(ModItems.ROSALITA_AXE)
            || stack.is(ModItems.ROSALITA_SHOVEL)
            || stack.is(ModItems.ROSALITA_HOE)
            || stack.is(ModItems.ROSALITA_HELMET)
            || stack.is(ModItems.ROSALITA_CHESTPLATE)
            || stack.is(ModItems.ROSALITA_LEGGINGS)
            || stack.is(ModItems.ROSALITA_BOOTS);
    }

    private static Component recolor(Component source, int color) {
        MutableComponent result = source.plainCopy().setStyle(source.getStyle().withColor(color));
        for (Component sibling : source.getSiblings()) result.append(recolor(sibling, color));
        return result;
    }

    private static double phase(long nowNanos) {
        return Math.floorMod(nowNanos, CYCLE_NANOS) / (double) CYCLE_NANOS;
    }

    private static double pingPong(double value) {
        return value < 0.5D ? value * 2.0D : (1.0D - value) * 2.0D;
    }

    private static int blend(int first, int second, double progress) {
        double clamped = Math.max(0.0D, Math.min(1.0D, progress));
        return channel(first >> 16 & 0xFF, second >> 16 & 0xFF, clamped) << 16
            | channel(first >> 8 & 0xFF, second >> 8 & 0xFF, clamped) << 8
            | channel(first & 0xFF, second & 0xFF, clamped);
    }

    private static int hsvToRgb(double hue, float saturation, float brightness) {
        double wrappedHue = hue - Math.floor(hue);
        double chroma = brightness * saturation;
        double sector = wrappedHue * 6.0D;
        double x = chroma * (1.0D - Math.abs(sector % 2.0D - 1.0D));
        double red;
        double green;
        double blue;

        if (sector < 1.0D) {
            red = chroma; green = x; blue = 0.0D;
        } else if (sector < 2.0D) {
            red = x; green = chroma; blue = 0.0D;
        } else if (sector < 3.0D) {
            red = 0.0D; green = chroma; blue = x;
        } else if (sector < 4.0D) {
            red = 0.0D; green = x; blue = chroma;
        } else if (sector < 5.0D) {
            red = x; green = 0.0D; blue = chroma;
        } else {
            red = chroma; green = 0.0D; blue = x;
        }

        double match = brightness - chroma;
        return channel(red + match) << 16 | channel(green + match) << 8 | channel(blue + match);
    }

    private static int channel(int first, int second, double progress) {
        return (int) Math.round(first + (second - first) * progress);
    }

    private static int channel(double value) {
        return (int) Math.round(Math.max(0.0D, Math.min(1.0D, value)) * 255.0D);
    }
}
