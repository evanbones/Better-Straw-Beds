package com.evandev.straw_backup_beds.compat;

import com.evandev.straw_backup_beds.block.StrawBedRegistrations;
import com.evandev.straw_backup_beds.config.ModConfig;
import com.farcr.nomansland.common.registry.blocks.NMLBlocks;

public final class NMLCompat {
    public static final String MOD_ID = "nomansland";

    public static void init() {
        StrawBedRegistrations.registerDestroyOnSleep(
                state -> ModConfig.get().nmlStrawBedBreaksOnSleep && state.is(NMLBlocks.STRAW_BED.get()));
    }
}
