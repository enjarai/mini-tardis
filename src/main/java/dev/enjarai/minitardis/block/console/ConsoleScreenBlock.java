package dev.enjarai.minitardis.block.console;

import dev.enjarai.minitardis.block.TardisAware;
import eu.pb4.polymer.core.api.block.PolymerBlock;
import eu.pb4.polymer.virtualentity.api.BlockWithElementHolder;
import eu.pb4.polymer.virtualentity.api.ElementHolder;
import eu.pb4.polymer.virtualentity.api.elements.TextDisplayElement;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.HorizontalFacingBlock;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.DirectionProperty;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.WorldView;
import org.jetbrains.annotations.Nullable;

public class ConsoleScreenBlock extends Block implements PolymerBlock, TardisAware, BlockWithElementHolder {
    public static final DirectionProperty FACING = HorizontalFacingBlock.FACING;

    public ConsoleScreenBlock(Settings settings) {
        super(settings);
        setDefaultState(getStateManager().getDefaultState().with(FACING, Direction.NORTH));
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Nullable
    @Override
    public BlockState getPlacementState(ItemPlacementContext ctx) {
        BlockState blockState = this.getDefaultState();
        WorldView worldView = ctx.getWorld();
        BlockPos blockPos = ctx.getBlockPos();
        Direction[] directions = ctx.getPlacementDirections();

        for(Direction direction : directions) {
            if (direction.getAxis().isHorizontal()) {
                Direction direction2 = direction.getOpposite();
                blockState = blockState.with(FACING, direction2);
                if (blockState.canPlaceAt(worldView, blockPos)) {
                    return blockState;
                }
            }
        }

        return null;
    }

    @Override
    public Block getPolymerBlock(BlockState state) {
        return Blocks.OAK_WALL_SIGN;
    }

    @Override
    public BlockState getPolymerBlockState(BlockState state) {
        return getPolymerBlock(state).getStateWithProperties(state);
    }

    @Override
    public boolean tickElementHolder(ServerWorld world, BlockPos pos, BlockState initialBlockState) {
        return true;
    }

    @Override
    public @Nullable ElementHolder createElementHolder(ServerWorld world, BlockPos pos, BlockState initialBlockState) {
        var tardisOptional = getTardis(world);
        if (tardisOptional.isPresent()) {
            var tardis = tardisOptional.get();
            var rotation = RotationAxis.NEGATIVE_Y.rotationDegrees(initialBlockState.get(FACING).asRotation());

            var stateText = new TextDisplayElement();
            var destinationText = new TextDisplayElement();

            stateText.setRightRotation(rotation);
            destinationText.setRightRotation(rotation);
            destinationText.setOffset(new Vec3d(0, -0.5, 0));

            return new ElementHolder() {
                {
                    addElement(stateText);
                    addElement(destinationText);
                    update();
                }

                @Override
                protected void onTick() {
                    update();
                }

                private void update() {
                    stateText.setText(tardis.getState().getName());
                    destinationText.setText(Text.literal(tardis.getDestination()
                            .map(l -> l.pos().getX() + " " + l.pos().getY() + " " + l.pos().getZ() + " " + l.facing().getName().toUpperCase().charAt(0))
                            .orElse("Unknown"))); // TODO dimension
                }
            };
        }
        return null;
    }

    //    @Override
//    public void onPolymerBlockSend(BlockState blockState, BlockPos.Mutable pos, ServerPlayerEntity player) {
//        var main = new NbtCompound();
//        main.putString("id", "minecraft:sign");
//        main.putInt("x", pos.getX());
//        main.putInt("y", pos.getY());
//        main.putInt("z", pos.getZ());
//        main.putBoolean("is_waxed", true);
//        var frontText = new NbtCompound();
//        var messages = new NbtList();
//        messages.add()
//        player.networkHandler.sendPacket(PolymerBlockUtils.createBlockEntityPacket(pos.toImmutable(), BlockEntityType.SIGN, main));
//    }
}
