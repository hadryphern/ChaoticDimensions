package net.blue.chaoticd.client.rarity;

import java.util.Objects;
import java.util.Optional;
import net.blue.chaoticd.ChaoticDimensions;
import net.blue.chaoticd.rarity.RarityDefinition;
import net.blue.chaoticd.rarity.RarityStyle;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;

/** Component-safe rarity styling used only from client mixins and client compatibility facades. */
public final class TooltipRarityRenderer {
    private static final TooltipRarityRenderer GLOBAL =
        new TooltipRarityRenderer(AnimatedColorProvider.global());

    private final AnimatedColorProvider colors;

    public TooltipRarityRenderer(AnimatedColorProvider colors) {
        this.colors = Objects.requireNonNull(colors, "colors");
    }

    public static TooltipRarityRenderer global() {
        return GLOBAL;
    }

    public Component style(Component source, RarityDefinition definition) {
        return styleAt(source, definition, colors.nowNanos());
    }

    public Component styleAt(Component source, RarityDefinition definition, long nowNanos) {
        Objects.requireNonNull(source, "source");
        Objects.requireNonNull(definition, "definition");
        if (!definition.style().gradientByCodePoint()) {
            int color = colors.colorAt(definition, nowNanos, 0, 1);
            return copyUniform(source, definition, color);
        }
        return copyGradient(source, definition, nowNanos);
    }

    /** Compatibility path for the old public RarityText.gradient API. */
    public Component customGradient(Component source, int... palette) {
        if (palette == null || palette.length == 0) return source.copy();
        if (palette.length == 1) return copyWithColor(source, palette[0]);
        RarityDefinition temporary = RarityDefinition.builder(
                new ResourceLocation(ChaoticDimensions.MOD_ID, "compatibility_gradient"),
                "rarity.chaoticd.common", palette[0])
            .paletteAnimation(3_600L, RarityDefinition.Easing.SMOOTHSTEP, palette)
            .style(RarityStyle.PRESERVE_WITH_GRADIENT)
            .build();
        return style(source, temporary);
    }

    public static Component copyWithColor(Component source, int color) {
        MutableComponent result = source.plainCopy().setStyle(source.getStyle().withColor(color));
        for (Component sibling : source.getSiblings()) result.append(copyWithColor(sibling, color));
        return result;
    }

    private Component copyUniform(Component source, RarityDefinition definition, int color) {
        MutableComponent result = source.plainCopy()
            .setStyle(definition.style().apply(source.getStyle(), color));
        for (Component sibling : source.getSiblings()) {
            result.append(copyUniform(sibling, definition, color));
        }
        return result;
    }

    private Component copyGradient(Component source, RarityDefinition definition, long nowNanos) {
        MutableComponent result = Component.empty();
        String renderedText = source.getString();
        int totalCodePoints = Math.max(1, renderedText.codePointCount(0, renderedText.length()));
        int[] codePointIndex = {0};
        FormattedText.StyledContentConsumer<Void> visitor = (originalStyle, text) -> {
            text.codePoints().forEachOrdered(codePoint -> {
                int color = colors.colorAt(definition, nowNanos, codePointIndex[0]++, totalCodePoints);
                Style styled = definition.style().apply(originalStyle, color);
                result.append(Component.literal(new String(Character.toChars(codePoint))).setStyle(styled));
            });
            return Optional.empty();
        };
        source.visit(visitor, Style.EMPTY);
        return result;
    }
}
