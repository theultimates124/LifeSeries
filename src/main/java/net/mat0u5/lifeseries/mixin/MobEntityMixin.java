package net.mat0u5.lifeseries.mixin;

import net.minecraft.entity.EntityData;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.world.LocalDifficulty;
import net.minecraft.world.ServerWorldAccess;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(MobEntity.class)
public abstract class MobEntityMixin {
    @Inject(method = "initialize", at = @At("HEAD"))
    private void initialize(ServerWorldAccess world, LocalDifficulty difficulty, SpawnReason spawnReason, @Nullable EntityData entityData, CallbackInfoReturnable<EntityData> cir) {
        if (spawnReason == SpawnReason.NATURAL) return;
        if (spawnReason == SpawnReason.CHUNK_GENERATION) return;
        MobEntity mobEntity = ((MobEntity) (Object) this);
        mobEntity.addCommandTag("notNatural");
    }
}
