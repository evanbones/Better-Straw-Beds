package com.evandev.better_straw_beds;

import com.evandev.better_straw_beds.block.StrawBedBlock;
import com.evandev.better_straw_beds.client.ClientConfigSetup;
import com.evandev.better_straw_beds.registry.ModBlocks;
import com.evandev.better_straw_beds.registry.ModSounds;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.item.BedItem;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.FireBlock;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

@Mod(Constants.MOD_ID)
public class BetterStrawBeds {
    private static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(Constants.MOD_ID);
    private static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(Constants.MOD_ID);
    private static final DeferredRegister<SoundEvent> SOUNDS = DeferredRegister.create(Registries.SOUND_EVENT, Constants.MOD_ID);

    static {
        SOUNDS.register("block.straw_bed.break", () -> ModSounds.STRAW_BED_BREAK);
        SOUNDS.register("block.straw_bed.step", () -> ModSounds.STRAW_BED_STEP);
        SOUNDS.register("block.straw_bed.place", () -> ModSounds.STRAW_BED_PLACE);
        SOUNDS.register("block.straw_bed.hit", () -> ModSounds.STRAW_BED_HIT);
        SOUNDS.register("block.straw_bed.fall", () -> ModSounds.STRAW_BED_FALL);
        SOUNDS.register("block.straw_bed.break_leave", () -> ModSounds.STRAW_BED_BREAK_LEAVE);
    }

    private static final DeferredBlock<Block> STRAW_BED =
            BLOCKS.register("straw_bed", () -> new StrawBedBlock(ModBlocks.strawBedProperties()));
    private static final DeferredItem<BedItem> STRAW_BED_ITEM =
            ITEMS.register("straw_bed", () -> new BedItem(STRAW_BED.get(), new Item.Properties()
                    .setId(ResourceKey.create(Registries.ITEM, ModBlocks.STRAW_BED_ID))));

    public BetterStrawBeds(IEventBus modEventBus, ModContainer modContainer) {
        SOUNDS.register(modEventBus);
        BLOCKS.register(modEventBus);
        ITEMS.register(modEventBus);

        modEventBus.addListener(this::commonSetup);
        modEventBus.addListener(this::addCreativeTabContents);

        if (FMLEnvironment.getDist().isClient()) {
            ClientConfigSetup.register(modContainer);
        }
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        ModBlocks.STRAW_BED = STRAW_BED.get();
        ModBlocks.STRAW_BED_ITEM = STRAW_BED_ITEM.get();

        CommonClass.init();

        event.enqueueWork(() -> ((FireBlock) Blocks.FIRE).setFlammable(ModBlocks.STRAW_BED, 60, 20));
    }

    private void addCreativeTabContents(final BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.FUNCTIONAL_BLOCKS) {
            event.accept(STRAW_BED_ITEM);
        }
    }
}
