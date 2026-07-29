package net.blue.chaoticd.content.item;

import net.blue.chaoticd.ChaoticDimensions;
import net.blue.chaoticd.content.ModTags;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.tags.TagKey;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.EyeOfEnder;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.structure.Structure;

/**
 * Aurora-themed Eye of Ender that locates the nearest Dream Fluid lake.
 *
 * <p>In the Overworld it searches for either the underground cavern or the
 * extremely high floating lake. In Aurora it searches for the original
 * floating Dream Fluid island.</p>
 */
public final class CrystalineSeeItem extends Item {
    private static final int SEARCH_RADIUS_CHUNKS = 256;

    private static final ResourceKey<Level>
        AURORA_DIMENSION =
        ResourceKey.create(
            Registries.DIMENSION,
            new ResourceLocation(
                ChaoticDimensions.MOD_ID,
                "aurora_dimension"
            )
        );

    public CrystalineSeeItem(
        Properties properties
    ) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(
        Level level,
        Player player,
        InteractionHand hand
    ) {
        ItemStack stack =
            player.getItemInHand(hand);

        if (level.isClientSide) {
            return InteractionResultHolder.success(
                stack
            );
        }

        ServerLevel serverLevel =
            (ServerLevel) level;

        TagKey<Structure> targetTag =
            structureTagFor(serverLevel);

        if (targetTag == null) {
            player.displayClientMessage(
                Component.translatable(
                    "message.chaoticd.crystaline_see_wrong_dimension"
                ),
                true
            );

            return InteractionResultHolder.fail(
                stack
            );
        }

        BlockPos target =
            serverLevel.findNearestMapStructure(
                targetTag,
                player.blockPosition(),
                SEARCH_RADIUS_CHUNKS,
                false
            );

        if (target == null) {
            player.displayClientMessage(
                Component.translatable(
                    "message.chaoticd.crystaline_see_not_found"
                ),
                true
            );

            return InteractionResultHolder.fail(
                stack
            );
        }

        EyeOfEnder locator =
            new EyeOfEnder(
                serverLevel,
                player.getX(),
                player.getY() + 0.5D,
                player.getZ()
            );

        ItemStack displayedItem =
            stack.copy();

        displayedItem.setCount(1);

        locator.setItem(displayedItem);
        locator.signalTo(target);

        serverLevel.addFreshEntity(locator);

        serverLevel.playSound(
            null,
            player.getX(),
            player.getY(),
            player.getZ(),
            SoundEvents.ENDER_EYE_LAUNCH,
            SoundSource.NEUTRAL,
            0.5F,
            0.4F
                / (
                    serverLevel.getRandom()
                        .nextFloat()
                        * 0.4F
                    + 0.8F
                )
        );

        serverLevel.levelEvent(
            null,
            1003,
            player.blockPosition(),
            0
        );

        if (
            !player.getAbilities()
                .instabuild
        ) {
            stack.shrink(1);
        }

        player.awardStat(
            Stats.ITEM_USED.get(this)
        );

        if (player instanceof ServerPlayer) {
            player.swing(hand, true);
        }

        return InteractionResultHolder.consume(
            stack
        );
    }

    private static TagKey<Structure>
        structureTagFor(
            ServerLevel level
        ) {
        if (
            level.dimension()
                .equals(Level.OVERWORLD)
        ) {
            return ModTags
                .DREAM_FLUID_OVERWORLD_STRUCTURES;
        }

        if (
            level.dimension()
                .equals(AURORA_DIMENSION)
        ) {
            return ModTags
                .DREAM_FLUID_AURORA_STRUCTURES;
        }

        return null;
    }
}
