package dev.enjarai.minitardis.block;

import com.mojang.serialization.MapCodec;
import dev.enjarai.minitardis.util.PerhapsPolymerBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.FacingBlock;
import net.minecraft.state.StateManager;
import net.minecraft.util.math.Direction;

public class ArcNodeBlock extends FacingBlock implements PerhapsPolymerBlock {
    public static final MapCodec<ArcNodeBlock> CODEC = createCodec(ArcNodeBlock::new);

    protected ArcNodeBlock(Settings settings) {
        super(settings);
        setDefaultState(getStateManager().getDefaultState().with(FACING, Direction.NORTH));
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Override
    public Block getPerhapsPolymerBlock(BlockState state) {
        return Blocks.OBSERVER; // TODO proper facing and texture?
    }

    @Override
    protected MapCodec<? extends FacingBlock> getCodec() {
        return CODEC;
    }
}
