package dev.enjarai.minitardis.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.block.BlockState;
import net.minecraft.block.BlockWithEntity;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class MakeshiftEngineBlock extends BlockWithEntity {
    MapCodec<MakeshiftEngineBlock> CODEC = createCodec(MakeshiftEngineBlock::new);
    public MakeshiftEngineBlock(Settings settings) {
        super(settings);
    }

    @Override
    protected MapCodec<? extends BlockWithEntity> getCodec() {
        return CODEC;
    }

    @Nullable
    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new MakeshiftEngineBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(World world, BlockState state, BlockEntityType<T> type) {
        return validateTicker(type, ModBlocks.MAKESHIFT_ENGINE_ENTITY, (world1, pos, state1, blockEntity) -> {
            if (world1 instanceof ServerWorld serverWorld) {
                blockEntity.tick(serverWorld, pos, state1);
            }
        });
    }
}
