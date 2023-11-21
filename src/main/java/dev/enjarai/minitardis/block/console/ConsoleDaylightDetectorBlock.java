package dev.enjarai.minitardis.block.console;

import dev.enjarai.minitardis.block.TardisAware;
import dev.enjarai.minitardis.component.TardisControl;
import eu.pb4.polymer.core.api.block.PolymerBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.ShapeContext;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.sound.SoundEvents;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;

import java.util.function.BiFunction;

@SuppressWarnings("deprecation")
public class ConsoleDaylightDetectorBlock extends Block implements PolymerBlock, ConsoleInput, TardisAware {
    public static final BooleanProperty INVERTED = Properties.INVERTED;
    protected static final VoxelShape SHAPE = Block.createCuboidShape(0.0, 0.0, 0.0, 16.0, 6.0, 16.0);

    private final BiFunction<TardisControl, Boolean, Boolean> controlInput;

    public ConsoleDaylightDetectorBlock(Settings settings, BiFunction<TardisControl, Boolean, Boolean> controlInput) {
        super(settings);
        this.controlInput = controlInput;
        setDefaultState(getStateManager().getDefaultState().with(INVERTED, true));
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(INVERTED);
    }

    @Override
    public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        if (!player.getAbilities().allowModifyWorld) {
            return ActionResult.PASS;
        } else {
            var value = !state.cycle(INVERTED).get(INVERTED);
            if (getTardis(world).map(tardis -> controlInput.apply(tardis.getControls(), value)).orElse(false)) {
                inputSuccess(world, pos, SoundEvents.BLOCK_NOTE_BLOCK_HAT.value(), value ? 2 : 1);
                world.setBlockState(pos, state.with(INVERTED, !value), Block.NOTIFY_ALL);
                if (value) {
//                    inputSuccess(world, pos, SoundEvents.ENTITY_HORSE_BREATHE, 0); // TODO smt better
                }
            } else {
                inputFailure(world, pos, SoundEvents.ENTITY_ZOMBIE_ATTACK_IRON_DOOR, 3);
            }
            return ActionResult.SUCCESS;
        }
    }

    @Override
    public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return SHAPE;
    }

    @Override
    public Block getPolymerBlock(BlockState state) {
        return Blocks.DAYLIGHT_DETECTOR;
    }

    @Override
    public BlockState getPolymerBlockState(BlockState state) {
        return getPolymerBlock(state).getStateWithProperties(state);
    }
}
