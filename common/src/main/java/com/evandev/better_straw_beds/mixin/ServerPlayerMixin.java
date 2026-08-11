package com.evandev.better_straw_beds.mixin;

import com.evandev.better_straw_beds.block.BedBreaker;
import com.evandev.better_straw_beds.config.ModConfig;
import com.evandev.better_straw_beds.registry.ModBlocks;
import com.evandev.better_straw_beds.respawn.PendingBedSoundHolder;
import com.evandev.better_straw_beds.respawn.SpawnChain;
import com.evandev.better_straw_beds.respawn.SpawnChainHolder;
import com.evandev.better_straw_beds.respawn.SpawnEntry;
import com.evandev.better_straw_beds.respawn.SpawnPointState;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.RespawnAnchorBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.portal.DimensionTransition;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.List;

@Mixin(ServerPlayer.class)
public abstract class ServerPlayerMixin implements SpawnChainHolder, PendingBedSoundHolder {
    @Unique
    private static final String STRAW_BACKUP_BEDS$CHAIN_KEY = "BetterStrawBedsSpawnChain";

    @Unique
    private final ServerPlayer better_straw_beds$self = (ServerPlayer) (Object) this;

    @Unique
    private final SpawnChain better_straw_beds$chain = new SpawnChain();

    @Unique
    private boolean better_straw_beds$updatingChain;

    @Unique
    @Nullable
    private ServerLevel better_straw_beds$pendingBreakSoundLevel;

    @Unique
    @Nullable
    private BlockPos better_straw_beds$pendingBreakSoundPos;

    @Override
    public SpawnChain better_straw_beds$getSpawnChain() {
        return this.better_straw_beds$chain;
    }

    @Override
    @Nullable
    public ServerLevel better_straw_beds$getPendingBreakSoundLevel() {
        return this.better_straw_beds$pendingBreakSoundLevel;
    }

    @Override
    @Nullable
    public BlockPos better_straw_beds$getPendingBreakSoundPos() {
        return this.better_straw_beds$pendingBreakSoundPos;
    }

    @Override
    public void better_straw_beds$clearPendingBreakSound() {
        this.better_straw_beds$pendingBreakSoundLevel = null;
        this.better_straw_beds$pendingBreakSoundPos = null;
    }

    @Inject(method = "addAdditionalSaveData", at = @At("TAIL"))
    private void better_straw_beds$saveChain(CompoundTag tag, CallbackInfo ci) {
        tag.put(STRAW_BACKUP_BEDS$CHAIN_KEY, this.better_straw_beds$chain.save());
    }

    @Inject(method = "readAdditionalSaveData", at = @At("TAIL"))
    private void better_straw_beds$loadChain(CompoundTag tag, CallbackInfo ci) {
        if (tag.contains(STRAW_BACKUP_BEDS$CHAIN_KEY, Tag.TAG_LIST)) {
            this.better_straw_beds$chain.load(tag.getList(STRAW_BACKUP_BEDS$CHAIN_KEY, Tag.TAG_COMPOUND));
        }
    }

    @Inject(method = "restoreFrom", at = @At("TAIL"))
    private void better_straw_beds$restoreChain(ServerPlayer that, boolean keepEverything, CallbackInfo ci) {
        this.better_straw_beds$chain.copyFrom(((SpawnChainHolder) that).better_straw_beds$getSpawnChain());
    }

    @Inject(method = "setRespawnPosition", at = @At("HEAD"), cancellable = true)
    private void better_straw_beds$preventStrawBedSpawn(
            ResourceKey<Level> dimension, @Nullable BlockPos position, float angle, boolean forced, boolean sendMessage, CallbackInfo ci) {
        ModConfig config = ModConfig.get();
        if (config.strawBedSetsSpawn || position == null || forced || !sendMessage) {
            return;
        }

        ServerLevel level = this.better_straw_beds$self.server.getLevel(dimension);
        if (level != null && ModBlocks.isStrawBed(level.getBlockState(position))) {
            ci.cancel();
        }
    }

    @Inject(method = "setRespawnPosition", at = @At("HEAD"))
    private void better_straw_beds$recordSpawnPoint(
            ResourceKey<Level> dimension, @Nullable BlockPos position, float angle, boolean forced, boolean sendMessage, CallbackInfo ci) {
        if (!ModConfig.get().chainRespawnEnabled || this.better_straw_beds$updatingChain || position == null || forced || !sendMessage) {
            return;
        }

        BlockPos current = this.better_straw_beds$self.getRespawnPosition();
        ResourceKey<Level> currentDimension = this.better_straw_beds$self.getRespawnDimension();
        if (current != null
                && !this.better_straw_beds$self.isRespawnForced()
                && !(current.equals(position) && currentDimension.equals(dimension))) {
            this.better_straw_beds$chain.push(new SpawnEntry(currentDimension, current, this.better_straw_beds$self.getRespawnAngle()));
        }

        this.better_straw_beds$chain.remove(dimension, position);
    }

