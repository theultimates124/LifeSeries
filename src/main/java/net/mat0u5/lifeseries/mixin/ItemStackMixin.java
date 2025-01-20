package net.mat0u5.lifeseries.mixin;

import net.mat0u5.lifeseries.series.wildlife.WildLife;
import net.mat0u5.lifeseries.series.wildlife.wildcards.WildcardManager;
import net.mat0u5.lifeseries.series.wildlife.wildcards.Wildcards;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.FoodComponent;
import net.minecraft.util.ActionResult;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

//? if <= 1.21 {
import net.minecraft.util.TypedActionResult;
//?}

import static net.mat0u5.lifeseries.Main.currentSeries;

@Mixin(ItemStack.class)
public abstract class ItemStackMixin {
    //? if <=1.21 {
    
    @Inject(method = "use", at = @At("HEAD"), cancellable = true)
    private void use(World world, PlayerEntity user, Hand hand, CallbackInfoReturnable<TypedActionResult<ItemStack>> cir) {
        if (currentSeries instanceof WildLife) {
            if (WildcardManager.isActiveWildcard(Wildcards.HUNGER)) {
                ItemStack itemStack = (ItemStack)(Object)this;
                FoodComponent foodComponent = itemStack.get(DataComponentTypes.FOOD);
                if (foodComponent != null) {
                    if (user.canConsume(foodComponent.canAlwaysEat())) {
                        user.setCurrentHand(hand);
                        cir.setReturnValue(TypedActionResult.pass(itemStack));
                    } else {
                        cir.setReturnValue(TypedActionResult.fail(itemStack));
                    }
                }
            }
        }
    }
     //?} else {
    /*@Inject(method = "use", at = @At("HEAD"), cancellable = true)
    private void use(World world, PlayerEntity user, Hand hand, CallbackInfoReturnable<ActionResult> cir) {
        if (currentSeries instanceof WildLife) {
            if (WildcardManager.isActiveWildcard(Wildcards.HUNGER)) {
                ItemStack itemStack = (ItemStack)(Object)this;
                FoodComponent foodComponent = itemStack.get(DataComponentTypes.FOOD);
                if (foodComponent != null) {
                    if (user.canConsume(foodComponent.canAlwaysEat())) {
                        user.setCurrentHand(hand);
                        cir.setReturnValue(ActionResult.CONSUME);
                    } else {
                        cir.setReturnValue(ActionResult.FAIL);
                    }
                }
            }
        }
    }
    *///?}
}