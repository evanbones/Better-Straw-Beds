package com.evandev.better_straw_beds.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BedPart;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class StrawBedBlock extends BedBlock {
    @SuppressWarnings("unchecked")
    public static final MapCodec<BedBlock> CODEC = (MapCodec<BedBlock>) (MapCodec<?>) simpleCodec(StrawBedBlock::new);

    private static final VoxelShape BASE = Block.box(0.0, 0.0, 0.0, 16.0, 4.0, 16.0);
    private static final VoxelShape HEAD_NORTH = Shapes.or(BASE, Block.box(0.0, 0.0, 0.0, 16.0, 5.0, 8.0));
    private static final VoxelShape HEAD_EAST = Shapes.or(BASE, Block.box(8.0, 0.0, 0.0, 16.0, 5.0, 16.0));
    private static final VoxelShape HEAD_SOUTH = Shapes.or(BASE, Block.box(0.0, 0.0, 8.0, 16.0, 5.0, 16.0));
    private static final VoxelShape HEAD_WEST = Shapes.or(BASE, Block.box(0.0, 0.0, 0.0, 8.0, 5.0, 16.0));

    public StrawBedBlock(Properties properties) {
        super(DyeColor.YELLOW, properties);
    }

    @Override
    public MapCodec<BedBlock> codec() {
        return CODEC;
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        if (state.getValue(PART) != BedPart.HEAD) {
            return BASE;
        }

        Direction direction = getConnectedDirection(state).getOpposite();
        return switch (direction) {
            case SOUTH -> HEAD_SOUTH;
            case WEST -> HEAD_WEST;
            case EAST -> HEAD_EAST;
            default -> HEAD_NORTH;
        };
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return null;
    }
}
