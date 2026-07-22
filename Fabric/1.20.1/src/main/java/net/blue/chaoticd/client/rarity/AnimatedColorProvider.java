package net.blue.chaoticd.client.rarity;

import java.util.Objects;
import java.util.function.LongSupplier;
import net.blue.chaoticd.rarity.RarityDefinition;

/**
 * Stateless color sampling over an injectable monotonic nanosecond clock.
 *
 * <p>No frame, tick, tooltip-open or rendered-item counter participates in the phase, so the same
 * definition runs at the same speed at every FPS and does not restart when the cursor moves.</p>
 */
public final class AnimatedColorProvider {
    private static final AnimatedColorProvider GLOBAL = new AnimatedColorProvider(System::nanoTime);

    private final LongSupplier nanoTimeSource;

    public AnimatedColorProvider(LongSupplier nanoTimeSource) {
        this.nanoTimeSource = Objects.requireNonNull(nanoTimeSource, "nanoTimeSource");
    }

    public static AnimatedColorProvider global() {
        return GLOBAL;
    }

    public long nowNanos() {
        return nanoTimeSource.getAsLong();
    }

    public int currentColor(RarityDefinition definition) {
        return colorAt(definition, nowNanos(), 0, 1);
    }

    /** Samples one Unicode code-point position; index/count are ignored by uniform definitions. */
    public int colorAt(RarityDefinition definition, long nowNanos, int codePointIndex, int codePointCount) {
        Objects.requireNonNull(definition, "definition");
        if (definition.animationMode() == RarityDefinition.AnimationMode.STATIC) {
            return definition.staticColor();
        }

        double phase = cyclePhase(nowNanos, definition.cycleDurationNanos());
        if (definition.style().gradientByCodePoint() && codePointCount > 0) {
            phase = wrap(phase + codePointIndex / (double) codePointCount);
        }

        return switch (definition.animationMode()) {
            case STATIC -> definition.staticColor();
            case PALETTE -> paletteColor(definition, phase);
            case RAINBOW_HSV -> hsvToRgb(phase, definition.saturation(), definition.brightness());
        };
    }

    public static double cyclePhase(long nowNanos, long cycleDurationNanos) {
        if (cycleDurationNanos <= 0L) return 0.0D;
        return Math.floorMod(nowNanos, cycleDurationNanos) / (double) cycleDurationNanos;
    }

    private static int paletteColor(RarityDefinition definition, double phase) {
        double position = phase * definition.paletteSize();
        int firstIndex = (int) Math.floor(position);
        int secondIndex = firstIndex + 1;
        double localProgress = position - Math.floor(position);
        double eased = definition.easing().apply(localProgress);
        return interpolateRgb(definition.paletteColor(firstIndex), definition.paletteColor(secondIndex), eased);
    }

    public static int interpolateRgb(int first, int second, double progress) {
        double clamped = Math.max(0.0D, Math.min(1.0D, progress));
        int red = blend(first >> 16 & 0xFF, second >> 16 & 0xFF, clamped);
        int green = blend(first >> 8 & 0xFF, second >> 8 & 0xFF, clamped);
        int blue = blend(first & 0xFF, second & 0xFF, clamped);
        return red << 16 | green << 8 | blue;
    }

    /** Continuous HSV hue rotation; saturation/brightness remain stable through the full cycle. */
    public static int hsvToRgb(double hue, float saturation, float brightness) {
        double wrappedHue = wrap(hue);
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

    private static int blend(int first, int second, double progress) {
        return (int) Math.round(first + (second - first) * progress);
    }

    private static int channel(double value) {
        return (int) Math.round(Math.max(0.0D, Math.min(1.0D, value)) * 255.0D);
    }

    private static double wrap(double value) {
        return value - Math.floor(value);
    }
}
