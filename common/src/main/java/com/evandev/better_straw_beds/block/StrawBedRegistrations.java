package com.evandev.better_straw_beds.block;

import net.minecraft.world.level.block.state.BlockState;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Predicate;

public final class StrawBedRegistrations {
    private static final List<Predicate<BlockState>> DESTROY_ON_SLEEP = new CopyOnWriteArrayList<>();

    public static void registerDestroyOnSleep(Predicate<BlockState> predicate) {
        DESTROY_ON_SLEEP.add(predicate);
    }

    public static boolean shouldDestroyOnSleep(BlockState state) {
        for (Predicate<BlockState> predicate : DESTROY_ON_SLEEP) {
            if (predicate.test(state)) {
                return true;
            }
        }
        return false;
    }
}
