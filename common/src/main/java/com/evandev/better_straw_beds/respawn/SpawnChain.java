package com.evandev.better_straw_beds.respawn;

import com.evandev.better_straw_beds.config.ModConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

import org.jetbrains.annotations.Nullable;
import java.util.ArrayList;
import java.util.List;

public final class SpawnChain {
    private final List<SpawnEntry> entries = new ArrayList<>();

    public boolean isEmpty() {
        return this.entries.isEmpty();
    }

    public int size() {
        return this.entries.size();
    }

    public void push(SpawnEntry entry) {
        this.remove(entry.dimension(), entry.pos());
        this.entries.add(entry);
        this.trim();
    }

    @Nullable
    public SpawnEntry pop() {
        return this.entries.isEmpty() ? null : this.entries.removeLast();
    }

    public void remove(ResourceKey<Level> dimension, @Nullable BlockPos pos) {
        this.entries.removeIf(entry -> entry.isAt(dimension, pos));
    }

    public void copyFrom(SpawnChain other) {
        this.entries.clear();
        this.entries.addAll(other.entries);
    }

    private void trim() {
        int max = ModConfig.get().maxTrackedSpawnPoints;
        if (max <= 0) {
            return;
        }

        while (this.entries.size() > max) {
            this.entries.removeFirst();
        }
    }

    public void save(ValueOutput output, String key) {
        if (this.entries.isEmpty()) {
            return;
        }

        ValueOutput.TypedOutputList<SpawnEntry> list = output.list(key, SpawnEntry.CODEC);
        for (SpawnEntry entry : this.entries) {
            list.add(entry);
        }
    }

    public void load(ValueInput input, String key) {
        this.entries.clear();
        for (SpawnEntry entry : input.listOrEmpty(key, SpawnEntry.CODEC)) {
            this.entries.add(entry);
        }
    }
}
