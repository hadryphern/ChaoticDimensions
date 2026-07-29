package net.blue.chaoticd.content.entity;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.Cow;
import net.minecraft.world.level.Level;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager.ControllerRegistrar;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.AnimationState;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;

/** Fabric/GeckoLib restoration of a legacy cow. */
public final class LegacyCowEntity extends Cow implements LegacyAnimatedMob {
    private final LegacyMobVariant variant;
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    public LegacyCowEntity(
        EntityType<? extends Cow> type,
        Level level,
        LegacyMobVariant variant
    ) {
        super(type, level);
        this.variant = variant;
    }

    @Override
    public LegacyMobVariant variant() {
        return variant;
    }

    @Override
    public void registerControllers(ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(
            this,
            "movement",
            4,
            this::movementAnimation
        ));
    }

    private PlayState movementAnimation(AnimationState<LegacyCowEntity> state) {
        String animation = state.isMoving()
            ? variant.walkAnimation()
            : variant.idleAnimation();

        state.getController().setAnimation(
            RawAnimation.begin().thenLoop(animation)
        );

        return PlayState.CONTINUE;
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
    }
}
