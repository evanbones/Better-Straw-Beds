package com.evandev.better_straw_beds;

import com.evandev.better_straw_beds.block.StrawBedRegistrations;
import com.evandev.better_straw_beds.config.ModConfig;
import com.evandev.better_straw_beds.registry.ModBlocks;

public class CommonClass {

    public static void init() {
        ModConfig.load();

        StrawBedRegistrations.registerDestroyOnSleep(state -> ModConfig.get().strawBedBreaksOnSleep && ModBlocks.isStrawBed(state));
    }
}
