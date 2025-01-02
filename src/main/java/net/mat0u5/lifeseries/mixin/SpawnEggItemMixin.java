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

import static net.mat0u5.lifeseries.Main.seriesConfig;

@Mixin(SpawnEggItem.class)
public abstract class SpawnEggItemMixin {

    @Inject(method = "useOnBlock", at = @At("HEAD"), cancellable = true)
    private void preventSpawnerModification(ItemUsageContext context, CallbackInfoReturnable<ActionResult> cir) {
        if (seriesConfig.getOrCreateBoolean("spawn_egg_allow_on_spawner", false)) return;
        Block block = context.getWorld().getBlockState(context.getBlockPos()).getBlock();
        if (block != Blocks.SPAWNER) return;
        if (context.getPlayer() == null) return;
        if (context.getPlayer().isCreative() && seriesConfig.getOrCreateBoolean("creative_ignore_blacklist", true)) return;
        cir.setReturnValue(ActionResult.FAIL);
    }
}
