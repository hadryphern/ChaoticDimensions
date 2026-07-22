package net.blue.chaoticd.validation;

import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import net.blue.chaoticd.client.rarity.AnimatedColorProvider;
import net.blue.chaoticd.client.rarity.TooltipRarityRenderer;
import net.blue.chaoticd.rarity.ModRarities;
import net.blue.chaoticd.rarity.RarityDefinition;
import net.blue.chaoticd.rarity.RarityRegistry;
import net.blue.chaoticd.rarity.RarityResolver;
import net.blue.chaoticd.rarity.RarityStyle;
import net.minecraft.SharedConstants;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.Bootstrap;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

/** Headless deterministic checks for rarity balance, interpolation, NBT priority and text styling. */
public final class RarityApiValidator {
    private static final long SECOND = 1_000_000_000L;

    private RarityApiValidator() {
    }

    public static void main(String[] args) {
        SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();
        validatePreservedDefinitions();
        validateSmoothPaletteAndMonotonicClock();
        validateHsvRainbow();
        validateUnicodeAndStyles();
        validateTranslatableComponentCopy();
        validateRegistryPriorityAndNbtOverride();
        System.out.println("RARITY API VALIDATION PASSED: balance, NBT priority, smooth time, HSV, Unicode and Style isolation.");
    }

    private static void validatePreservedDefinitions() {
        List<RarityDefinition> definitions = List.of(
            ModRarities.COMMON, ModRarities.UNCOMMON, ModRarities.RARE, ModRarities.VERY_RARE,
            ModRarities.EXTREMELY_RARE, ModRarities.ULTRA_RARE, ModRarities.IMPOSSIBLE,
            ModRarities.FORBIDDEN, ModRarities.LEGENDARY, ModRarities.EXTRAVAGANT,
            ModRarities.GOD, ModRarities.ENDGAME);
        int[] thresholds = {0, 4, 10, 20, 36, 60, 100, 170, 280, 430, 620, 850};
        int[] scores = {1, 2, 6, 16, 25, 40, 65, 100, 150, 210, 300, 420};
        int[] colors = {
            0x55DFFF, 0x55FF55, 0x55FFFF, 0xFF55FF, 0x5555FF, 0xAA00FF,
            0xFF5555, 0xAAAAAA, 0xFFD700, 0xFFD700, 0xFFD700, 0xAA00FF
        };
        for (int index = 0; index < definitions.size(); index++) {
            RarityDefinition definition = definitions.get(index);
            check(definition.progressionThreshold() == thresholds[index], "Changed threshold for " + definition.id());
            check(definition.enchantmentScore() == scores[index], "Changed score for " + definition.id());
            check(definition.staticColor() == colors[index], "Changed base color for " + definition.id());
        }
        check(ModRarities.FORBIDDEN.paletteSize() == 2, "Forbidden palette changed");
        check(ModRarities.ENDGAME.paletteSize() == 4, "Endgame palette changed");
        check(ModRarities.LEGENDARY.animationMode() == RarityDefinition.AnimationMode.RAINBOW_HSV,
            "Legendary must use HSV rainbow");
    }

    private static void validateSmoothPaletteAndMonotonicClock() {
        AtomicLong clock = new AtomicLong();
        AnimatedColorProvider provider = new AnimatedColorProvider(clock::get);
        RarityDefinition fade = RarityDefinition.builder(id("test_fade"), "test.fade", 0xFF0000)
            .paletteAnimation(4_000L, RarityDefinition.Easing.SMOOTHSTEP, 0xFF0000, 0xFFFFFF)
            .build();

        check(provider.currentColor(fade) == 0xFF0000, "Palette did not start at first color");
        clock.set(SECOND);
        check(provider.currentColor(fade) == 0xFF8080, "Smooth midpoint is not a clean RGB fade");
        clock.set(2L * SECOND);
        check(provider.currentColor(fade) == 0xFFFFFF, "Palette did not reach second color");
        clock.set(3L * SECOND);
        check(provider.currentColor(fade) == 0xFF8080, "Palette return path is not smooth");
        clock.set(4L * SECOND);
        check(provider.currentColor(fade) == 0xFF0000, "Palette cycle is not seamless");
        check(AnimatedColorProvider.cyclePhase(-1L, 4L * SECOND) >= 0.0D,
            "Negative monotonic origins must wrap safely");
    }

    private static void validateHsvRainbow() {
        AnimatedColorProvider provider = new AnimatedColorProvider(() -> 0L);
        RarityDefinition rainbow = RarityDefinition.builder(id("test_rainbow"), "test.rainbow", 0xFFD700)
            .rainbowAnimation(4_000L, 2.0F / 3.0F, 1.0F)
            .style(RarityStyle.PRESERVE_WITH_GRADIENT)
            .build();
        int red = provider.colorAt(rainbow, 0L, 0, 1);
        int green = provider.colorAt(rainbow, 4L * SECOND / 3L, 0, 1);
        int blue = provider.colorAt(rainbow, 8L * SECOND / 3L, 0, 1);
        check(red == 0xFF5555, "HSV rainbow lost the established Minecraft-like saturation");
        check(red != green && green != blue && blue != red, "HSV rainbow is not traversing distinct hues");
    }

