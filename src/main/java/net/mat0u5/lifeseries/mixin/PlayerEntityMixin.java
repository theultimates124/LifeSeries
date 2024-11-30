package net.mat0u5.lifeseries.mixin;

import net.mat0u5.lifeseries.series.SeriesList;
import net.mat0u5.lifeseries.series.doublelife.DoubleLife;
import net.mat0u5.lifeseries.utils.OtherUtils;
import net.minecraft.component.type.FoodComponent;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.lang.reflect.Method;

import static net.mat0u5.lifeseries.Main.currentSeries;

@Mixin(PlayerEntity.class)
public abstract class PlayerEntityMixin {

    @Shadow
    public abstract float getAbsorptionAmount();


    @Inject(method = "applyDamage", at = @At("HEAD"))
    private void onApplyDamage(DamageSource source, float amount, CallbackInfo info) {
        /*
        Disabled for now, Reflection is not the way......
        try {
            Method applyArmorToDamage = LivingEntity.class.getDeclaredMethod("applyArmorToDamage", DamageSource.class, float.class);
            Method modifyAppliedDamage = LivingEntity.class.getDeclaredMethod("modifyAppliedDamage", DamageSource.class, float.class);
            applyArmorToDamage.setAccessible(true);
            modifyAppliedDamage.setAccessible(true);


            float adjustedAmount = (float) applyArmorToDamage.invoke(this, source, amount);
            adjustedAmount = (float) modifyAppliedDamage.invoke(this, source, adjustedAmount);
            float actualDamage = Math.max(adjustedAmount - this.getAbsorptionAmount(), 0.0F);

            if (actualDamage == 0.0F) return;
            PlayerEntity player = (PlayerEntity) (Object) this;
            currentSeries.onPlayerDamage((ServerPlayerEntity) player, source, actualDamage);
        } catch (ReflectiveOperationException e) {
            e.printStackTrace(); // Log any reflection issues
        }
        */
    }

    @Inject(method = "canFoodHeal", at = @At("HEAD"), cancellable = true)
    private void canFoodHeal(CallbackInfoReturnable<Boolean> cir) {
        if (currentSeries.getSeries() != SeriesList.DOUBLE_LIFE) return;

        PlayerEntity player = (PlayerEntity) (Object) this;
        ServerPlayerEntity serverPlayer = (ServerPlayerEntity) player;
        boolean orig =  player.getHealth() > 0.0F && player.getHealth() < player.getMaxHealth();
        if (!orig) {
            cir.setReturnValue(false);
            return;
        }

        DoubleLife doubleLife = ((DoubleLife) currentSeries);
        if (!doubleLife.hasSoulmate(serverPlayer)) return;
        if (!doubleLife.isSoulmateOnline(serverPlayer)) return;
        if (doubleLife.isMainSoulmate(serverPlayer)) return;
        ServerPlayerEntity soulmate = doubleLife.getSoulmate(serverPlayer);
        if (soulmate == null) return;
        if (soulmate.isDead()) return;

        boolean canHealWithSaturation =  player.getHungerManager().getSaturationLevel() > 0.0F && player.getHungerManager().getFoodLevel() >= 20;
        boolean canHealWithSaturationOther =  soulmate.getHungerManager().getSaturationLevel() > 0.0F && soulmate.getHungerManager().getFoodLevel() >= 20;

        if (canHealWithSaturation && canHealWithSaturationOther) {
            cir.setReturnValue(false);
        }
    }
}
