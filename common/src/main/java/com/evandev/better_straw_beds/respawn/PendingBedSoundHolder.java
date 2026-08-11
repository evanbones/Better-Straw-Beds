package com.evandev.better_straw_beds.respawn;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import org.jetbrains.annotations.Nullable;

public interface PendingBedSoundHolder {
    @Nullable
    ServerLevel better_straw_beds$getPendingBreakSoundLevel();

    @Nullable
    BlockPos better_straw_beds$getPendingBreakSoundPos();

    void better_straw_beds$clearPendingBreakSound();
}
