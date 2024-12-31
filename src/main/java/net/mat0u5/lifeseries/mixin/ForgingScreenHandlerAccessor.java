package net.mat0u5.lifeseries.mixin;

import net.minecraft.inventory.CraftingResultInventory;
import net.minecraft.screen.ForgingScreenHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ForgingScreenHandler.class)
public interface ForgingScreenHandlerAccessor {
    @Accessor("output")
    CraftingResultInventory getOutput();
}

