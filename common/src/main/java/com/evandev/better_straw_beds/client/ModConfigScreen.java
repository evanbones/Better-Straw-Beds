package com.evandev.better_straw_beds.client;

import com.evandev.better_straw_beds.config.ModConfig;
import dev.isxander.yacl3.api.ConfigCategory;
import dev.isxander.yacl3.api.Option;
import dev.isxander.yacl3.api.OptionDescription;
import dev.isxander.yacl3.api.YetAnotherConfigLib;
import dev.isxander.yacl3.api.controller.IntegerFieldControllerBuilder;
import dev.isxander.yacl3.api.controller.TickBoxControllerBuilder;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.function.Consumer;
import java.util.function.Supplier;

public class ModConfigScreen {

    public static Screen createScreen(Screen parent) {
        YetAnotherConfigLib.Builder builder = YetAnotherConfigLib.createBuilder()
                .title(Component.translatable("config.better_straw_beds.title"))
                .save(ModConfig::save);

        ConfigCategory.Builder general = ConfigCategory.createBuilder()
                .name(Component.translatable("config.better_straw_beds.category.general"))
                .option(createBoolOption("enabled", true, () -> ModConfig.get().enabled, val -> ModConfig.get().enabled = val))
                .option(createIntOption("max_tracked_spawn_points", 0, 0, 100,
                        () -> ModConfig.get().maxTrackedSpawnPoints, val -> ModConfig.get().maxTrackedSpawnPoints = val))
                .option(createBoolOption("straw_bed_breaks_on_respawn", true,
                        () -> ModConfig.get().strawBedBreaksOnRespawn, val -> ModConfig.get().strawBedBreaksOnRespawn = val))
                .option(createBoolOption("chain_messages", true,
                        () -> ModConfig.get().chainMessages, val -> ModConfig.get().chainMessages = val))
                .option(createBoolOption("straw_bed_breaks_on_sleep", false,
                        () -> ModConfig.get().strawBedBreaksOnSleep, val -> ModConfig.get().strawBedBreaksOnSleep = val))
                .option(createBoolOption("nml_straw_bed_breaks_on_sleep", false,
                        () -> ModConfig.get().nmlStrawBedBreaksOnSleep, val -> ModConfig.get().nmlStrawBedBreaksOnSleep = val));

        return builder.category(general.build()).build().generateScreen(parent);
    }

    private static Option<Boolean> createBoolOption(String name, boolean defaultValue, Supplier<Boolean> getter, Consumer<Boolean> setter) {
        return Option.<Boolean>createBuilder()
                .name(Component.translatable("config.better_straw_beds.option." + name))
                .description(OptionDescription.of(Component.translatable("config.better_straw_beds.option." + name + ".description")))
                .binding(defaultValue, getter, setter)
                .controller(TickBoxControllerBuilder::create)
                .build();
    }

    private static Option<Integer> createIntOption(String name, int defaultValue, int min, int max, Supplier<Integer> getter, Consumer<Integer> setter) {
        return Option.<Integer>createBuilder()
                .name(Component.translatable("config.better_straw_beds.option." + name))
                .description(OptionDescription.of(Component.translatable("config.better_straw_beds.option." + name + ".description")))
                .binding(defaultValue, getter, setter)
                .controller(opt -> IntegerFieldControllerBuilder.create(opt).min(min).max(max))
                .build();
    }
}
