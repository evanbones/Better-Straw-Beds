package com.evandev.straw_backup_beds.respawn;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import org.jetbrains.annotations.Nullable;

public interface PendingBedSoundHolder {
    @Nullable
    ServerLevel straw_backup_beds$getPendingBreakSoundLevel();

    @Nullable
    BlockPos straw_backup_beds$getPendingBreakSoundPos();

    void straw_backup_beds$clearPendingBreakSound();
}
