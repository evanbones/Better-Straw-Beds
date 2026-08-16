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
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.attribute.EnvironmentAttributes;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.RespawnAnchorBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.portal.TeleportTransition;
import net.minecraft.world.level.storage.LevelData;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
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
    private void better_straw_beds$saveChain(ValueOutput output, CallbackInfo ci) {
        this.better_straw_beds$chain.save(output, STRAW_BACKUP_BEDS$CHAIN_KEY);
    }

    @Inject(method = "readAdditionalSaveData", at = @At("TAIL"))
    private void better_straw_beds$loadChain(ValueInput input, CallbackInfo ci) {
        this.better_straw_beds$chain.load(input, STRAW_BACKUP_BEDS$CHAIN_KEY);
    }

    @Inject(method = "restoreFrom", at = @At("TAIL"))
    private void better_straw_beds$restoreChain(ServerPlayer that, boolean restoreAll, CallbackInfo ci) {
        this.better_straw_beds$chain.copyFrom(((SpawnChainHolder) that).better_straw_beds$getSpawnChain());
    }

    @Inject(method = "setRespawnPosition", at = @At("HEAD"), cancellable = true)
    private void better_straw_beds$preventStrawBedSpawn(
            ServerPlayer.@Nullable RespawnConfig respawnConfig, boolean showMessage, CallbackInfo ci) {
        ModConfig config = ModConfig.get();
        if (config.strawBedSetsSpawn || respawnConfig == null || respawnConfig.forced() || !showMessage) {
            return;
        }

        LevelData.RespawnData data = respawnConfig.respawnData();
        ServerLevel level = this.better_straw_beds$self.level().getServer().getLevel(data.dimension());
        if (level != null && ModBlocks.isStrawBed(level.getBlockState(data.pos()))) {
            ci.cancel();
        }
    }

    @Inject(method = "setRespawnPosition", at = @At("HEAD"))
    private void better_straw_beds$recordSpawnPoint(
            ServerPlayer.@Nullable RespawnConfig respawnConfig, boolean showMessage, CallbackInfo ci) {
        if (!ModConfig.get().chainRespawnEnabled || this.better_straw_beds$updatingChain || respawnConfig == null
                || respawnConfig.forced() || !showMessage) {
            return;
        }

        ServerPlayer.RespawnConfig current = this.better_straw_beds$self.getRespawnConfig();
        if (current != null && !current.forced() && !current.isSamePosition(respawnConfig)) {
            LevelData.RespawnData currentData = current.respawnData();
            this.better_straw_beds$chain.push(new SpawnEntry(currentData.dimension(), currentData.pos(), currentData.yaw()));
        }

        LevelData.RespawnData data = respawnConfig.respawnData();
        this.better_straw_beds$chain.remove(data.dimension(), data.pos());
    }

    @Inject(method = "findRespawnPositionAndUseSpawnBlock", at = @At("HEAD"))
    private void better_straw_beds$fallBackToPreviousSpawn(
            boolean consumeSpawnBlock, TeleportTransition.PostTeleportTransition postTeleportTransition,
            CallbackInfoReturnable<TeleportTransition> cir) {
        if (!ModConfig.get().chainRespawnEnabled) {
            return;
        }

        this.better_straw_beds$advanceToUsableSpawnPoint();
    }

    @Inject(method = "findRespawnPositionAndUseSpawnBlock", at = @At("RETURN"))
    private void better_straw_beds$consumeStrawBed(
            boolean consumeSpawnBlock, TeleportTransition.PostTeleportTransition postTeleportTransition,
            CallbackInfoReturnable<TeleportTransition> cir) {
        ModConfig config = ModConfig.get();
        if (!config.strawBedBreaksOnRespawn || !consumeSpawnBlock) {
            return;
        }

        TeleportTransition transition = cir.getReturnValue();
        if (transition == null || transition.missingRespawnBlock()) {
            return;
        }

        ServerPlayer.RespawnConfig respawnConfig = this.better_straw_beds$self.getRespawnConfig();
        if (respawnConfig == null || respawnConfig.forced()) {
            return;
        }

        BlockPos pos = respawnConfig.respawnData().pos();
        ServerLevel level = this.better_straw_beds$self.level().getServer().getLevel(respawnConfig.respawnData().dimension());
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
        ServerPlayer.RespawnConfig initial = self.getRespawnConfig();
        if (initial != null && initial.forced()) {
            return true;
        }

        List<SpawnEntry> blockedForNow = new ArrayList<>();
        boolean found = true;

        SpawnPointState state;
        while (true) {
            ServerPlayer.RespawnConfig config = self.getRespawnConfig();
            ResourceKey<Level> dimension = config != null ? config.respawnData().dimension() : Level.OVERWORLD;
            BlockPos pos = config != null ? config.respawnData().pos() : null;
            float angle = config != null ? config.respawnData().yaw() : 0.0F;

            state = this.better_straw_beds$checkSpawnPoint(dimension, pos, angle);
            if (state == SpawnPointState.USABLE) {
                break;
            }

            if (state == SpawnPointState.BLOCKED && pos != null) {
                blockedForNow.add(new SpawnEntry(dimension, pos, angle));
            }

            SpawnEntry previous = this.better_straw_beds$chain.pop();
            this.better_straw_beds$updatingChain = true;
            try {
                if (previous == null) {
                    self.setRespawnPosition(null, false);
                    found = false;
                    break;
                }

                self.setRespawnPosition(
                        new ServerPlayer.RespawnConfig(
                                LevelData.RespawnData.of(previous.dimension(), previous.pos(), previous.angle(), 0.0F), false),
                        false);
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

        ServerLevel level = this.better_straw_beds$self.level().getServer().getLevel(dimension);
        if (level == null) {
            return SpawnPointState.BLOCKED;
        }

        BlockState state = level.getBlockState(pos);
        if (state.getBlock() instanceof RespawnAnchorBlock) {
            if (state.getValue(RespawnAnchorBlock.CHARGE) <= 0 || !RespawnAnchorBlock.canSetSpawn(level, pos)) {
                return SpawnPointState.GONE;
            }

            return RespawnAnchorBlock.findStandUpPosition(EntityType.PLAYER, level, pos).isPresent()
                    ? SpawnPointState.USABLE
                    : SpawnPointState.BLOCKED;
        }

        if (state.getBlock() instanceof BedBlock) {
            if (!level.environmentAttributes().getValue(EnvironmentAttributes.BED_RULE, pos).canSetSpawn(level)) {
                return SpawnPointState.GONE;
            }

            return BedBlock.findStandUpPosition(EntityType.PLAYER, level, pos, state.getValue(BedBlock.FACING), angle).isPresent()
                    ? SpawnPointState.USABLE
                    : SpawnPointState.BLOCKED;
        }

        return SpawnPointState.GONE;
    }
}
