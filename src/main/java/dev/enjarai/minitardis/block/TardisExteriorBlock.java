package dev.enjarai.minitardis.block;

import dev.enjarai.minitardis.item.PolymerModels;
import dev.enjarai.minitardis.util.PerhapsElementHolder;
import dev.enjarai.minitardis.util.PerhapsPolymerBlock;
import eu.pb4.polymer.core.api.block.PolymerBlock;
import eu.pb4.polymer.core.api.utils.PolymerSyncUtils;
import eu.pb4.polymer.core.api.utils.PolymerUtils;
import eu.pb4.polymer.networking.api.PolymerHandshakeHandler;
import eu.pb4.polymer.networking.api.PolymerServerNetworking;
import eu.pb4.polymer.resourcepack.api.PolymerResourcePackUtils;
import eu.pb4.polymer.virtualentity.api.BlockWithElementHolder;
import eu.pb4.polymer.virtualentity.api.ElementHolder;
import eu.pb4.polymer.virtualentity.api.elements.InteractionElement;
import eu.pb4.polymer.virtualentity.api.elements.ItemDisplayElement;
import eu.pb4.polymer.virtualentity.api.elements.VirtualElement;
import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.DirectionProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.*;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

import static dev.enjarai.minitardis.block.TardisExteriorExtensionBlock.VISIBLENESS;

@SuppressWarnings("deprecation")
public class TardisExteriorBlock extends BlockWithEntity implements PerhapsPolymerBlock {
    public static final DirectionProperty FACING = Properties.HORIZONTAL_FACING;

    public static final VoxelShape OUTLINE_SHAPE = VoxelShapes.union(
            Block.createCuboidShape(-1, 2, -1, 17, 16, 17),
            Block.createCuboidShape(-3, 0, -3, 19, 2, 19),
            Block.createCuboidShape(-2, 2, -2, 0, 16, 0),
            Block.createCuboidShape(16, 2, -2, 18, 16, 0),
            Block.createCuboidShape(16, 2, 16, 18, 16, 18),
            Block.createCuboidShape(-2, 2, 16, 0, 16, 18)
    );

    protected TardisExteriorBlock(Settings settings) {
        super(settings);
        setDefaultState(getStateManager().getDefaultState().with(FACING, Direction.NORTH));
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Nullable
    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new TardisExteriorBlockEntity(pos, state);
    }

    @Override
    public Block getPerhapsPolymerBlock(BlockState state, ServerPlayerEntity player) {
        return Blocks.BARRIER;
    }

    @Override
    public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        if (!world.isClient()
                && hit.getSide() == state.get(FACING)
                && world.getBlockEntity(pos) instanceof TardisExteriorBlockEntity blockEntity) {
            blockEntity.teleportEntityIn(player);
            return ActionResult.SUCCESS;
        }
        return super.onUse(state, world, pos, player, hand, hit);
    }

    // Disabled because of silly transparency issues on block outlines
//    @Override
//    public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
//        return OUTLINE_SHAPE;
//    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return VoxelShapes.fullCube();
    }

    @Override
    public void onBlockAdded(BlockState state, World world, BlockPos pos, BlockState oldState, boolean notify) {
        world.setBlockState(pos.up(), ModBlocks.TARDIS_EXTERIOR_EXTENSION.getStateWithProperties(state).with(VISIBLENESS, 0));
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(World world, BlockState state, BlockEntityType<T> type) {
        return checkType(type, ModBlocks.TARDIS_EXTERIOR_ENTITY, (world1, pos, state1, blockEntity) -> blockEntity.tick(world1, pos, state1));
    }

    @Override
    public boolean isTransparent(BlockState state, BlockView world, BlockPos pos) {
        return true;
    }
}
