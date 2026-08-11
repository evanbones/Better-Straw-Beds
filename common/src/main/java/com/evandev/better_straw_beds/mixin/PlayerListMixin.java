package com.evandev.better_straw_beds.mixin;

import com.evandev.better_straw_beds.registry.ModSounds;
import com.evandev.better_straw_beds.respawn.PendingBedSoundHolder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.protocol.game.ClientboundSoundPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerList.class)
public abstract class PlayerListMixin {
    @Inject(method = "respawn", at = @At("RETURN"))
    private void better_straw_beds$playPendingBreakSound(
            ServerPlayer player, boolean keepInventory, Entity.RemovalReason reason, CallbackInfoReturnable<ServerPlayer> cir) {
        PendingBedSoundHolder holder = (PendingBedSoundHolder) player;
        ServerLevel level = holder.better_straw_beds$getPendingBreakSoundLevel();
        BlockPos pos = holder.better_straw_beds$getPendingBreakSoundPos();
        holder.better_straw_beds$clearPendingBreakSound();
        if (level == null || pos == null) {
            return;
        }

        ServerPlayer respawned = cir.getReturnValue();
        if (respawned == null) {
            return;
        }

        respawned.connection.send(new ClientboundSoundPacket(
                BuiltInRegistries.SOUND_EVENT.wrapAsHolder(ModSounds.STRAW_BED_BREAK_LEAVE),
                SoundSource.BLOCKS,
                pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5,
                1.0F, 1.0F, level.getRandom().nextLong()));
    }
}
