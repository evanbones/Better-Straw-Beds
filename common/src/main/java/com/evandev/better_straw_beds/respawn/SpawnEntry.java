package com.evandev.better_straw_beds.respawn;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;

import org.jetbrains.annotations.Nullable;

public record SpawnEntry(ResourceKey<Level> dimension, BlockPos pos, float angle) {

    public boolean isAt(ResourceKey<Level> dimension, @Nullable BlockPos pos) {
        return this.pos.equals(pos) && this.dimension.equals(dimension);
    }

    public CompoundTag save() {
        CompoundTag tag = new CompoundTag();
        tag.putString("dimension", this.dimension.location().toString());
        tag.put("pos", NbtUtils.writeBlockPos(this.pos));
        tag.putFloat("angle", this.angle);
        return tag;
    }

    @Nullable
    public static SpawnEntry load(CompoundTag tag) {
        ResourceLocation dimension = ResourceLocation.tryParse(tag.getString("dimension"));
        if (dimension == null) {
            return null;
        }

        return NbtUtils.readBlockPos(tag, "pos")
                .map(pos -> new SpawnEntry(ResourceKey.create(Registries.DIMENSION, dimension), pos, tag.getFloat("angle")))
                .orElse(null);
    }
}
