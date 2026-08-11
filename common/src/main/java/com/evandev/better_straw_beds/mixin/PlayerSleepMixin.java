package com.evandev.better_straw_beds.mixin;

import com.evandev.better_straw_beds.block.BedBreaker;
import com.evandev.better_straw_beds.block.StrawBedRegistrations;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Player.class)
public abstract class PlayerSleepMixin {
    @Unique
    @Nullable
    private BlockPos better_straw_beds$sleepingPos;

    @Inject(method = "stopSleepInBed", at = @At("HEAD"))
    private void better_straw_beds$captureSleepingPos(boolean wakeImmediately, boolean updateLevelForSleepingPlayers, CallbackInfo ci) {
        Player self = (Player) (Object) this;
        this.better_straw_beds$sleepingPos = self.getSleepingPos().orElse(null);
    }

    @Inject(method = "stopSleepInBed", at = @At("TAIL"))
    private void better_straw_beds$destroyBedOnLeave(boolean wakeImmediately, boolean updateLevelForSleepingPlayers, CallbackInfo ci) {
        BlockPos pos = this.better_straw_beds$sleepingPos;
        this.better_straw_beds$sleepingPos = null;

        Player self = (Player) (Object) this;
        Level level = self.level();
        if (pos == null || level.isClientSide()) {
            return;
        }

        if (StrawBedRegistrations.shouldDestroyOnSleep(level.getBlockState(pos))) {
            BedBreaker.destroyBed(level, pos);
        }
    }
}