    @Inject(method = "findRespawnPositionAndUseSpawnBlock", at = @At("HEAD"))
    private void better_straw_beds$fallBackToPreviousSpawn(
            boolean keepInventory, DimensionTransition.PostDimensionTransition postTransition, CallbackInfoReturnable<DimensionTransition> cir) {
        if (!ModConfig.get().chainRespawnEnabled) {
            return;
        }

        this.better_straw_beds$advanceToUsableSpawnPoint();
    }

    @Inject(method = "findRespawnPositionAndUseSpawnBlock", at = @At("RETURN"))
    private void better_straw_beds$consumeStrawBed(
            boolean keepInventory, DimensionTransition.PostDimensionTransition postTransition, CallbackInfoReturnable<DimensionTransition> cir) {
        ModConfig config = ModConfig.get();
        if (!config.strawBedBreaksOnRespawn || keepInventory) {
            return;
        }

        DimensionTransition transition = cir.getReturnValue();
        if (transition == null || transition.missingRespawnBlock()) {
            return;
        }

        BlockPos pos = this.better_straw_beds$self.getRespawnPosition();
        if (pos == null || this.better_straw_beds$self.isRespawnForced()) {
            return;
        }

        ServerLevel level = this.better_straw_beds$self.server.getLevel(this.better_straw_beds$self.getRespawnDimension());
        if (level == null || !ModBlocks.isStrawBed(level.getBlockState(pos))) {
            return;
        }

        BedBreaker.destroyBed(level, pos, false);
        this.better_straw_beds$pendingBreakSoundLevel = level;
        this.better_straw_beds$pendingBreakSoundPos = pos;

        boolean hasBackup = this.better_straw_beds$advanceToUsableSpawnPoint();
        if (config.chainMessages) {
            this.better_straw_beds$self.sendSystemMessage(Component.translatable(
                    hasBackup ? "message.better_straw_beds.chain_fallback" : "message.better_straw_beds.chain_empty"));
        }
    }

    @Unique
    private boolean better_straw_beds$advanceToUsableSpawnPoint() {
        ServerPlayer self = this.better_straw_beds$self;
        if (self.isRespawnForced()) {
            return true;
        }

        List<SpawnEntry> blockedForNow = new ArrayList<>();
        boolean found = true;

        SpawnPointState state;
        while ((state = this.better_straw_beds$checkSpawnPoint(self.getRespawnDimension(), self.getRespawnPosition(), self.getRespawnAngle()))
                != SpawnPointState.USABLE) {
            if (state == SpawnPointState.BLOCKED) {
                blockedForNow.add(new SpawnEntry(self.getRespawnDimension(), self.getRespawnPosition(), self.getRespawnAngle()));
            }

            SpawnEntry previous = this.better_straw_beds$chain.pop();
            this.better_straw_beds$updatingChain = true;
            try {
                if (previous == null) {
                    self.setRespawnPosition(Level.OVERWORLD, null, 0.0F, false, false);
                    found = false;
                    break;
                }

                self.setRespawnPosition(previous.dimension(), previous.pos(), previous.angle(), false, false);
            } finally {
                this.better_straw_beds$updatingChain = false;
            }
        }

        for (int i = blockedForNow.size() - 1; i >= 0; i--) {
            this.better_straw_beds$chain.push(blockedForNow.get(i));
        }

        return found;
    }

    @Unique
    private SpawnPointState better_straw_beds$checkSpawnPoint(ResourceKey<Level> dimension, @Nullable BlockPos pos, float angle) {
        if (pos == null) {
            return SpawnPointState.GONE;
        }

        ServerLevel level = this.better_straw_beds$self.server.getLevel(dimension);
        if (level == null) {
            return SpawnPointState.BLOCKED;
        }

        BlockState state = level.getBlockState(pos);
        if (state.getBlock() instanceof RespawnAnchorBlock) {
            if (state.getValue(RespawnAnchorBlock.CHARGE) <= 0 || !RespawnAnchorBlock.canSetSpawn(level)) {
                return SpawnPointState.GONE;
            }

            return RespawnAnchorBlock.findStandUpPosition(EntityType.PLAYER, level, pos).isPresent()
                    ? SpawnPointState.USABLE
                    : SpawnPointState.BLOCKED;
        }

        if (state.getBlock() instanceof BedBlock) {
            if (!BedBlock.canSetSpawn(level)) {
                return SpawnPointState.GONE;
            }

            return BedBlock.findStandUpPosition(EntityType.PLAYER, level, pos, state.getValue(BedBlock.FACING), angle).isPresent()
                    ? SpawnPointState.USABLE
                    : SpawnPointState.BLOCKED;
        }

        return SpawnPointState.GONE;
    }
}
