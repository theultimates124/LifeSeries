package net.mat0u5.lifeseries.series.wildlife.wildcards.wildcard.trivia;

import net.mat0u5.lifeseries.Main;
import net.mat0u5.lifeseries.series.wildlife.wildcards.wildcard.trivia.gui.QuizScreen;
import net.mat0u5.lifeseries.network.NetworkHandlerClient;
import net.mat0u5.lifeseries.network.packets.TriviaQuestionPayload;
import net.minecraft.client.MinecraftClient;

import java.util.ArrayList;
import java.util.List;

public class Trivia {
    public static String question = "";
    public static List<String> answers = new ArrayList<>();
    public static int difficulty = 0;
    public static int secondsToComplete = 0;
    public static long timestamp = 0;
    public static void receiveTrivia(TriviaQuestionPayload payload) {
        question = payload.question();
        answers = payload.answers();
        difficulty = payload.difficulty();
        timestamp = payload.timestamp();
        secondsToComplete = payload.timeToComplete();
        Main.LOGGER.info("[PACKET_CLIENT] Received trivia question: {" + question + ", " + difficulty + ", " + answers + "}");
        openGui();
    }

    public static long getRemainingTime() {
        long timeSinceStart = (int) Math.ceil((System.currentTimeMillis() - timestamp) / 1000.0);
        return secondsToComplete - timeSinceStart;
    }

    public static boolean isDoingTrivia() {
        if (Trivia.secondsToComplete == 0) return false;
        long remaining = Trivia.getRemainingTime();
        if (remaining <= 0) return false;
        if (remaining > 1000000) return false;
        return true;
    }

    public static void openGui() {
        if (question.isEmpty() || answers.isEmpty()) return;
        if (!Main.isClient()) return;
        MinecraftClient.getInstance().setScreen(new QuizScreen());
    }

    public static void closeGui() {

    }

    public static void sendAnswer(int answer) {
        question = "";
        answers = new ArrayList<>();
        difficulty = 0;
        secondsToComplete = 0;
        timestamp = 0;
        closeGui();
        NetworkHandlerClient.sendTriviaAnswer(answer);
    }
}
