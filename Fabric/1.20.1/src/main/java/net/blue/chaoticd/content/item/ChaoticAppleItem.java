package net.blue.chaoticd.content.item;

import java.util.Map;
import net.blue.chaoticd.ChaoticDimensions;
import net.blue.chaoticd.content.worldgen.AuroraSafeArrival;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

/** A one-way Aurora Dimension food. Every physical apple carries Curse of Vanishing. */
public final class ChaoticAppleItem extends Item {
    private static final ResourceKey<Level> AURORA_DIMENSION = ResourceKey.create(Registries.DIMENSION,
        new ResourceLocation(ChaoticDimensions.MOD_ID, "aurora_dimension"));

    public ChaoticAppleItem(Properties properties) {
        super(properties);
    }

    @Override
    public ItemStack getDefaultInstance() {
        ItemStack stack = super.getDefaultInstance();
        applyCurse(stack);
        return stack;
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slot, boolean selected) {
        applyCurse(stack);
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
        ItemStack result = super.finishUsingItem(stack, level, entity);
        if (!level.isClientSide && entity instanceof ServerPlayer player) {
            ServerLevel aurora = player.server.getLevel(AURORA_DIMENSION);
            if (aurora == null) {
                player.displayClientMessage(
                    Component.translatable("message.chaoticd.aurora_unavailable"),
                    false
                );
                refundApple(player, result);
            } else {
                AuroraSafeArrival.find(aurora).ifPresentOrElse(
                    arrival -> teleportToAurora(player, aurora, arrival),
                    () -> {
                        player.displayClientMessage(
                            Component.translatable("message.chaoticd.aurora_no_safe_arrival"),
                            false
                        );
                        refundApple(player, result);
                    }
                );
            }
        }
        return result;
    }

    private static void teleportToAurora(ServerPlayer player, ServerLevel aurora, BlockPos arrival) {
        // A dimension arrival must never retain falling momentum from the old world.
        player.setDeltaMovement(Vec3.ZERO);
        player.fallDistance = 0.0F;
        player.teleportTo(
            aurora,
            arrival.getX() + 0.5D,
            arrival.getY() + 0.1D,
            arrival.getZ() + 0.5D,
            player.getYRot(),
            player.getXRot()
        );
    }

    private void refundApple(ServerPlayer player, ItemStack result) {
        if (player.getAbilities().instabuild) {
            return;
        }

        /* Item.finishUsingItem returns the same shrunk stack for food. Growing
         * it before returning avoids a race with the use-hand code replacing
         * the active slot after this method completes. */
        if (result.is(this)) {
            result.grow(1);
            return;
        }

        ItemStack refund = getDefaultInstance();
        if (!player.getInventory().add(refund)) {
            player.drop(refund, false);
        }
    }

    private static void applyCurse(ItemStack stack) {
        if (!EnchantmentHelper.getEnchantments(stack).containsKey(Enchantments.VANISHING_CURSE)) {
            EnchantmentHelper.setEnchantments(Map.of(Enchantments.VANISHING_CURSE, 1), stack);
        }
    }
}
