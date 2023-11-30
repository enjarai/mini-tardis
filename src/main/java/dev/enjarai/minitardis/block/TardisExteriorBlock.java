package dev.enjarai.minitardis.block;

import dev.enjarai.minitardis.item.PolymerModels;
import eu.pb4.polymer.core.api.block.PolymerBlock;
import eu.pb4.polymer.resourcepack.api.PolymerResourcePackUtils;
import eu.pb4.polymer.virtualentity.api.BlockWithElementHolder;
import eu.pb4.polymer.virtualentity.api.ElementHolder;
import eu.pb4.polymer.virtualentity.api.elements.ItemDisplayElement;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.BlockWithEntity;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.DirectionProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.*;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

@SuppressWarnings("deprecation")
public class TardisExteriorBlock extends BlockWithEntity implements PolymerBlock, BlockWithElementHolder {
    public static final DirectionProperty FACING = Properties.HORIZONTAL_FACING;

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
    public Block getPolymerBlock(BlockState state) {
        return Blocks.BARRIER;
    }

    @Override
    public Block getPolymerBlock(BlockState state, ServerPlayerEntity player) {
        return PolymerResourcePackUtils.hasPack(player) ? PolymerBlock.super.getPolymerBlock(state, player) : Blocks.LAPIS_BLOCK;
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

    @Override
    public void onBlockAdded(BlockState state, World world, BlockPos pos, BlockState oldState, boolean notify) {
        world.setBlockState(pos.up(), ModBlocks.TARDIS_EXTERIOR_EXTENSION.getDefaultState());
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(World world, BlockState state, BlockEntityType<T> type) {
        return validateTicker(type, ModBlocks.TARDIS_EXTERIOR_ENTITY, (world1, pos, state1, blockEntity) -> blockEntity.tick(world1, pos, state1));
    }

    @Override
    public boolean isTransparent(BlockState state, BlockView world, BlockPos pos) {
        return true;
    }

    @Override
    public boolean tickElementHolder(ServerWorld world, BlockPos pos, BlockState initialBlockState) {
        return true;
    }

    @Override
    public @Nullable ElementHolder createElementHolder(ServerWorld world, BlockPos pos, BlockState initialBlockState) {
        var exteriorElement = new ItemDisplayElement();
        exteriorElement.setItem(PolymerModels.getStack(PolymerModels.TARDIS_ALPHA[0]));
        exteriorElement.setOffset(new Vec3d(0, 1, 0));
        exteriorElement.setRightRotation(RotationAxis.NEGATIVE_Y.rotationDegrees(initialBlockState.get(FACING).asRotation()));

        return new ElementHolder() {
            byte currentAlpha = 0;

            {
                addElement(exteriorElement);
            }

            @Override
            protected void onTick() {
                if (world.getTime() % 20 == 0) {
                    exteriorElement.setRightRotation(RotationAxis.NEGATIVE_Y.rotationDegrees(world.getBlockState(pos).get(FACING).asRotation()));
                }

                if (world.getBlockEntity(pos) instanceof TardisExteriorBlockEntity blockEntity) {
                    byte alpha = (byte) MathHelper.clamp(blockEntity.getLinkedTardis().getState()
                            .getExteriorAlpha(blockEntity.getLinkedTardis()), -1, 15);
                    if (alpha != currentAlpha) {
                        exteriorElement.setItem(PolymerModels.getStack(alpha < 0 ? PolymerModels.TARDIS : PolymerModels.TARDIS_ALPHA[alpha]));
                        currentAlpha = alpha;
                    }
                }
            }
        };
    }
}
