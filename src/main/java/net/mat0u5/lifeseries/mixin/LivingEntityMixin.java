package net.mat0u5.lifeseries.mixin;

import net.mat0u5.lifeseries.Main;
import net.mat0u5.lifeseries.utils.ItemStackUtils;
import net.mat0u5.lifeseries.utils.OtherUtils;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static net.mat0u5.lifeseries.Main.currentSeries;

@Mixin(value = LivingEntity.class, priority = 1)
public abstract class LivingEntityMixin {
    @Inject(method = "heal", at = @At("HEAD"), cancellable = true)
    private void onHealHead(float amount, CallbackInfo info) {
        if (!Main.isLogicalSide())return;
        if (!currentSeries.NO_HEALING) return;

        LivingEntity entity = (LivingEntity) (Object) this;
        if (entity instanceof ServerPlayerEntity player) {
            info.cancel();
        }
    }

    @Inject(method = "heal", at = @At("TAIL"))
    private void onHeal(float amount, CallbackInfo info) {
        if (!Main.isLogicalSide()) return;
        LivingEntity entity = (LivingEntity) (Object) this;
        if (entity instanceof ServerPlayerEntity player) {
            currentSeries.onPlayerHeal(player, amount);
        }
    }

    @Inject(method = "damage", at = @At("HEAD"), cancellable = true)
    //? if <= 1.21 {
    public void damage(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
     //?} else
    /*public void damage(ServerWorld world, DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {*/
        if (!Main.isLogicalSide()) return;
        ItemStack weapon = source.getWeaponStack();
        if (amount <= 2) return;
        if (weapon == null) return;
        if (weapon.isEmpty()) return;
        if (!weapon.isOf(Items.MACE)) return;
        if (!ItemStackUtils.hasCustomComponentEntry(weapon, "WindChargeSuperpower")) return;
        LivingEntity entity = (LivingEntity) (Object) this;
        //? if <= 1.21 {
        cir.setReturnValue(entity.damage(source, 2));
         //?} else
        /*cir.setReturnValue(entity.damage(world, source, 2));*/
    }
}
