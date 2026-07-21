package net.blue.chaoticd.content.item;

import java.util.Map;
import net.blue.chaoticd.ChaoticDimensions;
import net.blue.chaoticd.content.worldgen.PastelAuroraSkylandFeature;
import net.minecraft.core.registries.Registries;
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
            if (aurora != null) {
                PastelAuroraSkylandFeature.ensureArrivalIsland(aurora);
                player.teleportTo(aurora, 0.5D, 303.0D, 0.5D, player.getYRot(), player.getXRot());
            }
        }
        return result;
    }

    private static void applyCurse(ItemStack stack) {
        if (!EnchantmentHelper.getEnchantments(stack).containsKey(Enchantments.VANISHING_CURSE)) {
            EnchantmentHelper.setEnchantments(Map.of(Enchantments.VANISHING_CURSE, 1), stack);
        }
    }
}
