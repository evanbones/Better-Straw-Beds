package com.evandev.straw_backup_beds.mixin;

import com.evandev.straw_backup_beds.block.BedBreaker;
import com.evandev.straw_backup_beds.config.ModConfig;
import com.evandev.straw_backup_beds.registry.ModBlocks;
import com.evandev.straw_backup_beds.respawn.PendingBedSoundHolder;
import com.evandev.straw_backup_beds.respawn.SpawnChain;
import com.evandev.straw_backup_beds.respawn.SpawnChainHolder;
import com.evandev.straw_backup_beds.respawn.SpawnEntry;
import com.evandev.straw_backup_beds.respawn.SpawnPointState;
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
    private static final String STRAW_BACKUP_BEDS$CHAIN_KEY = "StrawBackupBedsSpawnChain";

    @Unique
    private final ServerPlayer straw_backup_beds$self = (ServerPlayer) (Object) this;

    @Unique
    private final SpawnChain straw_backup_beds$chain = new SpawnChain();

    @Unique
    private boolean straw_backup_beds$updatingChain;

    @Unique
    @Nullable
    private ServerLevel straw_backup_beds$pendingBreakSoundLevel;

    @Unique
    @Nullable
    private BlockPos straw_backup_beds$pendingBreakSoundPos;

    @Override
    public SpawnChain straw_backup_beds$getSpawnChain() {
        return this.straw_backup_beds$chain;
    }

    @Override
    @Nullable
    public ServerLevel straw_backup_beds$getPendingBreakSoundLevel() {
        return this.straw_backup_beds$pendingBreakSoundLevel;
    }

    @Override
    @Nullable
    public BlockPos straw_backup_beds$getPendingBreakSoundPos() {
        return this.straw_backup_beds$pendingBreakSoundPos;
    }

    @Override
    public void straw_backup_beds$clearPendingBreakSound() {
        this.straw_backup_beds$pendingBreakSoundLevel = null;
        this.straw_backup_beds$pendingBreakSoundPos = null;
    }

    @Inject(method = "addAdditionalSaveData", at = @At("TAIL"))
    private void straw_backup_beds$saveChain(CompoundTag tag, CallbackInfo ci) {
        tag.put(STRAW_BACKUP_BEDS$CHAIN_KEY, this.straw_backup_beds$chain.save());
    }

    @Inject(method = "readAdditionalSaveData", at = @At("TAIL"))
    private void straw_backup_beds$loadChain(CompoundTag tag, CallbackInfo ci) {
        if (tag.contains(STRAW_BACKUP_BEDS$CHAIN_KEY, Tag.TAG_LIST)) {
            this.straw_backup_beds$chain.load(tag.getList(STRAW_BACKUP_BEDS$CHAIN_KEY, Tag.TAG_COMPOUND));
        }
    }

    @Inject(method = "restoreFrom", at = @At("TAIL"))
    private void straw_backup_beds$restoreChain(ServerPlayer that, boolean keepEverything, CallbackInfo ci) {
        this.straw_backup_beds$chain.copyFrom(((SpawnChainHolder) that).straw_backup_beds$getSpawnChain());
    }

    @Inject(method = "setRespawnPosition", at = @At("HEAD"))
    private void straw_backup_beds$recordSpawnPoint(
            ResourceKey<Level> dimension, @Nullable BlockPos position, float angle, boolean forced, boolean sendMessage, CallbackInfo ci) {
        if (!ModConfig.get().enabled || this.straw_backup_beds$updatingChain || position == null || forced || !sendMessage) {
            return;
        }

        BlockPos current = this.straw_backup_beds$self.getRespawnPosition();
        ResourceKey<Level> currentDimension = this.straw_backup_beds$self.getRespawnDimension();
        if (current != null
                && !this.straw_backup_beds$self.isRespawnForced()
                && !(current.equals(position) && currentDimension.equals(dimension))) {
            this.straw_backup_beds$chain.push(new SpawnEntry(currentDimension, current, this.straw_backup_beds$self.getRespawnAngle()));
        }

        this.straw_backup_beds$chain.remove(dimension, position);
    }

    @Inject(method = "findRespawnPositionAndUseSpawnBlock", at = @At("HEAD"))
    private void straw_backup_beds$fallBackToPreviousSpawn(
            boolean keepInventory, DimensionTransition.PostDimensionTransition postTransition, CallbackInfoReturnable<DimensionTransition> cir) {
        if (!ModConfig.get().enabled) {
            return;
        }

        this.straw_backup_beds$advanceToUsableSpawnPoint();
    }

    @Inject(method = "findRespawnPositionAndUseSpawnBlock", at = @At("RETURN"))
    private void straw_backup_beds$consumeStrawBed(
            boolean keepInventory, DimensionTransition.PostDimensionTransition postTransition, CallbackInfoReturnable<DimensionTransition> cir) {
        ModConfig config = ModConfig.get();
        if (!config.enabled || !config.strawBedBreaksOnRespawn || keepInventory) {
            return;
        }

        DimensionTransition transition = cir.getReturnValue();
        if (transition == null || transition.missingRespawnBlock()) {
            return;
        }

        BlockPos pos = this.straw_backup_beds$self.getRespawnPosition();
        if (pos == null || this.straw_backup_beds$self.isRespawnForced()) {
            return;
        }

        ServerLevel level = this.straw_backup_beds$self.server.getLevel(this.straw_backup_beds$self.getRespawnDimension());
        if (level == null || !ModBlocks.isStrawBed(level.getBlockState(pos))) {
            return;
        }

        BedBreaker.destroyBed(level, pos, false);
        this.straw_backup_beds$pendingBreakSoundLevel = level;
        this.straw_backup_beds$pendingBreakSoundPos = pos;

        boolean hasBackup = this.straw_backup_beds$advanceToUsableSpawnPoint();
        if (config.chainMessages) {
            this.straw_backup_beds$self.sendSystemMessage(Component.translatable(
                    hasBackup ? "message.straw_backup_beds.chain_fallback" : "message.straw_backup_beds.chain_empty"));
        }
    }

    @Unique
    private boolean straw_backup_beds$advanceToUsableSpawnPoint() {
        ServerPlayer self = this.straw_backup_beds$self;
        if (self.isRespawnForced()) {
            return true;
        }

        List<SpawnEntry> blockedForNow = new ArrayList<>();
        boolean found = true;

        SpawnPointState state;
        while ((state = this.straw_backup_beds$checkSpawnPoint(self.getRespawnDimension(), self.getRespawnPosition(), self.getRespawnAngle()))
                != SpawnPointState.USABLE) {
            if (state == SpawnPointState.BLOCKED) {
                blockedForNow.add(new SpawnEntry(self.getRespawnDimension(), self.getRespawnPosition(), self.getRespawnAngle()));
            }

            SpawnEntry previous = this.straw_backup_beds$chain.pop();
            this.straw_backup_beds$updatingChain = true;
            try {
                if (previous == null) {
                    self.setRespawnPosition(Level.OVERWORLD, null, 0.0F, false, false);
                    found = false;
                    break;
                }

                self.setRespawnPosition(previous.dimension(), previous.pos(), previous.angle(), false, false);
            } finally {
                this.straw_backup_beds$updatingChain = false;
            }
        }

        for (int i = blockedForNow.size() - 1; i >= 0; i--) {
            this.straw_backup_beds$chain.push(blockedForNow.get(i));
        }

        return found;
    }

    @Unique
    private SpawnPointState straw_backup_beds$checkSpawnPoint(ResourceKey<Level> dimension, @Nullable BlockPos pos, float angle) {
        if (pos == null) {
            return SpawnPointState.GONE;
        }

        ServerLevel level = this.straw_backup_beds$self.server.getLevel(dimension);
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
