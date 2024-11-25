package net.mat0u5.lifeseries.mixin;

import net.mat0u5.lifeseries.series.SeriesList;
import net.mat0u5.lifeseries.series.doublelife.DoubleLife;
import net.mat0u5.lifeseries.utils.OtherUtils;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.lang.reflect.Method;

import static net.mat0u5.lifeseries.Main.currentSeries;

@Mixin(PlayerEntity.class)
public abstract class PlayerEntityMixin {

    @Shadow
    public abstract float getAbsorptionAmount();


    @Inject(method = "applyDamage", at = @At("HEAD"))
    private void onApplyDamage(DamageSource source, float amount, CallbackInfo info) {
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
    }
}
