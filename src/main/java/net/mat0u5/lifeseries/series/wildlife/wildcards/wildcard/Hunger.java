package net.mat0u5.lifeseries.series.wildlife.wildcards.wildcard;

import net.mat0u5.lifeseries.events.Events;
import net.mat0u5.lifeseries.mixin.ItemMixin;
import net.mat0u5.lifeseries.network.NetworkHandlerServer;
import net.mat0u5.lifeseries.series.wildlife.wildcards.Wildcard;
import net.mat0u5.lifeseries.series.wildlife.wildcards.WildcardManager;
import net.mat0u5.lifeseries.series.wildlife.wildcards.Wildcards;
import net.mat0u5.lifeseries.utils.ItemStackUtils;
import net.mat0u5.lifeseries.utils.OtherUtils;
import net.mat0u5.lifeseries.utils.PlayerUtils;
import net.mat0u5.lifeseries.utils.TaskScheduler;
import net.minecraft.component.*;
import net.minecraft.component.type.FoodComponent;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;

import java.util.*;

import static net.mat0u5.lifeseries.Main.currentSeries;
import static net.mat0u5.lifeseries.Main.currentSession;


//? if >= 1.21.2 {
/*import net.minecraft.component.type.ConsumableComponent;
import net.minecraft.item.consume.ApplyEffectsConsumeEffect;
import net.minecraft.item.consume.ConsumeEffect;
import net.minecraft.item.consume.UseAction;
*///?}

public class Hunger extends Wildcard {
    private static Random rnd = new Random();
    public static int SWITCH_DELAY = 36000;
    public static int shuffleVersion = 0;
    private static boolean shuffledBefore = false;
    private static int lastVersion = -1;

    private static final List<RegistryEntry<StatusEffect>> effects = List.of(
            StatusEffects.SPEED,
            StatusEffects.SLOWNESS,
            StatusEffects.HASTE,
            StatusEffects.MINING_FATIGUE,
            StatusEffects.STRENGTH,
            StatusEffects.INSTANT_HEALTH,
            StatusEffects.INSTANT_DAMAGE,
            StatusEffects.JUMP_BOOST,
            StatusEffects.NAUSEA,
            StatusEffects.REGENERATION,
            StatusEffects.RESISTANCE,
            StatusEffects.FIRE_RESISTANCE,
            StatusEffects.WATER_BREATHING,
            StatusEffects.INVISIBILITY,
            StatusEffects.BLINDNESS,
            StatusEffects.NIGHT_VISION,
            StatusEffects.WEAKNESS,
            StatusEffects.POISON,
            StatusEffects.WITHER,
            StatusEffects.HEALTH_BOOST,
            StatusEffects.ABSORPTION,
            StatusEffects.SATURATION,
            StatusEffects.GLOWING,
            StatusEffects.LEVITATION,
            StatusEffects.LUCK,
            StatusEffects.UNLUCK,
            StatusEffects.SLOW_FALLING,
            StatusEffects.CONDUIT_POWER,
            StatusEffects.DOLPHINS_GRACE,
            StatusEffects.HERO_OF_THE_VILLAGE,
            StatusEffects.DARKNESS,
            StatusEffects.WIND_CHARGED,
            StatusEffects.WEAVING,
            StatusEffects.OOZING,
            StatusEffects.INFESTED
    );

    private static final List<RegistryEntry<StatusEffect>> levelLimit = List.of(
            StatusEffects.STRENGTH,
            StatusEffects.INSTANT_HEALTH,
            StatusEffects.INSTANT_DAMAGE,
            StatusEffects.REGENERATION,
            StatusEffects.RESISTANCE,
            StatusEffects.WITHER,
            StatusEffects.ABSORPTION,
            StatusEffects.SATURATION
    );

    private static final List<RegistryEntry<StatusEffect>> durationLimit = List.of(
            StatusEffects.INSTANT_HEALTH,
            StatusEffects.INSTANT_DAMAGE,
            StatusEffects.SATURATION
    );

    @Override
    public Wildcards getType() {
        return Wildcards.HUNGER;
    }

