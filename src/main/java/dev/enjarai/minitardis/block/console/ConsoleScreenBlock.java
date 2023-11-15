package dev.enjarai.minitardis.block.console;

import dev.enjarai.minitardis.block.ModBlocks;
import dev.enjarai.minitardis.block.TardisAware;
import eu.pb4.polymer.core.api.block.PolymerBlock;
import eu.pb4.polymer.virtualentity.api.BlockWithElementHolder;
import eu.pb4.polymer.virtualentity.api.ElementHolder;
import eu.pb4.polymer.virtualentity.api.elements.TextDisplayElement;
import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.DirectionProperty;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class ConsoleScreenBlock extends BlockWithEntity implements PolymerBlock, TardisAware, BlockWithElementHolder {
    public static final DirectionProperty FACING = HorizontalFacingBlock.FACING;

    public ConsoleScreenBlock(Settings settings) {
        super(settings);
        setDefaultState(getStateManager().getDefaultState().with(FACING, Direction.NORTH));
    }

    @Nullable
    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new ConsoleScreenBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(World world, BlockState state, BlockEntityType<T> type) {
        return validateTicker(type, ModBlocks.CONSOLE_SCREEN_ENTITY, (world1, pos, state1, blockEntity) -> blockEntity.tick(world1, pos, state1));
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(FACING);
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
            var textDisplay = new TextDisplayElement();

            return new ElementHolder() {
                {
                    addElement(textDisplay);
                }

                @Override
                protected void onTick() {

                }

                private void update() {
                    textDisplay.setText(tardis.getState().getName());
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
