package net.mat0u5.lifeseries.mixin;

import net.mat0u5.lifeseries.series.wildlife.WildLife;
import net.mat0u5.lifeseries.series.wildlife.wildcards.WildcardManager;
import net.mat0u5.lifeseries.series.wildlife.wildcards.Wildcards;
import net.mat0u5.lifeseries.series.wildlife.wildcards.wildcard.Hunger;
import net.minecraft.component.ComponentMap;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.text.Text;
import net.minecraft.util.Rarity;
import net.minecraft.component.*;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static net.mat0u5.lifeseries.Main.currentSeries;

@Mixin(value = Item.class, priority = 1)
public abstract class ItemMixin {
    @Accessor("components")
    public abstract ComponentMap normalComponents();

    @Inject(method = "getComponents", at = @At("HEAD"), cancellable = true)
    public void getComponents(CallbackInfoReturnable<ComponentMap> cir) {
        if (currentSeries instanceof WildLife) {
            if (WildcardManager.isActiveWildcard(Wildcards.HUNGER)) {
                Item item = (Item) (Object) this;
                //? if <= 1.21 {
                ComponentMapImpl components = new ComponentMapImpl(normalComponents());
                 //?} else {
                /*MergedComponentMap components = new MergedComponentMap(normalComponents());
                *///?}
                Hunger.applyFoodComponents(item, components);
                //components.set(DataComponentTypes.CUSTOM_NAME, Text.of("testName"));
                cir.setReturnValue(components);
            }
        }
    }
}