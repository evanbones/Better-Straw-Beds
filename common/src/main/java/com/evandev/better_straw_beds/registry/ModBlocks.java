package com.evandev.better_straw_beds.registry;

import com.evandev.better_straw_beds.Constants;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;

public final class ModBlocks {
    public static final ResourceLocation STRAW_BED_ID = ResourceLocation.fromNamespaceAndPath(Constants.MOD_ID, "straw_bed");

    public static Block STRAW_BED;
    public static Item STRAW_BED_ITEM;

    private ModBlocks() {
    }

    public static BlockBehaviour.Properties strawBedProperties() {
        return BlockBehaviour.Properties.of()
                .mapColor(MapColor.COLOR_YELLOW)
                .sound(ModSounds.STRAW_BED)
                .strength(0.2F)
                .noOcclusion()
                .ignitedByLava()
                .pushReaction(PushReaction.DESTROY);
    }

    public static boolean isStrawBed(BlockState state) {
        return STRAW_BED != null && state.is(STRAW_BED);
    }
}
