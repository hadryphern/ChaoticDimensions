package net.blue.chaoticd.rarity;

import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;

/**
 * Optional typography overrides owned by a rarity definition.
 *
 * <p>A {@code null} flag means "preserve the component's existing value". This is important for
 * custom names, curses, hover events and text supplied by other mods: rarity styling changes only
 * the properties it explicitly owns.</p>
 */
public record RarityStyle(
    Boolean bold,
    Boolean italic,
    Boolean underlined,
    Boolean strikethrough,
    ResourceLocation font,
    boolean gradientByCodePoint
) {
    public static final RarityStyle PRESERVE = new RarityStyle(null, null, null, null, null, false);
    public static final RarityStyle PRESERVE_WITH_GRADIENT =
        new RarityStyle(null, null, null, null, null, true);

    /** Applies the rarity-owned fields while retaining every unrelated part of the original style. */
    public Style apply(Style original, int color) {
        Style result = original.withColor(color);
        if (bold != null) result = result.withBold(bold);
        if (italic != null) result = result.withItalic(italic);
        if (underlined != null) result = result.withUnderlined(underlined);
        if (strikethrough != null) result = result.withStrikethrough(strikethrough);
        if (font != null) result = result.withFont(font);
        return result;
    }
}
