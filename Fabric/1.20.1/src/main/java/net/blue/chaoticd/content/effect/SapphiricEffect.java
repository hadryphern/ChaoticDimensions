package net.blue.chaoticd.content.effect;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;

/** Makes AI mobs continually abandon their course for a random nearby destination. */
public final class SapphiricEffect extends MobEffect {
    private static final double WANDER_DISTANCE = 18.0D;

    public SapphiricEffect() {
        super(MobEffectCategory.HARMFUL, 0x7B2CBF);
    }

    @Override
    public boolean isDurationEffectTick(int duration, int amplifier) {
        return duration % 10 == 0;
    }

    @Override
    public void applyEffectTick(LivingEntity entity, int amplifier) {
        if (entity.level().isClientSide || !(entity instanceof Mob mob)) {
            return;
        }

        double targetX = mob.getX() + (mob.getRandom().nextDouble() - 0.5D) * WANDER_DISTANCE * 2.0D;
        double targetZ = mob.getZ() + (mob.getRandom().nextDouble() - 0.5D) * WANDER_DISTANCE * 2.0D;
        mob.getNavigation().moveTo(targetX, mob.getY(), targetZ, 1.25D + amplifier * 0.15D);
    }
}
