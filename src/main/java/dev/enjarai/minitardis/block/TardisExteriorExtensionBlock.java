package dev.enjarai.minitardis.block;

import dev.enjarai.minitardis.item.PolymerModels;
import dev.enjarai.minitardis.util.PerhapsElementHolder;
import dev.enjarai.minitardis.util.PerhapsPolymerBlock;
import eu.pb4.polymer.virtualentity.api.BlockWithElementHolder;
import eu.pb4.polymer.virtualentity.api.ElementHolder;
import eu.pb4.polymer.virtualentity.api.attachment.BlockBoundAttachment;
import eu.pb4.polymer.virtualentity.api.attachment.HolderAttachment;
import eu.pb4.polymer.virtualentity.api.elements.InteractionElement;
import eu.pb4.polymer.virtualentity.api.elements.ItemDisplayElement;
import eu.pb4.polymer.virtualentity.api.elements.VirtualElement;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.ShapeContext;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.DirectionProperty;
import net.minecraft.state.property.IntProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

@SuppressWarnings("deprecation")
public class TardisExteriorExtensionBlock extends Block implements PerhapsPolymerBlock, BlockWithElementHolder {
    public static final DirectionProperty FACING = Properties.HORIZONTAL_FACING;
    public static final IntProperty VISIBLENESS = IntProperty.of("visibleness", 0, 16);

    public static final VoxelShape OUTLINE_SHAPE = VoxelShapes.union(
            Block.createCuboidShape(-1, 0, -1, 17, 15, 17),
            Block.createCuboidShape(-2, 0, -2, 0, 15, 0),
            Block.createCuboidShape(16, 0, -2, 18, 15, 0),
            Block.createCuboidShape(16, 0, 16, 18, 15, 18),
            Block.createCuboidShape(-2, 0, 16, 0, 15, 18),
            Block.createCuboidShape(-2, 15, -2, 18, 21, 18),
            Block.createCuboidShape(-3, 16, -1, 19, 20, 17),
            Block.createCuboidShape(-1, 16, -3, 17, 20, 19),
            Block.createCuboidShape(-1, 21, -1, 17, 23, 17),
            Block.createCuboidShape(6, 23, 6, 10, 27, 10)
    );

    public TardisExteriorExtensionBlock(Settings settings) {
        super(settings);
        setDefaultState(getStateManager().getDefaultState().with(FACING, Direction.NORTH).with(VISIBLENESS, 16));
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(FACING, VISIBLENESS);
    }

    @Override
    public BlockState getStateForNeighborUpdate(BlockState state, Direction direction, BlockState neighborState, WorldAccess world, BlockPos pos, BlockPos neighborPos) {
        return direction == Direction.DOWN && !neighborState.isOf(ModBlocks.TARDIS_EXTERIOR) ? Blocks.AIR.getDefaultState() : state;
    }

    @Override
    public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, BlockHitResult hit) {
        if (!world.isClient()
                && hit.getSide() == state.get(FACING)
                && world.getBlockEntity(pos.down()) instanceof TardisExteriorBlockEntity blockEntity) {
            blockEntity.teleportEntityIn(player);
            return ActionResult.SUCCESS;
        }
        return super.onUse(state, world, pos, player, hit);
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
    public boolean isTransparent(BlockState state, BlockView world, BlockPos pos) {
        return true;
    }

    @Override
    public Block getPerhapsPolymerBlock(BlockState state) {
        return Blocks.BARRIER;
    }

    @Override
    public boolean tickElementHolder(ServerWorld world, BlockPos pos, BlockState initialBlockState) {
        return true;
    }

    @Override
    public @Nullable ElementHolder createElementHolder(ServerWorld world, BlockPos pos, BlockState initialBlockState) {
        int alpha = initialBlockState.get(VISIBLENESS);
        var facing = initialBlockState.get(FACING);

        var exteriorElement = new ItemDisplayElement();
        exteriorElement.setItem(PolymerModels.getStack(alpha >= 16 ? PolymerModels.TARDIS : PolymerModels.TARDIS_ALPHA[alpha]));
        exteriorElement.setRightRotation(RotationAxis.NEGATIVE_Y.rotationDegrees(facing.asRotation()));

        var doorElement = new ItemDisplayElement();
        doorElement.setItem(PolymerModels.getStack(PolymerModels.EXTERIOR_DOOR_OPEN));
        doorElement.setOffset(Vec3d.ZERO.offset(facing, 1));
        doorElement.setRightRotation(RotationAxis.NEGATIVE_Y.rotationDegrees(facing.asRotation()));

        var doorCloseInteractionHandler = new VirtualElement.InteractionHandler() {
            @Override
            public void interact(ServerPlayerEntity player, Hand hand) {
                if (world.getBlockEntity(pos.down()) instanceof TardisExteriorBlockEntity blockEntity && blockEntity.getLinkedTardis() != null) {
                    var tardis = blockEntity.getLinkedTardis();
                    world.playSound(null, pos.down(), SoundEvents.BLOCK_BAMBOO_WOOD_DOOR_CLOSE, SoundCategory.BLOCKS);
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

        return new PerhapsElementHolder() {
            boolean currentlyOpen;

            {
                addElement(exteriorElement);
            }

            @Override
            protected void onTick() {
                if (world.getTime() % 20 == 0 && world.getBlockEntity(pos.down()) instanceof TardisExteriorBlockEntity blockEntity && blockEntity.getLinkedTardis() != null) {
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

            @Override
            public void notifyUpdate(HolderAttachment.UpdateType updateType) {
                if (updateType == BlockBoundAttachment.BLOCK_STATE_UPDATE && getAttachment() instanceof BlockBoundAttachment blockAttachment) {
                    var state = blockAttachment.getBlockState();

                    var facing = state.get(FACING);

                    exteriorElement.setRightRotation(RotationAxis.NEGATIVE_Y.rotationDegrees(facing.asRotation()));
                    doorElement.setOffset(new Vec3d(0, 1, 0).offset(facing, 1));
                    doorElement.setRightRotation(RotationAxis.NEGATIVE_Y.rotationDegrees(facing.asRotation()));

                    int alpha = state.get(VISIBLENESS);
                    exteriorElement.setItem(PolymerModels.getStack(alpha >= 16 ? PolymerModels.TARDIS : PolymerModels.TARDIS_ALPHA[alpha]));
                }
            }
        };
    }
}
