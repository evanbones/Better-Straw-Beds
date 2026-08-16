package com.evandev.better_straw_beds.respawn;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

import org.jetbrains.annotations.Nullable;

public record SpawnEntry(ResourceKey<Level> dimension, BlockPos pos, float angle) {
    public static final Codec<SpawnEntry> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ResourceKey.codec(Registries.DIMENSION).fieldOf("dimension").forGetter(SpawnEntry::dimension),
            BlockPos.CODEC.fieldOf("pos").forGetter(SpawnEntry::pos),
            Codec.FLOAT.fieldOf("angle").forGetter(SpawnEntry::angle)
    ).apply(instance, SpawnEntry::new));

    public boolean isAt(ResourceKey<Level> dimension, @Nullable BlockPos pos) {
        return this.pos.equals(pos) && this.dimension.equals(dimension);
    }
}
