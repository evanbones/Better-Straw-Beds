package com.evandev.straw_backup_beds.respawn;

import com.evandev.straw_backup_beds.config.ModConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.ListTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

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

    public ListTag save() {
        ListTag list = new ListTag();
        for (SpawnEntry entry : this.entries) {
            list.add(entry.save());
        }
        return list;
    }

    public void load(ListTag list) {
        this.entries.clear();
        for (int i = 0; i < list.size(); i++) {
            SpawnEntry entry = SpawnEntry.load(list.getCompound(i));
            if (entry != null) {
                this.entries.add(entry);
            }
        }
    }

}
