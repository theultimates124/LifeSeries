package net.mat0u5.lifeseries.series.wildlife.wildcards.wildcard.superpowers.superpower;

import net.mat0u5.lifeseries.series.wildlife.wildcards.wildcard.superpowers.Superpowers;
import net.mat0u5.lifeseries.series.wildlife.wildcards.wildcard.superpowers.ToggleableSuperpower;
import net.mat0u5.lifeseries.utils.PlayerUtils;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;

import java.util.List;

import static net.mat0u5.lifeseries.Main.MORPH_COMPONENT;


public class AnimalDisguise extends ToggleableSuperpower {

    public AnimalDisguise(ServerPlayerEntity player) {
        super(player);
    }

    @Override
    public Superpowers getSuperpower() {
        return Superpowers.ANIMAL_DISGUISE;
    }
    List<EntityType<?>> defaultRandom = List.of(EntityType.COW, EntityType.SHEEP, EntityType.CHICKEN, EntityType.PIG);

    @Override
    public void activate() {
        super.activate();

        ServerPlayerEntity player = getPlayer();
        if (player == null) return;
        Entity lookingAt = PlayerUtils.getEntityLookingAt(player, 50);
        EntityType<?> morph = null;
        if (lookingAt != null)  {
            if (lookingAt instanceof LivingEntity livingEntity &&
                    !(lookingAt instanceof PlayerEntity)
            ) {
                morph = lookingAt.getType();
            }
        }
        if (morph == null) {
            morph = defaultRandom.get(player.getRandom().nextInt(defaultRandom.size()));
        }
        player.getServerWorld().playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.ENTITY_PUFFER_FISH_BLOW_UP, SoundCategory.MASTER, 1, 1);

        EntityType<?> finalMorph = morph;
        MORPH_COMPONENT.maybeGet(player).ifPresent(morphComponent -> morphComponent.setMorph(finalMorph));
        MORPH_COMPONENT.sync(player);
    }

    @Override
    public void deactivate() {
        super.deactivate();
        ServerPlayerEntity player = getPlayer();
        if (player == null) return;
        player.getServerWorld().playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.ENTITY_PUFFER_FISH_BLOW_OUT, SoundCategory.MASTER, 1, 1);

        MORPH_COMPONENT.maybeGet(player).ifPresent(morphComponent -> morphComponent.setMorph(null));
        MORPH_COMPONENT.sync(player);

    }

    public void onTakeDamage() {
        deactivate();
    }
}
