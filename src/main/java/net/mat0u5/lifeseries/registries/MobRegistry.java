package net.mat0u5.lifeseries.registries;

import eu.pb4.polymer.core.api.entity.PolymerEntityUtils;
import eu.pb4.polymer.core.api.item.PolymerItemGroupUtils;
import eu.pb4.polymer.core.api.item.PolymerSpawnEggItem;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricEntityTypeBuilder;
import net.mat0u5.lifeseries.Main;
import net.mat0u5.lifeseries.entity.custom.Snail;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class MobRegistry {
    public static final EntityType<Snail> SNAIL = register(
            Snail.ID,
            FabricEntityTypeBuilder.createMob()
                    .entityFactory(Snail::new)
                    .spawnGroup(SpawnGroup.MONSTER)
                    .dimensions(EntityDimensions.changing(0.7f, 1.8f))
                    .defaultAttributes(Snail::createAttributes)
    );

    private static <T extends Entity> EntityType<T> register(Identifier id, FabricEntityTypeBuilder<T> builder) {
        EntityType<T> type = builder.build();
        PolymerEntityUtils.registerType(type);
        return Registry.register(Registries.ENTITY_TYPE, id, type);
    }

    public static void registerMobs() {
        addSpawnEgg(SNAIL, Items.HORSE_SPAWN_EGG);
        PolymerItemGroupUtils.registerPolymerItemGroup(Identifier.of(Main.MOD_ID, "spawn-eggs"), ITEM_GROUP);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static void addSpawnEgg(EntityType type, Item item) {
        Item spawnEgg = new PolymerSpawnEggItem(type, item, new Item.Settings());
        registerItem(Identifier.of(Main.MOD_ID, EntityType.getId(type).getPath() + "_spawn_egg"), spawnEgg);
    }

    private static void registerItem(Identifier identifier, Item item) {
        Registry.register(Registries.ITEM, identifier, item);
        SPAWN_EGGS.putIfAbsent(identifier, item);
    }

    public static final Object2ObjectOpenHashMap<Identifier, Item> SPAWN_EGGS = new Object2ObjectOpenHashMap<>();
    public static final ItemGroup ITEM_GROUP = new ItemGroup.Builder (null, -1)
            .displayName(Text.literal("Life Series"))
            .icon(Items.BAT_SPAWN_EGG::getDefaultStack)
            .entries((parameters, output) -> SPAWN_EGGS.values().forEach(output::add))
            .build();
}

