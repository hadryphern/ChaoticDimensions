package net.blue.chaoticd.content.fluid;

import java.util.Optional;
import net.blue.chaoticd.content.ModBlocks;
import net.blue.chaoticd.content.ModFluids;
import net.blue.chaoticd.content.ModItems;
import net.blue.chaoticd.content.ModTags;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import org.jetbrains.annotations.Nullable;

/**
 * Dense magical liquid used by Dream Fluid lakes.
 *
 * <p>Its flow behavior intentionally resembles lava rather than water:
 * it spreads slowly, loses more height per block and cannot create infinite
 * source blocks. The block class handles the heavy movement slowdown.</p>
 */
@SuppressWarnings("deprecation") // 1.20.1 still requires deprecated FlowingFluid extension hooks.
public abstract class DreamFluid extends FlowingFluid {
    @Override
    public Fluid getFlowing() {
        return ModFluids.FLOWING_DREAM_FLUID;
    }

    @Override
    public Fluid getSource() {
        return ModFluids.DREAM_FLUID;
    }

    @Override
    public Item getBucket() {
        return ModItems.DREAM_FLUID_BUCKET;
    }

    @Override
    public boolean isSame(Fluid fluid) {
        return fluid == ModFluids.DREAM_FLUID
            || fluid == ModFluids.FLOWING_DREAM_FLUID;
    }

    @Nullable
    @Override
    public ParticleOptions getDripParticle() {
        return ParticleTypes.DRIPPING_LAVA;
    }

    /**
     * Dream Fluid is rare and must not become an infinite source like water.
     */
    @Override
    protected boolean canConvertToSource(Level level) {
        return false;
    }

    @Override
    protected void beforeDestroyingBlock(
        LevelAccessor level,
        BlockPos pos,
        BlockState state
    ) {
        BlockEntity blockEntity =
            state.hasBlockEntity()
                ? level.getBlockEntity(pos)
                : null;

        Block.dropResources(
            state,
            level,
            pos,
            blockEntity
        );
    }

    /**
     * Lava searches only a short distance for a downward path.
     */
    @Override
    protected int getSlopeFindDistance(
        LevelReader level
    ) {
        return 2;
    }

    /**
     * The fluid level falls quickly while spreading horizontally.
     */
    @Override
    public int getDropOff(
        LevelReader level
    ) {
        return 2;
    }

    /**
     * Thirty ticks makes it visibly slow and heavy like Overworld lava.
     */
    @Override
    public int getTickDelay(
        LevelReader level
    ) {
        return 30;
    }

    @Override
    public boolean canBeReplacedWith(
        FluidState state,
        BlockGetter level,
        BlockPos pos,
        Fluid fluid,
        Direction direction
    ) {
        return direction == Direction.DOWN
            && !fluid.is(ModTags.DREAM_FLUID);
    }

    @Override
    protected float getExplosionResistance() {
        return 100.0F;
    }

    @Override
    public Optional<SoundEvent> getPickupSound() {
        return Optional.of(
            SoundEvents.BUCKET_FILL_LAVA
        );
    }

    @Override
    protected BlockState createLegacyBlock(
        FluidState state
    ) {
        return ModBlocks.DREAM_FLUID
            .defaultBlockState()
            .setValue(
                LiquidBlock.LEVEL,
                getLegacyLevel(state)
            );
    }

    public static final class Flowing
        extends DreamFluid {

        public Flowing() {
            registerDefaultState(
                getStateDefinition()
                    .any()
                    .setValue(LEVEL, 7)
                    .setValue(FALLING, false)
            );
        }

        @Override
        protected void createFluidStateDefinition(
            StateDefinition.Builder<
                Fluid,
                FluidState
            > builder
        ) {
            super.createFluidStateDefinition(
                builder
            );

            builder.add(LEVEL);
        }

        @Override
        public int getAmount(
            FluidState state
        ) {
            return state.getValue(LEVEL);
        }

        @Override
        public boolean isSource(
            FluidState state
        ) {
            return false;
        }
    }

    public static final class Source
        extends DreamFluid {

        public Source() {
            registerDefaultState(
                getStateDefinition()
                    .any()
                    .setValue(FALLING, false)
            );
        }

        @Override
        public int getAmount(
            FluidState state
        ) {
            return 8;
        }

        @Override
        public boolean isSource(
            FluidState state
        ) {
            return true;
        }
    }
}
