package com.evandev.better_straw_beds.registry;

import com.evandev.better_straw_beds.Constants;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.level.block.SoundType;

public final class ModSounds {
    public static final SoundEvent STRAW_BED_BREAK = create("block.straw_bed.break");
    public static final SoundEvent STRAW_BED_STEP = create("block.straw_bed.step");
    public static final SoundEvent STRAW_BED_PLACE = create("block.straw_bed.place");
    public static final SoundEvent STRAW_BED_HIT = create("block.straw_bed.hit");
    public static final SoundEvent STRAW_BED_FALL = create("block.straw_bed.fall");
    public static final SoundEvent STRAW_BED_BREAK_LEAVE = create("block.straw_bed.break_leave");

    public static final SoundType STRAW_BED = new SoundType(
            1.0F, 1.0F, STRAW_BED_BREAK, STRAW_BED_STEP, STRAW_BED_PLACE, STRAW_BED_HIT, STRAW_BED_FALL);

    private static SoundEvent create(String path) {
        return SoundEvent.createVariableRangeEvent(Identifier.fromNamespaceAndPath(Constants.MOD_ID, path));
    }
}
