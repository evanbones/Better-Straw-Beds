package com.evandev.better_straw_beds.config;

import com.evandev.better_straw_beds.Constants;
import com.evandev.better_straw_beds.platform.Services;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.SerializedName;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class ModConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final File CONFIG_FILE = Services.PLATFORM.getConfigDirectory().resolve(Constants.MOD_ID + ".json").toFile();
    private static ModConfig INSTANCE;

    @SerializedName("enabled")
    public boolean enabled = true;

    @SerializedName("max_tracked_spawn_points")
    public int maxTrackedSpawnPoints = 0;

    @SerializedName("straw_bed_breaks_on_respawn")
    public boolean strawBedBreaksOnRespawn = true;

    @SerializedName("chain_messages")
    public boolean chainMessages = true;

    @SerializedName("straw_bed_breaks_on_sleep")
    public boolean strawBedBreaksOnSleep = false;

    @SerializedName("straw_bed_sets_spawn")
    public boolean strawBedSetsSpawn = true;

    @SerializedName("nml_straw_bed_breaks_on_sleep")
    public boolean nmlStrawBedBreaksOnSleep = false;

    public static ModConfig get() {
        if (INSTANCE == null) {
            load();
        }
        return INSTANCE;
    }

    public static void load() {
        if (CONFIG_FILE.exists()) {
            try (FileReader reader = new FileReader(CONFIG_FILE)) {
                INSTANCE = GSON.fromJson(reader, ModConfig.class);
            } catch (Exception e) {
                Constants.LOG.error("Failed to load " + Constants.MOD_ID + ".json", e);
                INSTANCE = new ModConfig();
                save();
            }

            if (INSTANCE == null) {
                INSTANCE = new ModConfig();
                save();
            }
        } else {
            INSTANCE = new ModConfig();
            save();
        }
    }

    public static void save() {
        try (FileWriter writer = new FileWriter(CONFIG_FILE)) {
            GSON.toJson(INSTANCE, writer);
        } catch (IOException e) {
            Constants.LOG.error("Failed to save " + Constants.MOD_ID + ".json", e);
        }
    }
}
