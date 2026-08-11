package com.evandev.straw_backup_beds;

import com.evandev.straw_backup_beds.block.StrawBedRegistrations;
import com.evandev.straw_backup_beds.config.ModConfig;
import com.evandev.straw_backup_beds.registry.ModBlocks;

public class CommonClass {

    public static void init() {
        ModConfig.load();

        StrawBedRegistrations.registerDestroyOnSleep(state -> ModConfig.get().strawBedBreaksOnSleep && ModBlocks.isStrawBed(state));
    }
}
