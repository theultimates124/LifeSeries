package net.mat0u5.lifeseries.mixin;

import net.mat0u5.lifeseries.utils.ItemStackUtils;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.screen.AnvilScreenHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Optional;

import static net.mat0u5.lifeseries.Main.blacklist;

@Mixin(AnvilScreenHandler.class)
public abstract class AnvilScreenHandlerMixin {

    @Inject(method = "updateResult", at = @At("TAIL"))
    private void modifyAnvilResultName(CallbackInfo ci) {
        if (blacklist == null) return;
        // Access the parent class's "output" inventory
        ForgingScreenHandlerAccessor accessor = (ForgingScreenHandlerAccessor) (Object) this;
        Inventory outputInventory = accessor.getOutput();

        AnvilScreenHandler self = (AnvilScreenHandler) (Object) this;
        // Get the result item (slot 0 is the output)
        ItemStack resultStack = outputInventory.getStack(0);

        if (resultStack.hasEnchantments()) {
            blacklist.clampEnchantments(resultStack.getEnchantments());
            if (ItemStackUtils.hasCustomComponentEntry(resultStack, "NoMending")) {
                for (it.unimi.dsi.fastutil.objects.Object2IntMap.Entry<RegistryEntry<Enchantment>> enchant : resultStack.getEnchantments().getEnchantmentEntries()) {
                    Optional<RegistryKey<Enchantment>> enchantRegistry = enchant.getKey().getKey();
                    if (enchantRegistry.isEmpty()) continue;
                    if (enchantRegistry.get() == Enchantments.MENDING) {
                        outputInventory.setStack(0, ItemStack.EMPTY);
                    }
                }
            }
        }

    }
}
