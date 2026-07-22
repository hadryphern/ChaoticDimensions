package net.blue.chaoticd.rarity;

import java.util.Arrays;
import java.util.Objects;
import net.minecraft.resources.ResourceLocation;

/** Immutable, central source of truth for one rarity's balance, name and visual presentation. */
public final class RarityDefinition {
    public enum AnimationMode {
        STATIC,
        PALETTE,
        RAINBOW_HSV
    }

    public enum Easing {
        LINEAR {
            @Override
            public double apply(double value) {
                return value;
            }
        },
        SMOOTHSTEP {
            @Override
            public double apply(double value) {
                return value * value * (3.0D - 2.0D * value);
            }
        };

        public abstract double apply(double value);
    }

    private final ResourceLocation id;
    private final String translationKey;
    private final int priority;
    private final int progressionThreshold;
    private final int enchantmentScore;
    private final int staticColor;
    private final AnimationMode animationMode;
    private final int[] palette;
    private final long cycleDurationNanos;
    private final float saturation;
    private final float brightness;
    private final Easing easing;
    private final RarityStyle style;
    private final ResourceLocation fallbackId;

    private RarityDefinition(Builder builder) {
        id = Objects.requireNonNull(builder.id, "id");
        translationKey = Objects.requireNonNull(builder.translationKey, "translationKey");
        priority = requireNonNegative(builder.priority, "priority");
        progressionThreshold = requireNonNegative(builder.progressionThreshold, "progressionThreshold");
        enchantmentScore = requireNonNegative(builder.enchantmentScore, "enchantmentScore");
        staticColor = normalizeColor(builder.staticColor);
        animationMode = Objects.requireNonNull(builder.animationMode, "animationMode");
        palette = Arrays.stream(builder.palette).map(RarityDefinition::normalizeColor).toArray();
        cycleDurationNanos = builder.cycleDurationNanos;
        saturation = requireUnit(builder.saturation, "saturation");
        brightness = requireUnit(builder.brightness, "brightness");
        easing = Objects.requireNonNull(builder.easing, "easing");
        style = Objects.requireNonNull(builder.style, "style");
        fallbackId = builder.fallbackId;

        if (palette.length == 0) {
            throw new IllegalArgumentException("A rarity palette cannot be empty: " + id);
        }
        if (animationMode != AnimationMode.STATIC && cycleDurationNanos <= 0L) {
            throw new IllegalArgumentException("Animated rarity requires a positive cycle: " + id);
        }
        if (animationMode == AnimationMode.PALETTE && palette.length < 2) {
            throw new IllegalArgumentException("Palette animation requires at least two colors: " + id);
        }
    }

    public static Builder builder(ResourceLocation id, String translationKey, int color) {
        return new Builder(id, translationKey, color);
    }

    public ResourceLocation id() {
        return id;
    }

    public String translationKey() {
        return translationKey;
    }

    public int priority() {
        return priority;
    }

    public int progressionThreshold() {
        return progressionThreshold;
    }

    public int enchantmentScore() {
        return enchantmentScore;
    }

    public int staticColor() {
        return staticColor;
    }

    public AnimationMode animationMode() {
        return animationMode;
    }

    public int paletteSize() {
        return palette.length;
    }

    public int paletteColor(int index) {
        return palette[Math.floorMod(index, palette.length)];
    }

    /** Defensive copy for registration tools; frame rendering uses {@link #paletteColor(int)}. */
    public int[] palette() {
        return palette.clone();
    }

    public long cycleDurationNanos() {
        return cycleDurationNanos;
    }

    public float saturation() {
        return saturation;
    }

    public float brightness() {
        return brightness;
    }

    public Easing easing() {
        return easing;
    }

    public RarityStyle style() {
        return style;
    }

    public ResourceLocation fallbackId() {
        return fallbackId;
    }

    private static int requireNonNegative(int value, String name) {
        if (value < 0) throw new IllegalArgumentException(name + " cannot be negative");
        return value;
    }

    private static float requireUnit(float value, String name) {
        if (value < 0.0F || value > 1.0F || !Float.isFinite(value)) {
            throw new IllegalArgumentException(name + " must be between 0 and 1");
        }
        return value;
    }

    private static int normalizeColor(int color) {
        if ((color & 0xFF000000) != 0) {
            throw new IllegalArgumentException("Colors must be 24-bit RGB values: " + Integer.toHexString(color));
        }
        return color;
    }

    public static final class Builder {
        private final ResourceLocation id;
        private final String translationKey;
        private final int staticColor;
        private int priority;
        private int progressionThreshold;
        private int enchantmentScore;
        private AnimationMode animationMode = AnimationMode.STATIC;
        private int[] palette;
        private long cycleDurationNanos;
        private float saturation = 1.0F;
        private float brightness = 1.0F;
        private Easing easing = Easing.SMOOTHSTEP;
        private RarityStyle style = RarityStyle.PRESERVE;
        private ResourceLocation fallbackId;

        private Builder(ResourceLocation id, String translationKey, int staticColor) {
            this.id = id;
            this.translationKey = translationKey;
            this.staticColor = staticColor;
            this.palette = new int[]{staticColor};
        }

        public Builder priority(int value) {
            priority = value;
            return this;
        }

        public Builder progression(int threshold, int score) {
            progressionThreshold = threshold;
            enchantmentScore = score;
            return this;
        }

        public Builder paletteAnimation(long cycleMillis, Easing easing, int... colors) {
            animationMode = AnimationMode.PALETTE;
            cycleDurationNanos = Math.multiplyExact(cycleMillis, 1_000_000L);
            this.easing = easing;
            palette = colors.clone();
            return this;
        }

        public Builder rainbowAnimation(long cycleMillis, float saturation, float brightness) {
            animationMode = AnimationMode.RAINBOW_HSV;
            cycleDurationNanos = Math.multiplyExact(cycleMillis, 1_000_000L);
            this.saturation = saturation;
            this.brightness = brightness;
            return this;
        }

        public Builder style(RarityStyle value) {
            style = value;
            return this;
        }

        public Builder fallback(ResourceLocation value) {
            fallbackId = value;
            return this;
        }

        public RarityDefinition build() {
            return new RarityDefinition(this);
        }
    }
}
