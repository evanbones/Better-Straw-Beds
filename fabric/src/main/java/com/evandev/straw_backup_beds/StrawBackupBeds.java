package com.evandev.straw_backup_beds;

import com.evandev.straw_backup_beds.block.StrawBedBlock;
import com.evandev.straw_backup_beds.registry.ModBlocks;
import com.evandev.straw_backup_beds.registry.ModSounds;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.fabricmc.fabric.api.registry.FlammableBlockRegistry;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.BedItem;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;

public class StrawBackupBeds implements ModInitializer {

    private static void registerSounds() {
        Registry.register(BuiltInRegistries.SOUND_EVENT, ModSounds.STRAW_BED_BREAK.getLocation(), ModSounds.STRAW_BED_BREAK);
        Registry.register(BuiltInRegistries.SOUND_EVENT, ModSounds.STRAW_BED_STEP.getLocation(), ModSounds.STRAW_BED_STEP);
        Registry.register(BuiltInRegistries.SOUND_EVENT, ModSounds.STRAW_BED_PLACE.getLocation(), ModSounds.STRAW_BED_PLACE);
        Registry.register(BuiltInRegistries.SOUND_EVENT, ModSounds.STRAW_BED_HIT.getLocation(), ModSounds.STRAW_BED_HIT);
        Registry.register(BuiltInRegistries.SOUND_EVENT, ModSounds.STRAW_BED_FALL.getLocation(), ModSounds.STRAW_BED_FALL);
        Registry.register(BuiltInRegistries.SOUND_EVENT, ModSounds.STRAW_BED_BREAK_LEAVE.getLocation(), ModSounds.STRAW_BED_BREAK_LEAVE);
    }

    private static void registerContent() {
        ModBlocks.STRAW_BED = Registry.register(
                BuiltInRegistries.BLOCK, ModBlocks.STRAW_BED_ID, new StrawBedBlock(ModBlocks.strawBedProperties()));
        ModBlocks.STRAW_BED_ITEM = Registry.register(
                BuiltInRegistries.ITEM, ModBlocks.STRAW_BED_ID, new BedItem(ModBlocks.STRAW_BED, new Item.Properties()));

        ItemGroupEvents.modifyEntriesEvent(CreativeModeTabs.FUNCTIONAL_BLOCKS)
                .register(entries -> entries.accept(ModBlocks.STRAW_BED_ITEM));

        FlammableBlockRegistry.getDefaultInstance().add(ModBlocks.STRAW_BED, 60, 20);
    }

    @Override
    public void onInitialize() {
        registerSounds();
        registerContent();
        CommonClass.init();
    }
}
