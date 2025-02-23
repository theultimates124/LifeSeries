package net.mat0u5.lifeseries.utils.morph;

import net.mat0u5.lifeseries.series.wildlife.wildcards.wildcard.SizeShifting;
import net.mat0u5.lifeseries.utils.OtherUtils;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;
import org.ladysnake.cca.api.v3.component.sync.AutoSyncedComponent;
import org.ladysnake.cca.api.v3.component.tick.ClientTickingComponent;

import static net.mat0u5.lifeseries.Main.MORPH_COMPONENT;
/*
    Code from: https://gitlab.nexusrealms.de/Farpo/riftmorph
 */
public class MorphComponent implements AutoSyncedComponent, ClientTickingComponent {

    private PlayerEntity player;
    @Nullable
    private EntityType<?> morph = null;
    private boolean shouldMorph = false;
    private LivingEntity dummy = null;

    public MorphComponent(PlayerEntity player){
        this.player = player;
    }

    public void setMorph(EntityType<?> morph){
        shouldMorph = morph != null;
        this.morph = morph;
    }
    public boolean isMorphed(){
        return (morph != null) && shouldMorph;
    }

    @Override
    public boolean isRequiredOnClient() {
        return false;
    }

    @Override
    public void clientTick() {
        if(isMorphed()){
            if(dummy == null || dummy.getType() != morph){
                //? if <= 1.21 {
                Entity entity = morph.create(player.getWorld());
                //?} else {
                /*Entity entity = morph.create(player.getWorld(), SpawnReason.COMMAND);
                *///?}
                if(!(entity instanceof LivingEntity)){
                    morph = null;
                    shouldMorph = false;
                    MORPH_COMPONENT.sync(player);
                    return;
                }
                dummy = (LivingEntity) entity;
                /*
                ((DummyInterface) dummy).makeDummy();
                ((DummyInterface) dummy).setPlayer(player);
                */

            }
            dummy.prevX = player.prevX;
            dummy.prevY = player.prevY;
            dummy.prevZ = player.prevZ;
            dummy.prevBodyYaw = player.prevBodyYaw;
            dummy.prevHeadYaw = player.prevHeadYaw;
            dummy.prevPitch = player.prevPitch;

            //Some math to sync the dummy LimbAnimator to the player LimbAnimator
            float prevPlayerSpeed = (player.limbAnimator.getSpeed(-1)+player.limbAnimator.getSpeed())/2;
            dummy.limbAnimator.setSpeed(prevPlayerSpeed);
            //? if <= 1.21 {
            dummy.limbAnimator.updateLimbs(player.limbAnimator.getPos() - dummy.limbAnimator.getPos(), 1);
             //?} else {
            /*dummy.limbAnimator.updateLimbs(player.limbAnimator.getPos() - dummy.limbAnimator.getPos(), 1, 1);
            *///?}
            dummy.limbAnimator.setSpeed(player.limbAnimator.getSpeed());

            dummy.lastHandSwingProgress = player.lastHandSwingProgress;
            dummy.handSwingProgress = player.handSwingProgress;
            dummy.handSwinging = player.handSwinging;
            dummy.handSwingTicks = player.handSwingTicks;

            dummy.setPosition(player.getPos());
            dummy.setBodyYaw(player.bodyYaw);
            dummy.setHeadYaw(player.headYaw);
            dummy.setPitch(player.getPitch());
            dummy.setSneaking(player.isSneaking());
        }
    }
    @Nullable
    public LivingEntity getDummy(){
        return dummy;
    }

    @Override
    public void readFromNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup wrapperLookup) {
        if(nbt.contains("type")){
            morph = Registries.ENTITY_TYPE.get(Identifier.of(nbt.getString("type")));
        }
        shouldMorph = nbt.getBoolean("morph");
    }

    @Override
    public void writeToNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup wrapperLookup) {
        if(morph != null){
            nbt.putString("type", Registries.ENTITY_TYPE.getId(morph).toString());
        } else {
            nbt.remove("type");
        }
        nbt.putBoolean("morph", shouldMorph);
    }
}


