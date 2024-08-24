package dev.enjarai.minitardis.block.console;

import dev.enjarai.minitardis.block.TardisAware;
import dev.enjarai.minitardis.item.FloppyItem;
import dev.enjarai.minitardis.item.ModItems;
import dev.enjarai.minitardis.item.PolymerModels;
import eu.pb4.polymer.core.api.block.PolymerBlock;
import eu.pb4.polymer.resourcepack.api.PolymerModelData;
import eu.pb4.polymer.resourcepack.api.PolymerResourcePackUtils;
import eu.pb4.polymer.virtualentity.api.BlockWithElementHolder;
import eu.pb4.polymer.virtualentity.api.ElementHolder;
import eu.pb4.polymer.virtualentity.api.attachment.BlockBoundAttachment;
import eu.pb4.polymer.virtualentity.api.elements.ItemDisplayElement;
import net.minecraft.block.*;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.DirectionProperty;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldView;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

public abstract class ScreenBlock extends BlockWithEntity implements PolymerBlock, TardisAware, BlockWithElementHolder, ConsoleInput {
    public static final BooleanProperty HAS_FLOPPY = BooleanProperty.of("has_floppy");

    protected ScreenBlock(Settings settings) {
        super(settings);
    }

    public boolean trySwitchFloppy(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand) {
        if (world.getBlockEntity(pos) instanceof ScreenBlockEntity blockEntity) {
            var handStack = player.getStackInHand(hand);
            var blockStack = blockEntity.inventory.getStack(0);
            @SuppressWarnings("DataFlowIssue")
            var elementHolder = (ScreenElementHolder) BlockBoundAttachment.get(world, pos).holder();

            if (handStack.isOf(ModItems.FLOPPY) && blockStack.isEmpty()) {
                blockEntity.inventory.setStack(0, handStack.split(1));
                world.setBlockState(pos, state.with(HAS_FLOPPY, true));
                elementHolder.setFloppyVisible(true);

                world.playSound(null, pos, SoundEvents.BLOCK_DISPENSER_DISPENSE, SoundCategory.BLOCKS, 1, 2);
                return true;
            } else if (handStack.isEmpty() && !blockStack.isEmpty()) {
                player.setStackInHand(hand, blockStack.split(1));
                world.setBlockState(pos, state.with(HAS_FLOPPY, false));
                elementHolder.setFloppyVisible(false);

                world.playSound(null, pos, SoundEvents.BLOCK_DISPENSER_FAIL, SoundCategory.BLOCKS, 1, 1.5f);
                return true;
            }
        }
        return false;
    }

    @Override
    public BlockState getPolymerBlockState(BlockState state) {
        return Blocks.BARRIER.getDefaultState();
    }

    @Override
    public BlockState getPolymerBlockState(BlockState state, ServerPlayerEntity player) {
        return PolymerResourcePackUtils.hasMainPack(player) ? PolymerBlock.super.getPolymerBlockState(state, player) : Blocks.COAL_BLOCK.getDefaultState();
    }

    @Override
    public boolean isTransparent(BlockState state, BlockView world, BlockPos pos) {
        return true;
    }

    protected static abstract class ScreenElementHolder extends ElementHolder {
        final ItemDisplayElement floppyElement;

        ScreenElementHolder(BlockState initialBlockState, PolymerModelData model) {
            var modelElement = new ItemDisplayElement();
            modelElement.setItem(PolymerModels.getStack(model));
            applyModelTranslations(modelElement, initialBlockState);

            floppyElement = new ItemDisplayElement();
            floppyElement.setItem(PolymerModels.getStack(FloppyItem.MODEL));
            applyFloppyTranslations(floppyElement, initialBlockState);

            addElement(modelElement);
            if (initialBlockState.get(HAS_FLOPPY)) {
                addElement(floppyElement);
            }
        }

        abstract void applyModelTranslations(ItemDisplayElement element, BlockState state);

        abstract void applyFloppyTranslations(ItemDisplayElement element, BlockState state);

        void setFloppyVisible(boolean visible) {
            if (visible) {
                addElement(floppyElement);
            } else {
                removeElement(floppyElement);
            }
        }
    }
}