    @Override
    public void tick() {
        if (currentSession.sessionLength == null || currentSession.sessionLength - currentSession.passedTime > 6000) {
            int currentVersion = (int) Math.floor(currentSession.passedTime / (double) SWITCH_DELAY);
            if (lastVersion != currentVersion) {
                lastVersion = currentVersion;
                newFoodRules();
            }
        }
    }

    @Override
    public void deactivate() {
        shuffledBefore = false;
        TaskScheduler.scheduleTask(1, OtherUtils::reloadServerNoUpdate);
        TaskScheduler.scheduleTask(10, Hunger::updateInventories);
        for (ServerPlayerEntity player : PlayerUtils.getAllPlayers()) {
            player.removeStatusEffect(StatusEffects.HUNGER);
        }
        super.deactivate();
    }
    @Override
    public void activate() {
        shuffleVersion = rnd.nextInt(0,100);
        shuffledBefore = false;
        lastVersion = -1;
        super.activate();
    }

    public void newFoodRules() {
        if (shuffledBefore) {
            PlayerUtils.playSoundToPlayers(PlayerUtils.getAllPlayers(), SoundEvents.BLOCK_NOTE_BLOCK_PLING.value());
            PlayerUtils.sendTitleWithSubtitleToPlayers(PlayerUtils.getAllPlayers(), Text.of(""), Text.of("§7Food is about to be randomised..."), 0, 140, 0);
            TaskScheduler.scheduleTask(40, WildcardManager::showDots);
            TaskScheduler.scheduleTask(140, () -> {
                updateInventories();
                PlayerUtils.playSoundToPlayers(PlayerUtils.getAllPlayers(), SoundEvents.ENTITY_ELDER_GUARDIAN_CURSE, 0.2f, 1);
            });
        }
        else {
            TaskScheduler.scheduleTask(10, Hunger::updateInventories);
        }
        shuffledBefore = true;
        shuffleVersion++;
        for (ServerPlayerEntity player : PlayerUtils.getAllPlayers()) {
            addHunger(player);
        }
        TaskScheduler.scheduleTask(1, OtherUtils::reloadServerNoUpdate);
        NetworkHandlerServer.sendUpdatePackets();
    }

    public static void updateInventories() {
        for (ServerPlayerEntity player : PlayerUtils.getAllPlayers()) {
            updateInventory(player);
        }
    }

    public static void updateInventory(ServerPlayerEntity player) {
        PlayerInventory inventory = player.getInventory();
        for (int i = 0; i < inventory.size(); i++) {
            ItemStack stack = inventory.getStack(i);
            if (stack.isEmpty()) continue;

            stack.set(DataComponentTypes.FOOD, stack.getDefaultComponents().get(DataComponentTypes.FOOD));
            //? if >= 1.21.2 {
            /*stack.set(DataComponentTypes.CONSUMABLE, stack.getDefaultComponents().get(DataComponentTypes.CONSUMABLE));
            *///?}

            ComponentChanges changes = stack.getComponentChanges();
            inventory.setStack(i, new ItemStack(stack.getItem(), stack.getCount()));
            ItemStack newStack = inventory.getStack(i);
            newStack.applyChanges(changes);
        }
        player.getInventory().updateItems();
        player.currentScreenHandler.sendContentUpdates();
        player.playerScreenHandler.onContentChanged(player.getInventory());
    }

    public static void addHunger(ServerPlayerEntity player) {
        StatusEffectInstance statusEffectInstance = new StatusEffectInstance(StatusEffects.HUNGER, -1, 2);
        player.addStatusEffect(statusEffectInstance);
    }

    public static void onUseItem(ServerPlayerEntity player) {
        if (!player.hasStatusEffect(StatusEffects.HUNGER) && WildcardManager.isActiveWildcard(Wildcards.HUNGER)){
            addHunger(player);
        }
    }

