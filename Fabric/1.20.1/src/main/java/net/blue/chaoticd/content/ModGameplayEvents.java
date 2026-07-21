package net.blue.chaoticd.content;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
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
import net.minecraft.world.item.TieredItem;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.EnchantmentInstance;

/** Server-authoritative behavior for the two custom enchantments. */
public final class ModGameplayEvents {
    private static final Set<UUID> SURVIVED_DAMAGE_THIS_LIFE = new HashSet<>();
    private static final ThreadLocal<Boolean> APPLYING_BONUS_SWORD_DAMAGE = ThreadLocal.withInitial(() -> false);

    private ModGameplayEvents() {
    }

    public static void initialize() {
        ServerLivingEntityEvents.ALLOW_DAMAGE.register(ModGameplayEvents::onDamage);
        ServerLivingEntityEvents.AFTER_DEATH.register(ModGameplayEvents::onDeath);
    }

    private static boolean onDamage(LivingEntity victim, DamageSource source, float amount) {
        applySwordEnchantments(victim, source, amount);

        if (victim instanceof Player player && amount < player.getHealth()) {
            SURVIVED_DAMAGE_THIS_LIFE.add(player.getUUID());
            repairDheathicTools(player, amount);
        }
        return true;
    }

    private static void applySwordEnchantments(LivingEntity victim, DamageSource source, float amount) {
        if (!(source.getEntity() instanceof LivingEntity attacker)
            || source.getDirectEntity() != attacker) {
            return;
        }

        ItemStack sword = attacker.getMainHandItem();
        int sapphiric = EnchantmentHelper.getItemEnchantmentLevel(ModEnchantments.SAPPHIRIC, sword);
        if (sapphiric > 0) {
            applySapphiricArea(victim, attacker, sapphiric);
        }

        float multiplier = ModCombatEnchantments.damageMultiplier(sword);
        if (multiplier > 1.0F && !APPLYING_BONUS_SWORD_DAMAGE.get()) {
            APPLYING_BONUS_SWORD_DAMAGE.set(true);
            try {
                victim.hurt(source, amount * (multiplier - 1.0F));
            } finally {
                APPLYING_BONUS_SWORD_DAMAGE.set(false);
            }
        }
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
