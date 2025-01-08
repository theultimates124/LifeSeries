package net.mat0u5.lifeseries.utils;

import net.mat0u5.lifeseries.Main;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.scoreboard.Team;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import static net.mat0u5.lifeseries.Main.server;

public class TeamUtils {

    public static void createTeam(String teamName, Formatting color) {
        if (server == null) return;
        Scoreboard scoreboard = server.getScoreboard();
        if (scoreboard.getTeam(teamName) != null) {
            // A team with this name already exists
            return;
        }
        Team team = scoreboard.addTeam(teamName);
        team.setDisplayName(Text.literal(teamName).formatted(color));
        team.setColor(color);
    }

    public static void addPlayerToTeam(String teamName, ServerPlayerEntity player) {
        if (server == null) return;
        Scoreboard scoreboard = server.getScoreboard();
        Team team = scoreboard.getTeam(teamName);

        if (team == null) {
            // A team with this name does not exist
            return;
        }

        scoreboard.addScoreHolderToTeam(player.getNameForScoreboard(), team);
    }

    public static boolean removePlayerFromTeam(ServerPlayerEntity player) {
        if (server == null) return false;
        Scoreboard scoreboard = server.getScoreboard();
        String playerName = player.getNameForScoreboard();

        Team team = scoreboard.getScoreHolderTeam(playerName);
        if (team == null) {
            Main.LOGGER.warn("Player " + playerName + " is not part of any team!");
            return false;
        }

        scoreboard.removeScoreHolderFromTeam(playerName, team);
        return true;
    }

    public static boolean deleteTeam(String teamName) {
        if (server == null) return false;
        Scoreboard scoreboard = server.getScoreboard();
        Team team = scoreboard.getTeam(teamName);

        if (team == null) {
            Main.LOGGER.warn("Team with name " + teamName + " does not exist!");
            return false;
        }

        scoreboard.removeTeam(team);
        return true;
    }

    public static Team getPlayerTeam(ServerPlayerEntity player) {
        if (server == null) return null;
        Scoreboard scoreboard = server.getScoreboard();
        return scoreboard.getScoreHolderTeam(player.getNameForScoreboard());
    }
}
