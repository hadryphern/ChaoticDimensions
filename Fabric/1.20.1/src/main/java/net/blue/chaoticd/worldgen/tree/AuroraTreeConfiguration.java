package net.blue.chaoticd.worldgen.tree;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;

/**
 * Data-driven parameters for the procedural Aurora tree generator.
 *
 * <p>The generator intentionally uses weighted profiles rather than separately registered tree
 * features. This keeps the block palette, safety limits and shape tuning in one configured
 * feature while still allowing very different silhouettes.</p>
 */
public record AuroraTreeConfiguration(
    BlockState trunkState,
    List<BlockState> groundStates,
    List<FoliageChoice> foliagePalette,
    List<TreeProfile> profiles,
    int maxHorizontalReach
) implements FeatureConfiguration {
    public static final Codec<AuroraTreeConfiguration> CODEC = RecordCodecBuilder.create(instance -> instance.group(
        BlockState.CODEC.fieldOf("trunk_state").forGetter(AuroraTreeConfiguration::trunkState),
        BlockState.CODEC.listOf().fieldOf("ground_states").forGetter(AuroraTreeConfiguration::groundStates),
        FoliageChoice.CODEC.listOf().fieldOf("foliage_palette").forGetter(AuroraTreeConfiguration::foliagePalette),
        TreeProfile.CODEC.listOf().fieldOf("profiles").forGetter(AuroraTreeConfiguration::profiles),
        Codec.intRange(4, 12).optionalFieldOf("max_horizontal_reach", 8)
            .forGetter(AuroraTreeConfiguration::maxHorizontalReach)
    ).apply(instance, AuroraTreeConfiguration::new));

    public AuroraTreeConfiguration {
        groundStates = List.copyOf(groundStates);
        foliagePalette = List.copyOf(foliagePalette);
        profiles = List.copyOf(profiles);
        require(!groundStates.isEmpty(), "ground_states cannot be empty");
        require(!foliagePalette.isEmpty(), "foliage_palette cannot be empty");
        require(!profiles.isEmpty(), "profiles cannot be empty");
        require(foliagePalette.stream().mapToInt(FoliageChoice::weight).sum() > 0,
            "foliage_palette must have a positive total weight");
        require(profiles.stream().mapToInt(TreeProfile::weight).sum() > 0,
            "profiles must have a positive total weight");
    }

    public record FoliageChoice(BlockState state, int weight) {
        public static final Codec<FoliageChoice> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            BlockState.CODEC.fieldOf("state").forGetter(FoliageChoice::state),
            Codec.intRange(1, 1000).fieldOf("weight").forGetter(FoliageChoice::weight)
        ).apply(instance, FoliageChoice::new));
    }

    public record TreeProfile(
        String name,
        int weight,
        SizeSettings size,
        ShapeSettings shape,
        CanopySettings canopy
    ) {
        public static final Codec<TreeProfile> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.STRING.fieldOf("name").forGetter(TreeProfile::name),
            Codec.intRange(1, 10000).fieldOf("weight").forGetter(TreeProfile::weight),
            SizeSettings.CODEC.fieldOf("size").forGetter(TreeProfile::size),
            ShapeSettings.CODEC.fieldOf("shape").forGetter(TreeProfile::shape),
            CanopySettings.CODEC.fieldOf("canopy").forGetter(TreeProfile::canopy)
        ).apply(instance, TreeProfile::new));
    }

    public record SizeSettings(
        int minHeight,
        int maxHeight,
        int minTrunkWidth,
        int maxTrunkWidth,
        int minSegments,
        int maxSegments
    ) {
        public static final Codec<SizeSettings> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.intRange(3, 40).fieldOf("min_height").forGetter(SizeSettings::minHeight),
            Codec.intRange(3, 40).fieldOf("max_height").forGetter(SizeSettings::maxHeight),
            Codec.intRange(1, 3).fieldOf("min_trunk_width").forGetter(SizeSettings::minTrunkWidth),
            Codec.intRange(1, 3).fieldOf("max_trunk_width").forGetter(SizeSettings::maxTrunkWidth),
            Codec.intRange(1, 16).fieldOf("min_segments").forGetter(SizeSettings::minSegments),
            Codec.intRange(1, 16).fieldOf("max_segments").forGetter(SizeSettings::maxSegments)
        ).apply(instance, SizeSettings::new));

        public SizeSettings {
            require(minHeight <= maxHeight, "min_height cannot exceed max_height");
            require(minTrunkWidth <= maxTrunkWidth, "min_trunk_width cannot exceed max_trunk_width");
            require(minSegments <= maxSegments, "min_segments cannot exceed max_segments");
        }
    }

    public record ShapeSettings(
        float inclinationChance,
        float bendChance,
        float bendStrength,
        int minBranches,
        int maxBranches,
        float branchStartFraction,
        int minBranchLength,
        int maxBranchLength,
        int maxBranchWidth,
        float secondaryBranchChance,
        float rootChance,
        int maxRootLength
    ) {
        public static final Codec<ShapeSettings> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.floatRange(0.0F, 1.0F).fieldOf("inclination_chance").forGetter(ShapeSettings::inclinationChance),
            Codec.floatRange(0.0F, 1.0F).fieldOf("bend_chance").forGetter(ShapeSettings::bendChance),
            Codec.floatRange(0.0F, 0.75F).fieldOf("bend_strength").forGetter(ShapeSettings::bendStrength),
            Codec.intRange(0, 32).fieldOf("min_branches").forGetter(ShapeSettings::minBranches),
            Codec.intRange(0, 32).fieldOf("max_branches").forGetter(ShapeSettings::maxBranches),
            Codec.floatRange(0.1F, 0.9F).fieldOf("branch_start_fraction").forGetter(ShapeSettings::branchStartFraction),
            Codec.intRange(1, 12).fieldOf("min_branch_length").forGetter(ShapeSettings::minBranchLength),
            Codec.intRange(1, 12).fieldOf("max_branch_length").forGetter(ShapeSettings::maxBranchLength),
            Codec.intRange(1, 2).fieldOf("max_branch_width").forGetter(ShapeSettings::maxBranchWidth),
            Codec.floatRange(0.0F, 1.0F).fieldOf("secondary_branch_chance")
                .forGetter(ShapeSettings::secondaryBranchChance),
            Codec.floatRange(0.0F, 1.0F).fieldOf("root_chance").forGetter(ShapeSettings::rootChance),
            Codec.intRange(0, 4).fieldOf("max_root_length").forGetter(ShapeSettings::maxRootLength)
        ).apply(instance, ShapeSettings::new));

        public ShapeSettings {
            require(minBranches <= maxBranches, "min_branches cannot exceed max_branches");
            require(minBranchLength <= maxBranchLength,
                "min_branch_length cannot exceed max_branch_length");
        }
    }

    public record CanopySettings(
        int minRadius,
        int maxRadius,
        int minBlobs,
        int maxBlobs,
        float density,
        float verticalScale,
        int minTipRadius,
        int maxTipRadius
    ) {
        public static final Codec<CanopySettings> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.intRange(1, 8).fieldOf("min_radius").forGetter(CanopySettings::minRadius),
            Codec.intRange(1, 8).fieldOf("max_radius").forGetter(CanopySettings::maxRadius),
            Codec.intRange(1, 24).fieldOf("min_blobs").forGetter(CanopySettings::minBlobs),
            Codec.intRange(1, 24).fieldOf("max_blobs").forGetter(CanopySettings::maxBlobs),
            Codec.floatRange(0.35F, 0.95F).fieldOf("density").forGetter(CanopySettings::density),
            Codec.floatRange(0.5F, 1.6F).fieldOf("vertical_scale").forGetter(CanopySettings::verticalScale),
            Codec.intRange(1, 4).fieldOf("min_tip_radius").forGetter(CanopySettings::minTipRadius),
            Codec.intRange(1, 4).fieldOf("max_tip_radius").forGetter(CanopySettings::maxTipRadius)
        ).apply(instance, CanopySettings::new));

        public CanopySettings {
            require(minRadius <= maxRadius, "min_radius cannot exceed max_radius");
            require(minBlobs <= maxBlobs, "min_blobs cannot exceed max_blobs");
            require(minTipRadius <= maxTipRadius, "min_tip_radius cannot exceed max_tip_radius");
        }
    }

    private static void require(boolean condition, String message) {
        if (!condition) throw new IllegalArgumentException(message);
    }
}
