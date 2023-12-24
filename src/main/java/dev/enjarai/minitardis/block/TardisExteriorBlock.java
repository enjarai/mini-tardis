package dev.enjarai.minitardis.block;

import dev.enjarai.minitardis.item.PolymerModels;
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
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
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

import java.util.Objects;

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
        return checkType(type, ModBlocks.TARDIS_EXTERIOR_ENTITY, (world1, pos, state1, blockEntity) -> blockEntity.tick(world1, pos, state1));
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
        var facing = initialBlockState.get(FACING);

        var exteriorElement = new ItemDisplayElement();
        exteriorElement.setItem(PolymerModels.getStack(PolymerModels.TARDIS_ALPHA[0]));
        exteriorElement.setOffset(new Vec3d(0, 1, 0));
        exteriorElement.setRightRotation(RotationAxis.NEGATIVE_Y.rotationDegrees(facing.asRotation()));

        var doorElement = new ItemDisplayElement();
        doorElement.setItem(PolymerModels.getStack(PolymerModels.EXTERIOR_DOOR_OPEN));
        doorElement.setOffset(new Vec3d(0, 1, 0).offset(facing, 1));
        doorElement.setRightRotation(RotationAxis.NEGATIVE_Y.rotationDegrees(facing.asRotation()));

        var doorCloseInteractionHandler = new VirtualElement.InteractionHandler() {
            @Override
            public void interact(ServerPlayerEntity player, Hand hand) {
                if (world.getBlockEntity(pos) instanceof TardisExteriorBlockEntity blockEntity && blockEntity.getLinkedTardis() != null) {
                    var tardis = blockEntity.getLinkedTardis();
                    world.playSound(null, pos, SoundEvents.BLOCK_BAMBOO_WOOD_DOOR_CLOSE, SoundCategory.BLOCKS);
                    tardis.getExteriorWorld().ifPresent(world ->
                            world.playSound(null, tardis.getCurrentLandedLocation().get().pos(),
                                    SoundEvents.BLOCK_BAMBOO_WOOD_DOOR_CLOSE, SoundCategory.BLOCKS));
                    tardis.setDoorOpen(false, false);
                }
            }
        };

        var leftDoorInteraction = new InteractionElement();
        leftDoorInteraction.setHandler(doorCloseInteractionHandler);
        leftDoorInteraction.setOffset(new Vec3d(0, 1.0 / 16.0 * -7, 0)
                .offset(facing, 0.5)
                .offset(facing.rotateYCounterclockwise(), 1.0 / 16.0 * 8.0));
        leftDoorInteraction.setSize(0.25f, 2);

        var rightDoorInteraction = new InteractionElement();
        rightDoorInteraction.setHandler(doorCloseInteractionHandler);
        rightDoorInteraction.setOffset(new Vec3d(0, 1.0 / 16.0 * -7, 0)
                .offset(facing, 0.5)
                .offset(facing.rotateYClockwise(), 1.0 / 16.0 * 8.0));
        rightDoorInteraction.setSize(0.25f, 2);

        return new ElementHolder() {
            byte currentAlpha = 0;
            boolean currentlyOpen;

            {
                addElement(exteriorElement);
            }

            @Override
            protected void onTick() {
                if (world.getTime() % 20 == 0) {
                    var facing = world.getBlockState(pos).get(FACING);

                    exteriorElement.setRightRotation(RotationAxis.NEGATIVE_Y.rotationDegrees(facing.asRotation()));
                    doorElement.setOffset(new Vec3d(0, 1, 0).offset(facing, 1));
                    doorElement.setRightRotation(RotationAxis.NEGATIVE_Y.rotationDegrees(facing.asRotation()));
                }

                if (world.getBlockEntity(pos) instanceof TardisExteriorBlockEntity blockEntity && blockEntity.getLinkedTardis() != null) {
                    byte alpha = (byte) MathHelper.clamp(blockEntity.getLinkedTardis().getState()
                            .getExteriorAlpha(blockEntity.getLinkedTardis()), -1, 15);
                    if (alpha != currentAlpha) {
                        exteriorElement.setItem(PolymerModels.getStack(alpha < 0 ? PolymerModels.TARDIS : PolymerModels.TARDIS_ALPHA[alpha]));
                        currentAlpha = alpha;
                    }

                    var open = Objects.requireNonNull(blockEntity.getLinkedTardis()).isDoorOpen();
                    if (open != currentlyOpen) {
                        if (open) {
                            addElement(doorElement);
                            addElement(leftDoorInteraction);
                            addElement(rightDoorInteraction);
                        } else {
                            removeElement(doorElement);
                            removeElement(leftDoorInteraction);
                            removeElement(rightDoorInteraction);
                        }
                        currentlyOpen = open;
                    }
                }
            }
        };
    }
}
