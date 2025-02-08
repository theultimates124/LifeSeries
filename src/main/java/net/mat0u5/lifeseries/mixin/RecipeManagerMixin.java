package net.mat0u5.lifeseries.mixin;

import net.minecraft.resource.ResourceManager;
import net.minecraft.util.profiler.Profiler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

import static net.mat0u5.lifeseries.Main.blacklist;
import static net.mat0u5.lifeseries.Main.seriesConfig;

//? if <=1.21 {
import net.minecraft.recipe.RecipeManager;
import com.google.gson.JsonElement;
import net.minecraft.util.Identifier;
import java.util.ArrayList;
import java.util.Map;
@Mixin(value = RecipeManager.class, priority = 1)
public class RecipeManagerMixin {

    @Inject(method = "apply", at = @At("HEAD"))
    private void applyMixin(Map<Identifier, JsonElement> map, ResourceManager resourceManager, Profiler profiler, CallbackInfo info) {
        if (blacklist == null) return;
        if (blacklist.loadedListItemIdentifier == null)  {
            blacklist.getItemBlacklist();
        }
        if (blacklist.loadedListItemIdentifier.isEmpty()) return;

        List<Identifier> toRemove = new ArrayList<>();

        for (Identifier identifier : map.keySet()) {
            if (blacklist.loadedListItemIdentifier.contains(identifier)) {
                toRemove.add(identifier);
            }
        }
        for (Identifier id : toRemove) {
            map.remove(id);
        }

    }

}
 //?} else {
/*import net.minecraft.recipe.ServerRecipeManager;
import net.minecraft.recipe.PreparedRecipes;
import net.mat0u5.lifeseries.Main;
import net.minecraft.recipe.RecipeEntry;
import org.spongepowered.asm.mixin.Shadow;
@Mixin(ServerRecipeManager.class)
public abstract class RecipeManagerMixin {

    @Shadow
    private PreparedRecipes preparedRecipes;

    @Inject(method = "apply", at = @At("HEAD"), cancellable = true)
    private void applyMixin(PreparedRecipes preparedRecipes, ResourceManager resourceManager, Profiler profiler, CallbackInfo ci) {
        if (blacklist == null) return;
        if (blacklist.loadedListItemIdentifier == null)  {
            blacklist.getItemBlacklist();
        }
        if (blacklist.loadedListItemIdentifier.isEmpty()) return;

        List<RecipeEntry<?>> filteredRecipes = preparedRecipes.recipes().stream()
            .filter(recipe -> !blacklist.loadedListItemIdentifier.contains(recipe.id().getValue()))
            .toList();

        this.preparedRecipes = PreparedRecipes.of(filteredRecipes);

        // Log the updated recipe count
        Main.LOGGER.info("Loaded {} recipes after filtering", filteredRecipes.size());

        // Cancel further processing of the original method
        ci.cancel();

    }

}
*///?}