    private static void validateUnicodeAndStyles() {
        RarityDefinition gradient = RarityDefinition.builder(id("test_unicode"), "test.unicode", 0xFF0000)
            .paletteAnimation(4_000L, RarityDefinition.Easing.SMOOTHSTEP, 0xFF0000, 0x0000FF)
            .style(RarityStyle.PRESERVE_WITH_GRADIENT)
            .build();
        Component original = Component.literal("A😀 B")
            .setStyle(Style.EMPTY.withBold(true).withUnderlined(true).withInsertion("preserved"));
        Component styled = new TooltipRarityRenderer(new AnimatedColorProvider(() -> 0L))
            .styleAt(original, gradient, 0L);

        check(styled.getString().equals(original.getString()), "Unicode text changed during gradient styling");
        check(styled.getString().codePointCount(0, styled.getString().length()) == 4,
            "Supplementary Unicode code point was split");
        check(!styled.getString().contains("�"), "Unicode replacement character introduced");
        List<Component> glyphs = styled.getSiblings();
        check(glyphs.size() == 4, "Gradient must emit one component per Unicode code point");
        for (Component glyph : glyphs) {
            check(glyph.getStyle().isBold(), "Bold style was lost");
            check(glyph.getStyle().isUnderlined(), "Underline style was lost");
            check("preserved".equals(glyph.getStyle().getInsertion()), "Insertion metadata was lost");
        }
        check(!glyphs.get(0).getStyle().getColor().equals(glyphs.get(1).getStyle().getColor()),
            "Per-code-point gradient did not advance");
        check(original.getStyle().getColor() == null, "Renderer mutated the source Component");
    }

    private static void validateTranslatableComponentCopy() {
        RarityDefinition uniform = RarityDefinition.builder(id("test_uniform"), "test.uniform", 0x55FFFF).build();
        Component child = Component.literal(" child").setStyle(Style.EMPTY.withItalic(true).withInsertion("child"));
        Component original = Component.translatable("rarity.chaoticd.common")
            .setStyle(Style.EMPTY.withInsertion("root")).append(child);
        Component styled = new TooltipRarityRenderer(new AnimatedColorProvider(() -> 0L))
            .styleAt(original, uniform, 0L);

        check(styled.getContents().equals(original.getContents()), "Uniform styling destroyed translatable contents");
        check(styled.getStyle().getColor().getValue() == 0x55FFFF, "Uniform rarity color missing");
        check("root".equals(styled.getStyle().getInsertion()), "Root metadata was lost");
        check(styled.getSiblings().get(0).getStyle().isItalic(), "Child formatting was lost");
        check("child".equals(styled.getSiblings().get(0).getStyle().getInsertion()), "Child metadata was lost");
        check(original.getStyle().getColor() == null, "Original translatable Component was mutated");
    }

    private static void validateRegistryPriorityAndNbtOverride() {
        RarityDefinition fallback = RarityDefinition.builder(id("fallback"), "test.fallback", 0x111111)
            .priority(0).progression(0, 1).build();
        RarityDefinition exact = RarityDefinition.builder(id("exact"), "test.exact", 0x222222)
            .priority(1).progression(10, 2).build();
        RarityDefinition override = RarityDefinition.builder(id("override"), "test.override", 0x333333)
            .priority(2).progression(20, 3).build();
        RarityRegistry registry = new RarityRegistry();
        registry.registerDefinition(fallback);
        registry.registerDefinition(exact);
        registry.registerDefinition(override);
        registry.setFallback(fallback);

        ItemStack stack = new ItemStack(Items.STICK);
        registry.registerItem(Items.STICK, exact);
        RarityResolver resolver = new RarityResolver(registry);
        check(resolver.resolveBaseItem(stack) == exact, "Exact item registration was not resolved");
        resolver.setExplicitRarity(stack, override);
        check(resolver.resolveBaseItem(stack) == override, "NBT override did not outrank exact item registration");
        check(resolver.resolveItem(stack) == override, "NBT override was not authoritative for final stack rarity");
        stack.getOrCreateTagElement(RarityResolver.NBT_ROOT).putString(RarityResolver.NBT_RARITY, "bad id");
        check(resolver.resolveBaseItem(stack) == exact, "Malformed NBT override did not fall back safely");
        resolver.clearExplicitRarity(stack);
        check(resolver.resolveBaseItem(stack) == exact, "Clearing NBT override changed item registration");
    }

    private static ResourceLocation id(String path) {
        return new ResourceLocation("chaoticd_test", path);
    }

    private static void check(boolean condition, String message) {
        if (!condition) throw new IllegalStateException(message);
    }
}
