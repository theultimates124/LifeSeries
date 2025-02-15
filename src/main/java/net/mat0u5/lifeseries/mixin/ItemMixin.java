package net.mat0u5.lifeseries.mixin;

import net.mat0u5.lifeseries.Main;
import net.mat0u5.lifeseries.series.SeriesList;
import net.mat0u5.lifeseries.series.wildlife.WildLife;
import net.mat0u5.lifeseries.series.wildlife.wildcards.WildcardManager;
import net.mat0u5.lifeseries.series.wildlife.wildcards.Wildcards;
import net.mat0u5.lifeseries.series.wildlife.wildcards.wildcard.Hunger;
import net.minecraft.component.ComponentMap;
import net.minecraft.item.Item;
import net.minecraft.component.*;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static net.mat0u5.lifeseries.Main.currentSeries;
import static net.mat0u5.lifeseries.MainClient.clientActiveWildcards;
import static net.mat0u5.lifeseries.MainClient.clientCurrentSeries;

@Mixin(value = Item.class, priority = 1)
public abstract class ItemMixin {
    @Accessor("components")
    public abstract ComponentMap normalComponents();

    @Inject(method = "getComponents", at = @At("HEAD"), cancellable = true)
    public void getComponents(CallbackInfoReturnable<ComponentMap> cir) {
        boolean isLogicalSide = Main.isLogicalSide();
        if ((currentSeries instanceof WildLife && isLogicalSide) || (clientCurrentSeries == SeriesList.WILD_LIFE && !isLogicalSide)) {
            if ((WildcardManager.isActiveWildcard(Wildcards.HUNGER) && isLogicalSide) || (clientActiveWildcards.contains(Wildcards.HUNGER) && !isLogicalSide)) {
                Item item = (Item) (Object) this;
                //? if <= 1.21 {
                ComponentMapImpl components = new ComponentMapImpl(normalComponents());
                 //?} else {
                /*MergedComponentMap components = new MergedComponentMap(normalComponents());
                *///?}
                Hunger.applyFoodComponents(item, components);
                cir.setReturnValue(components);
            }
        }
    }
}