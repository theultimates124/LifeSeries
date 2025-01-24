package net.mat0u5.lifeseries.registries;

import eu.pb4.polymer.core.api.other.PolymerSoundEvent;
import net.mat0u5.lifeseries.Main;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Identifier;

public class SoundRegistry {
    public static final SoundEvent TEST_AMBIENT = register("test.ambient", SoundEvents.ENTITY_TURTLE_AMBIENT_LAND);
    public static final SoundEvent TEST_HURT = register("test.hurt", SoundEvents.ENTITY_TURTLE_HURT);
    public static final SoundEvent TEST_DEATH = register("test.death", SoundEvents.ENTITY_TURTLE_DEATH);

    private static SoundEvent register(String name, SoundEvent soundEvent) {
        Identifier id = Identifier.of(Main.MOD_ID, name);
        return Registry.register(Registries.SOUND_EVENT, id, PolymerSoundEvent.of(id, soundEvent));
    }

    public static void registerSounds() {
    }
}
