package net.mat0u5.lifeseries.utils;

import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.scoreboard.Team;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import static net.mat0u5.lifeseries.Main.server;

public class TeamUtils {

    public static Team createTeam(String teamName, Formatting color) {
        Scoreboard scoreboard = server.getScoreboard();
        if (scoreboard.getTeam(teamName) != null) {
            // A team with this name already exists
            return null;
        }
        Team team = scoreboard.addTeam(teamName);
        team.setDisplayName(Text.literal(teamName).formatted(color));
        team.setColor(color);
        return team;
    }

    public static boolean addPlayerToTeam(String teamName, ServerPlayerEntity player) {
        Scoreboard scoreboard = server.getScoreboard();
        Team team = scoreboard.getTeam(teamName);

        if (team == null) {
            // A team with this name does not exist
            return false;
        }

        scoreboard.addScoreHolderToTeam(player.getNameForScoreboard(), team);
        return true;
    }

    public static boolean removePlayerFromTeam(ServerPlayerEntity player) {
        Scoreboard scoreboard = server.getScoreboard();
        String playerName = player.getNameForScoreboard();

        Team team = scoreboard.getScoreHolderTeam(playerName);
        if (team == null) {
            System.out.println("Player " + playerName + " is not part of any team!");
            return false;
        }

        scoreboard.removeScoreHolderFromTeam(playerName, team);
        return true;
    }

    public static boolean deleteTeam(String teamName) {
        Scoreboard scoreboard = server.getScoreboard();
        Team team = scoreboard.getTeam(teamName);

        if (team == null) {
            System.out.println("Team with name " + teamName + " does not exist!");
            return false;
        }

        scoreboard.removeTeam(team);
        return true;
    }

    public static Team getPlayerTeam(ServerPlayerEntity player) {
        Scoreboard scoreboard = server.getScoreboard();
        return scoreboard.getScoreHolderTeam(player.getNameForScoreboard());
    }
}
