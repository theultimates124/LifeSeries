package net.mat0u5.lifeseries.series.wildlife.wildcards.wildcard.superpowers.superpower;

import net.mat0u5.lifeseries.entity.fakeplayer.FakePlayer;
import net.mat0u5.lifeseries.network.NetworkHandlerServer;
import net.mat0u5.lifeseries.series.wildlife.wildcards.wildcard.superpowers.Superpowers;
import net.mat0u5.lifeseries.series.wildlife.wildcards.wildcard.superpowers.ToggleableSuperpower;
import net.mat0u5.lifeseries.utils.OtherUtils;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.network.DisconnectionInfo;
import net.minecraft.network.packet.s2c.play.PositionFlag;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.GameMode;
import org.jetbrains.annotations.Nullable;

import java.util.EnumSet;
import java.util.Set;

import static net.mat0u5.lifeseries.Main.server;

public class AstralProjection extends ToggleableSuperpower {
    @Nullable
    public FakePlayer clone;
    @Nullable
    private Vec3d startedPos;
    @Nullable
    private ServerWorld startedWorld;
    private float[] startedLooking = new float[2];

    public AstralProjection(ServerPlayerEntity player) {
        super(player);
    }

    @Override
    public Superpowers getSuperpower() {
        return Superpowers.ASTRAL_PROJECTION;
    }

    @Override
    public void activate() {
        super.activate();
        resetParams();
        startProjection();
    }

    @Override
    public void deactivate() {
        super.deactivate();
        cancelProjection();
        resetParams();
    }

    @Override
    public int deactivateCooldown() {
        return 5;
    }

    public void resetParams() {
        clone = null;
        startedPos = null;
        startedLooking = new float[2];
        startedWorld = null;
    }

    public void startProjection() {
        ServerPlayerEntity player = getPlayer();
        if (player == null) return;
        if (player.isSpectator()) return;

        String fakePlayerName = "`"+player.getNameForScoreboard();

        startedPos = player.getPos();
        startedLooking[0] = player.getYaw();
        startedLooking[1] = player.getPitch();
        startedWorld = player.getServerWorld();
        player.changeGameMode(GameMode.SPECTATOR);
        PlayerInventory inv = player.getInventory();

        FakePlayer.createFake(fakePlayerName, player.server, startedPos, startedLooking[0], startedLooking[1], player.server.getOverworld().getRegistryKey(),
                GameMode.SURVIVAL, false, inv, player.getUuid(), player.getDisplayName()).thenAccept((fakePlayer) -> {
            clone = fakePlayer;
            NetworkHandlerServer.sendPlayerDisguise("player_disguise", clone.getUuid().toString(), clone.getName().getString(), player.getUuid().toString(), player.getName().getString());
        });
    }

    public void cancelProjection() {
        ServerPlayerEntity player = getPlayer();
        if (player == null) return;

        Vec3d toBackPos = startedPos;
        if (clone != null) {
            toBackPos = clone.getPos();
            clone.networkHandler.onDisconnected(new DisconnectionInfo(Text.of("")));
            NetworkHandlerServer.sendPlayerDisguise("player_disguise", clone.getUuid().toString(), clone.getName().getString(), "", "");
        }

        if (startedWorld != null && toBackPos != null) {
            //? if <= 1.21 {
            player.teleport(startedWorld, toBackPos.getX(), toBackPos.getY(), toBackPos.getZ(),
                    EnumSet.noneOf(PositionFlag.class), startedLooking[0], startedLooking[1]);
             //?} else {
            /*player.teleport(startedWorld, toBackPos.getX(), toBackPos.getY(), toBackPos.getZ(),
                    EnumSet.noneOf(PositionFlag.class), startedLooking[0], startedLooking[1], false);
            *///?}
        }
        player.changeGameMode(GameMode.SURVIVAL);
    }


    //? if <= 1.21 {
    public void onDamageClone(DamageSource source, float amount) {
     //?} else {
    /*public void onDamageClone(ServerWorld world, DamageSource source, float amount) {
    *///?}
        deactivate();
        ServerPlayerEntity player = getPlayer();
        if (player == null) return;
        //? if <= 1.21 {
        player.damage(source, amount);
         //?} else {
        /*player.damage(world, source, amount);
        *///?}
    }
}
