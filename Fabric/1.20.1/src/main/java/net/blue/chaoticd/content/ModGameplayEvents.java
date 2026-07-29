package net.blue.chaoticd.content;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import net.blue.chaoticd.gameplay.DimensionSoulDrops;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.minecraft.core.NonNullList;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.EnchantedBookItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.TieredItem;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.EnchantmentInstance;

/** Server-authoritative behavior for the two custom enchantments. */
public final class ModGameplayEvents {
    private static final double MAX_SAPPHIRIC_MELEE_DISTANCE_SQUARED = 16.0D;
    private static final Set<UUID> SURVIVED_DAMAGE_THIS_LIFE = new HashSet<>();

    private ModGameplayEvents() {
    }

    public static void initialize() {
        ServerLivingEntityEvents.AFTER_DEATH.register(ModGameplayEvents::onDeath);
    }

    /**
     * Returns the complete server-side damage amount for a direct sword hit.
     *
     * <p>This is deliberately applied by the LivingEntity mixin before vanilla
     * damage handling instead of recursively calling {@code LivingEntity.hurt}.
     * A recursive hit makes the outer player attack look cancelled, which skips
     * weapon durability, knockback, sweeping and the Sapphire Sword area wave.</p>
     */
    public static float scaleDirectSwordDamage(DamageSource source, float amount) {
        if (amount <= 0.0F || !Float.isFinite(amount)) {
            return amount;
        }
        float multiplier = swordDamageMultiplier(source);
        if (multiplier <= 1.0F) {
            return amount;
        }

        float scaled = amount * multiplier;
        return Float.isFinite(scaled) ? scaled : Float.MAX_VALUE;
    }

    /** Called after vanilla has confirmed and applied a non-zero damage hit. */
    public static void onDamageApplied(LivingEntity victim, DamageSource source, float damageTaken) {
        if (damageTaken <= 0.0F || victim.level().isClientSide) {
            return;
        }

        applySwordEnchantments(victim, source);

        if (victim instanceof Player player && player.isAlive()) {
            SURVIVED_DAMAGE_THIS_LIFE.add(player.getUUID());
            repairDheathicTools(player, damageTaken);
        }
    }

    private static float swordDamageMultiplier(DamageSource source) {
        if (!(source.getEntity() instanceof LivingEntity attacker)
            || !isDirectSwordAttack(source, attacker)) {
            return 1.0F;
        }

        return ModCombatEnchantments.damageMultiplier(attacker.getMainHandItem());
    }

    private static void applySwordEnchantments(LivingEntity victim, DamageSource source) {
        if (!(source.getEntity() instanceof LivingEntity attacker)
            || !isDirectSwordAttack(source, attacker)) {
            return;
        }

        ItemStack sword = attacker.getMainHandItem();
        int sapphiric = EnchantmentHelper.getItemEnchantmentLevel(ModEnchantments.SAPPHIRIC, sword);
        if (sapphiric > 0
            && attacker.distanceToSqr(victim) <= MAX_SAPPHIRIC_MELEE_DISTANCE_SQUARED) {
            applySapphiricArea(victim, attacker, sapphiric);
        }
    }

    /**
     * Royal and Sapphiric are sword enchantments, not generic modifiers for
     * every direct DamageSource emitted by the entity.  In particular this
     * excludes Thorns and modded direct damage while a sword happens to be
     * held in the main hand.
     */
    private static boolean isDirectSwordAttack(DamageSource source, LivingEntity attacker) {
        if (source.getDirectEntity() != attacker
            || !(attacker.getMainHandItem().getItem() instanceof SwordItem)) {
            return false;
        }

        return source.is(DamageTypes.PLAYER_ATTACK)
            || source.is(DamageTypes.MOB_ATTACK)
            || source.is(DamageTypes.MOB_ATTACK_NO_AGGRO);
    }

    private static void applySapphiricArea(LivingEntity victim, LivingEntity attacker, int level) {
        victim.addEffect(new MobEffectInstance(ModEffects.SAPPHIRIC, 20 * 45), attacker);
        double radius = ModCombatEnchantments.sapphiricEffectRadius(level);
        if (radius <= 0.0D) {
            return;
        }
        for (LivingEntity nearby : attacker.level().getEntitiesOfClass(LivingEntity.class,
            victim.getBoundingBox().inflate(radius), candidate -> candidate != attacker && candidate.isAlive())) {
            nearby.addEffect(new MobEffectInstance(ModEffects.SAPPHIRIC, 20 * 45), attacker);
        }
    }

    private static void repairDheathicTools(Player player, float damageTaken) {
        repairDheathicStacks(player.getInventory().items, damageTaken);
        repairDheathicStacks(player.getInventory().armor, damageTaken);
        repairDheathicStacks(player.getInventory().offhand, damageTaken);
    }

    private static void repairDheathicStacks(NonNullList<ItemStack> stacks, float damageTaken) {
        for (ItemStack stack : stacks) {
            if (!(stack.getItem() instanceof TieredItem) || stack.getDamageValue() <= 0
                || EnchantmentHelper.getItemEnchantmentLevel(ModEnchantments.DHEATHIC, stack) <= 0) {
                continue;
            }
            int repair = Math.max(1, (int) Math.ceil(stack.getDamageValue() * 0.30D)
                + (int) Math.ceil(damageTaken * 8.0D));
            stack.setDamageValue(Math.max(0, stack.getDamageValue() - repair));
        }
    }

    private static void onDeath(LivingEntity entity, DamageSource source) {
        DimensionSoulDrops.onLivingEntityDeath(entity);

        if (entity.getType() == EntityType.ENDER_DRAGON) {
            entity.spawnAtLocation(EnchantedBookItem.createForEnchantment(
                new EnchantmentInstance(ModEnchantments.DHEATHIC, 1)));
        }

        if (!(entity instanceof Player player)) {
            return;
        }
        boolean instantProjectileOrFall = source.getDirectEntity() instanceof Projectile || source.is(DamageTypes.FALL);
        if (instantProjectileOrFall && !SURVIVED_DAMAGE_THIS_LIFE.contains(player.getUUID())) {
            removeDheathicTools(player.getInventory().items);
            removeDheathicTools(player.getInventory().armor);
            removeDheathicTools(player.getInventory().offhand);
        }
        SURVIVED_DAMAGE_THIS_LIFE.remove(player.getUUID());
    }

    private static void removeDheathicTools(NonNullList<ItemStack> stacks) {
        for (int index = 0; index < stacks.size(); index++) {
            ItemStack stack = stacks.get(index);
            if (stack.getItem() instanceof TieredItem
                && EnchantmentHelper.getItemEnchantmentLevel(ModEnchantments.DHEATHIC, stack) > 0) {
                stacks.set(index, ItemStack.EMPTY);
            }
        }
    }
}