    //? if <=1.21 {
    public static void applyFoodComponents(Item item, ComponentMapImpl components) {
        if (components.contains(DataComponentTypes.FOOD)) {
            StatusEffectInstance statusEffectInstance = new StatusEffectInstance(StatusEffects.HUNGER, 3600, 7);
            FoodComponent.StatusEffectEntry statusEffect = new FoodComponent.StatusEffectEntry(statusEffectInstance, 1);
            components.set(DataComponentTypes.FOOD, new FoodComponent(0, 0, false, 1.6f, Optional.empty(), List.of(statusEffect)));
            return;
        }
        int hash = getHash(item);
        List<FoodComponent.StatusEffectEntry> foodEffects = new ArrayList<>();
        if ((hash % 13) % 3 != 0) {
            int amplifier = hash % 5; // 0 -> 4
            int duration = ((3 + hash) % 18) * 20; // 1 -> 20 seconds
            RegistryEntry<StatusEffect> registryEntryEffect = effects.get(hash % effects.size());
            if (levelLimit.contains(registryEntryEffect)) {
                amplifier = 0;
            }
            if (durationLimit.contains(registryEntryEffect)) {
                duration = 1;
            }
            StatusEffectInstance statusEffectInstance = new StatusEffectInstance(registryEntryEffect, duration, amplifier);
            FoodComponent.StatusEffectEntry statusEffect = new FoodComponent.StatusEffectEntry(statusEffectInstance, 1);
            foodEffects.add(statusEffect);
        }

        int nutrition = hash % 19 - 10; // -10 -> 8
        int saturation = hash % 12 - 7; // -7 -> 4
        if (nutrition < 0) nutrition = 0;
        if (saturation < 0) saturation = 0;
        if (saturation > nutrition) saturation = nutrition;
        components.set(DataComponentTypes.FOOD, new FoodComponent(nutrition, saturation, false, 1.6f, Optional.empty(), foodEffects));
    }
    //?} else {
    /*public static void applyFoodComponents(Item item, MergedComponentMap components) {
        if (components.contains(DataComponentTypes.CONSUMABLE)) {
            StatusEffectInstance statusEffectInstance = new StatusEffectInstance(StatusEffects.HUNGER, 3600, 7);
            ApplyEffectsConsumeEffect statusEffect = new ApplyEffectsConsumeEffect(statusEffectInstance, 1);
            components.set(DataComponentTypes.CONSUMABLE,
                    new ConsumableComponent(ConsumableComponent.DEFAULT_CONSUME_SECONDS, UseAction.EAT, SoundEvents.ENTITY_GENERIC_EAT, true, List.of(statusEffect))
            );
            components.set(DataComponentTypes.FOOD, new FoodComponent(0, 0, false));
            return;
        }
        int hash = getHash(item);
        List<ConsumeEffect> foodEffects = new ArrayList<>();
        if ((hash % 13) % 3 != 0) {
            int amplifier = hash % 5; // 0 -> 4
            int duration = ((3 + hash) % 18) * 20; // 1 -> 20 seconds
            RegistryEntry<StatusEffect> registryEntryEffect = effects.get(hash % effects.size());
            if (levelLimit.contains(registryEntryEffect)) {
                amplifier = 0;
            }
            if (durationLimit.contains(registryEntryEffect)) {
                duration = 1;
            }
            StatusEffectInstance statusEffectInstance = new StatusEffectInstance(registryEntryEffect, duration, amplifier);
            ApplyEffectsConsumeEffect statusEffect = new ApplyEffectsConsumeEffect(statusEffectInstance, 1);
            foodEffects.add(statusEffect);
        }

        int nutrition = hash % 19 - 10; // -10 -> 8
        int saturation = hash % 12 - 7; // -7 -> 4
        if (nutrition < 0) nutrition = 0;
        if (saturation < 0) saturation = 0;
        if (saturation > nutrition) saturation = nutrition;

        components.set(DataComponentTypes.CONSUMABLE,
                new ConsumableComponent(ConsumableComponent.DEFAULT_CONSUME_SECONDS, UseAction.EAT, SoundEvents.ENTITY_GENERIC_EAT, true, foodEffects)
        );
        components.set(DataComponentTypes.FOOD, new FoodComponent(nutrition, saturation, false));

    }
    *///?}

    private static int getHash(Item item) {
        String itemId = Registries.ITEM.getId(item).toString();
        return Math.abs((itemId.hashCode() + shuffleVersion) * 31);
    }
}