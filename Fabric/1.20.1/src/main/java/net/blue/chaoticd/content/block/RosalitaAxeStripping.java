package net.blue.chaoticd.content.block;

import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.state.BlockState;

/** Makes every axe, including vanilla axes, strip Rosalita logs like native wood. */
public final class RosalitaAxeStripping {
    private RosalitaAxeStripping() {
    }

    public static void initialize() {
        UseBlockCallback.EVENT.register((player, world, hand, hitResult) -> {
            ItemStack held = player.getItemInHand(hand);
            if (!(held.getItem() instanceof AxeItem)) {
                return InteractionResult.PASS;
            }

            BlockPos pos = hitResult.getBlockPos();
            BlockState stripped = strippedState(world.getBlockState(pos));
            if (stripped == null) {
                return InteractionResult.PASS;
            }

            if (!world.isClientSide) {
                world.setBlock(pos, stripped, 11);
                held.hurtAndBreak(1, player, broken -> broken.broadcastBreakEvent(hand));
            }
            return InteractionResult.sidedSuccess(world.isClientSide);
        });
    }

    private static BlockState strippedState(BlockState state) {
        if (state.is(ModBlocks.get("rosalita_log"))) {
            return withAxis(ModBlocks.get("stripped_rosalita_log").defaultBlockState(), state);
        }
        if (state.is(ModBlocks.get("rosalita_wood"))) {
            return withAxis(ModBlocks.get("stripped_rosalita_wood").defaultBlockState(), state);
        }
        return null;
    }

    private static BlockState withAxis(BlockState replacement, BlockState source) {
        return replacement.setValue(RotatedPillarBlock.AXIS, source.getValue(RotatedPillarBlock.AXIS));
    }
}
