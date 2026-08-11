package com.evandev.better_straw_beds.block;

import com.evandev.better_straw_beds.registry.ModBlocks;
import com.evandev.better_straw_beds.registry.ModSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BedPart;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

public final class BedBreaker {
    public static boolean destroyBed(Level level, BlockPos pos) {
        return destroyBed(level, pos, true);
    }

    public static boolean destroyBed(Level level, BlockPos pos, boolean playSound) {
        BlockState state = level.getBlockState(pos);
        if (!isBedState(state)) {
            return false;
        }

        Direction toOther = state.getValue(BlockStateProperties.BED_PART) == BedPart.HEAD
                ? state.getValue(BlockStateProperties.HORIZONTAL_FACING).getOpposite()
                : state.getValue(BlockStateProperties.HORIZONTAL_FACING);

        BlockPos otherPos = pos.relative(toOther);
        BlockState otherState = level.getBlockState(otherPos);
        if (otherState.is(state.getBlock()) && otherState.getValue(BlockStateProperties.BED_PART) != state.getValue(BlockStateProperties.BED_PART)) {
            breakSilently(level, otherPos, otherState, playSound);
        }

        breakSilently(level, pos, state, playSound);
        return true;
    }

    public static void playBreakSound(Level level, BlockPos pos) {
        level.playSound(null, pos, ModSounds.STRAW_BED_BREAK_LEAVE, SoundSource.BLOCKS, 1.0F, 1.0F);
    }

    private static void breakSilently(Level level, BlockPos pos, BlockState state, boolean playSound) {
        if (ModBlocks.isStrawBed(state)) {
            if (playSound) {
                playBreakSound(level, pos);
            }
        } else {
            level.levelEvent(2001, pos, Block.getId(state));
        }

        level.setBlock(pos, Blocks.AIR.defaultBlockState(),
                Block.UPDATE_ALL | Block.UPDATE_SUPPRESS_DROPS | Block.UPDATE_KNOWN_SHAPE);
    }

    public static boolean isBedState(BlockState state) {
        return state.hasProperty(BlockStateProperties.BED_PART) && state.hasProperty(BlockStateProperties.HORIZONTAL_FACING);
    }
}
