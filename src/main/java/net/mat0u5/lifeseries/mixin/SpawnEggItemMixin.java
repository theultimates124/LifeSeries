package net.mat0u5.lifeseries.mixin;

import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.item.SpawnEggItem;
import net.minecraft.util.ActionResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(SpawnEggItem.class)
public abstract class SpawnEggItemMixin {

    @Inject(method = "useOnBlock", at = @At("HEAD"), cancellable = true)
    private void preventSpawnerModification(ItemUsageContext context, CallbackInfoReturnable<ActionResult> cir) {
        // Check if the clicked block is a spawner
        Block block = context.getWorld().getBlockState(context.getBlockPos()).getBlock();
        if (block != Blocks.SPAWNER) return;
        if (context.getPlayer().isCreative()) return;
        // Cancel the action
        cir.setReturnValue(ActionResult.FAIL);
    }
}
