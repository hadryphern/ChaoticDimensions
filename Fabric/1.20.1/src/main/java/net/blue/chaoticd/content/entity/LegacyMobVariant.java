package net.blue.chaoticd.content.entity;

/** Resource and animation mapping for every restored legacy creature. */
public enum LegacyMobVariant {
    DIMENSION_PIG(
        "geo/dimension_pig.geo.json",
        "textures/entity/dimension_pig_texture.png",
        "animations/dimension_pig.animation.json",
        "animation.dimension_pig.walk",
        "animation.dimension_pig.idle"
    ),
    GOLD_DIMENSION_PIG(
        "geo/gold_dimension_pig.geo.json",
        "textures/entity/gold_dimension_pig_texture.png",
        "animations/gold_dimension_pig.animation.json",
        "animation.gold_dimension_pig.walk",
        "animation.gold_dimension_pig.idle"
    ),
    APPLE_COW(
        "geo/apple_cow.geo.json",
        "textures/entity/apple_cow_texture.png",
        "animations/apple_cow.animation.json",
        "animation.apple_cow.walk",
        "animation.apple_cow.idle"
    ),
    GOLDEN_APPLE_COW(
        "geo/apple_cow.geo.json",
        "textures/entity/golden_apple_cow_texture.png",
        "animations/apple_cow.animation.json",
        "animation.apple_cow.walk",
        "animation.apple_cow.idle"
    ),
    CRYSTAL_APPLE_COW(
        "geo/crystal_apple_cow.geo.json",
        "textures/entity/crystal_apple_cow.png",
        "animations/crystal_apple_cow.animation.json",
        "animation.apple_cow.walk",
        "animation.apple_cow.idle"
    ),
    CRYSTAL_GOLDEN_APPLE(
        "geo/crystal_apple_cow.geo.json",
        "textures/entity/crystal_goldenapple_cow.png",
        "animations/crystal_apple_cow.animation.json",
        "animation.golden_apple_cow.walk",
        "animation.golden_apple_cow.idle"
    ),
    CRYSTAL_CREEPER(
        "geo/crystal_creeper.geo.json",
        "textures/entity/crystal_creeper_texture.png",
        "animations/crystal_creeper.animation.json",
        "animation.creeper_branco.walk",
        "animation.creeper_branco.idle"
    );

    private final String model;
    private final String texture;
    private final String animation;
    private final String walkAnimation;
    private final String idleAnimation;

    LegacyMobVariant(
        String model,
        String texture,
        String animation,
        String walkAnimation,
        String idleAnimation
    ) {
        this.model = model;
        this.texture = texture;
        this.animation = animation;
        this.walkAnimation = walkAnimation;
        this.idleAnimation = idleAnimation;
    }

    public String model() {
        return model;
    }

    public String texture() {
        return texture;
    }

    public String animation() {
        return animation;
    }

    public String walkAnimation() {
        return walkAnimation;
    }

    public String idleAnimation() {
        return idleAnimation;
    }
}
