package net.mat0u5.lifeseries.command;

import com.mojang.brigadier.CommandDispatcher;
import net.mat0u5.lifeseries.utils.OtherUtils;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.command.CommandSource;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static net.mat0u5.lifeseries.Main.*;
import static net.mat0u5.lifeseries.utils.PermissionManager.isAdmin;
import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public class ClaimKillCommand {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher,
                                CommandRegistryAccess commandRegistryAccess,
                                CommandManager.RegistrationEnvironment registrationEnvironment) {
        dispatcher.register(
            literal("claimkill")
                .then(argument("player", EntityArgumentType.player())
                    .suggests((context, builder) -> CommandSource.suggestMatching(getSuggestions(), builder))
                    .executes(context -> claimCredit(
                        context.getSource(), EntityArgumentType.getPlayer(context, "player")
                    ))
                )
                .then(literal("validate")
                    .requires(source -> ((isAdmin(source.getPlayer()) || (source.getEntity() == null))))
                    .then(argument("killer", EntityArgumentType.player())
                        .then(argument("victim", EntityArgumentType.player())
                            .executes(context -> claimCreditAccept(
                                context.getSource(),
                                EntityArgumentType.getPlayer(context, "killer"),
                                EntityArgumentType.getPlayer(context, "victim")
                            ))
                        )
                    )
                )
        );
    }

    public static List<String> getSuggestions() {
        List<String> suggestions = new ArrayList<>();
        Set<UUID> recentDeaths = currentSession.playerNaturalDeathLog.keySet();
        for (UUID uuid : recentDeaths) {
            ServerPlayerEntity player = server.getPlayerManager().getPlayer(uuid);
            if (player == null) continue;
            suggestions.add(player.getNameForScoreboard());
        }
        return suggestions;
    }

    public static int claimCredit(ServerCommandSource source, ServerPlayerEntity victim) {
        if (victim == null) return -1;
        PlayerEntity player = source.getPlayer();
        if (player == null) return -1;

        Set<UUID> recentDeaths = currentSession.playerNaturalDeathLog.keySet();
        UUID victimUUID = victim.getUuid();
        if (!recentDeaths.contains(victimUUID)) {
            source.sendError(Text.of(victim.getNameForScoreboard() + " did not die in the last 2 minutes. Or they might have been killed by a player directly."));
            return -1;
        }
        if (player == victim) {
            source.sendError(Text.of("You cannot claim credit for your own death :P"));
            return -1;
        }
        Text textAll = Text.literal("").append(player.getStyledDisplayName()).append("§7 claims credit for ")
                .append(victim.getStyledDisplayName())
                .append(Text.of("§7's death. Only an admin can validate this claim."));
        OtherUtils.broadcastMessage(textAll);
        Text adminText = Text.literal("§7Click ").append(
                Text.literal("here")
                        .styled(style -> style
                                .withColor(Formatting.WHITE)
                                .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/claimkill validate " + player.getNameForScoreboard() + " "+victim.getNameForScoreboard()))
                                .withUnderline(true)
                        )).append(Text.of("§7 to accept the claim if you think it's valid."));
        OtherUtils.broadcastMessageToAdmins(adminText);

        return 1;
    }

    public static int claimCreditAccept(ServerCommandSource source, ServerPlayerEntity killer, ServerPlayerEntity victim) {
        if (killer == null) return -1;
        if (victim == null) return -1;

        Text message = Text.literal("").append(killer.getStyledDisplayName()).append(Text.of("§7's kill claim on ")).append(victim.getStyledDisplayName()).append(Text.of("§7 was accepted."));
        OtherUtils.broadcastMessage(message);
        currentSeries.onClaimKill(killer, victim);
        currentSession.playerNaturalDeathLog.remove(victim.getUuid());

        return 1;
    }
}
