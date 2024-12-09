package net.mat0u5.lifeseries.command;

import com.mojang.brigadier.CommandDispatcher;
import net.mat0u5.lifeseries.series.Boogeyman;
import net.mat0u5.lifeseries.series.BoogeymanManager;
import net.mat0u5.lifeseries.series.SeriesList;
import net.mat0u5.lifeseries.series.lastlife.LastLife;
import net.mat0u5.lifeseries.series.limitedlife.LimitedLife;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.List;

import static net.mat0u5.lifeseries.Main.currentSeries;
import static net.mat0u5.lifeseries.utils.PermissionManager.isAdmin;
import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public class BoogeymanCommand {

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher,
                                CommandRegistryAccess commandRegistryAccess,
                                CommandManager.RegistrationEnvironment registrationEnvironment) {
        if (currentSeries.getSeries() != SeriesList.LAST_LIFE &&
            currentSeries.getSeries() != SeriesList.LIMITED_LIFE) return;
        dispatcher.register(
            literal("boogeyman")
                .then(literal("clear")
                    .requires(source -> ((isAdmin(source.getPlayer()) || (source.getEntity() == null))))
                    .executes(context -> boogeyClear(
                        context.getSource()
                    ))
                )
                .then(literal("list")
                    .requires(source -> ((isAdmin(source.getPlayer()) || (source.getEntity() == null) || !currentSeries.isAlive(source.getPlayer()))))
                    .executes(context -> boogeyList(
                        context.getSource()
                    ))
                )
                .then(literal("add")
                    .requires(source -> ((isAdmin(source.getPlayer()) || (source.getEntity() == null))))
                    .then(argument("player", EntityArgumentType.player())
                        .executes(context -> addBoogey(context.getSource(), EntityArgumentType.getPlayer(context, "player")))
                    )
                )
                .then(literal("remove")
                    .requires(source -> ((isAdmin(source.getPlayer()) || (source.getEntity() == null))))
                    .then(argument("player", EntityArgumentType.player())
                        .executes(context -> removeBoogey(context.getSource(), EntityArgumentType.getPlayer(context, "player")))
                    )
                )
                .then(literal("cure")
                    .requires(source -> ((isAdmin(source.getPlayer()) || (source.getEntity() == null))))
                    .then(argument("player", EntityArgumentType.player())
                        .executes(context -> cureBoogey(context.getSource(), EntityArgumentType.getPlayer(context, "player")))
                    )
                )
                .then(literal("chooseRandom")
                    .requires(source -> ((isAdmin(source.getPlayer()) || (source.getEntity() == null))))
                    .executes(context -> boogeyChooseRandom(
                        context.getSource()
                    ))
                )

        );
    }
    public static BoogeymanManager getBM() {
        if (currentSeries.getSeries() == SeriesList.LAST_LIFE) return ((LastLife) currentSeries).boogeymanManager;
        if (currentSeries.getSeries() == SeriesList.LIMITED_LIFE) return ((LimitedLife) currentSeries).boogeymanManager;
        return null;
    }
    public static int cureBoogey(ServerCommandSource source, ServerPlayerEntity target) {

        if (target == null) return -1;

        BoogeymanManager bm = getBM();
        if (bm == null) return -1;

        if (!bm.isBoogeyman(target)) {
            source.sendError(Text.of("That player is not a boogeyman!"));
            return -1;
        }
        bm.cure(target);

        source.sendMessage(Text.literal("").append(target.getStyledDisplayName()).append(Text.of(" is now cured.")));

        return 1;
    }
    public static int addBoogey(ServerCommandSource source, ServerPlayerEntity target) {

        if (target == null) return -1;

        BoogeymanManager bm = getBM();
        if (bm == null) return -1;

        if (bm.isBoogeyman(target)) {
            source.sendError(Text.of("That player is already a boogeyman!"));
            return -1;
        }
        bm.addBoogeymanManually(target);

        source.sendMessage(Text.literal("").append(target.getStyledDisplayName()).append(Text.of(" is now a boogeyman.")));

        return 1;
    }
    public static int removeBoogey(ServerCommandSource source, ServerPlayerEntity target) {

        if (target == null) return -1;

        BoogeymanManager bm = getBM();
        if (bm == null) return -1;

        if (!bm.isBoogeyman(target)) {
            source.sendError(Text.of("That player is not a boogeyman!"));
            return -1;
        }
        bm.removeBoogeymanManually(target);

        source.sendMessage(Text.literal("").append(target.getStyledDisplayName()).append(Text.of(" is no longer a boogeyman.")));

        return 1;
    }
    public static int boogeyList(ServerCommandSource source) {
        BoogeymanManager bm = getBM();
        if (bm == null) return -1;

        List<String> boogeymen = new ArrayList<>();
        for (Boogeyman boogeyman : bm.boogeymen) {
            boogeymen.add(boogeyman.name);
        }
        source.sendMessage(Text.of("Current boogeymen: ["+String.join(", ",boogeymen)+"]"));

        return 1;
    }
    public static int boogeyClear(ServerCommandSource source) {
        BoogeymanManager bm = getBM();
        if (bm == null) return -1;

        bm.resetBoogeymen();
        source.sendMessage(Text.of("All boogeymen have been cleared"));

        return 1;
    }
    public static int boogeyChooseRandom(ServerCommandSource source) {
        BoogeymanManager bm = getBM();
        if (bm == null) return -1;

        bm.resetBoogeymen();
        bm.prepareToChooseBoogeymen();

        return 1;
    }
}
