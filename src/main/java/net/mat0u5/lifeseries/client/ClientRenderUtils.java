package net.mat0u5.lifeseries.client;

import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.mat0u5.lifeseries.MainClient;
import net.mat0u5.lifeseries.series.wildlife.wildcards.wildcard.trivia.Trivia;
import net.mat0u5.lifeseries.utils.OtherUtils;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.text.Text;

public class ClientRenderUtils {
    public static void onInitialize() {
        HudRenderCallback.EVENT.register(ClientRenderUtils::renderHud);
    }

    private static void renderHud(DrawContext context, RenderTickCounter renderTickCounter) {
        renderHud(context);
    }

    public static void renderHud(DrawContext context) {
        MinecraftClient client = MinecraftClient.getInstance();
        int yPos = client.getWindow().getScaledHeight() - 10;
        yPos += renderTriviaTimer(context, yPos);
        yPos += renderSuperpowerCooldown(context, yPos);
    }

    public static int renderTriviaTimer(DrawContext context, int y) {
        if (!Trivia.isDoingTrivia()) return 0;
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.currentScreen != null) return 0;

        int secondsLeft = (int) Trivia.getRemainingTime();

        Text actualTimer = Text.of(OtherUtils.formatTimeNoHours(secondsLeft*20));
        Text timerText = Text.of("§7Trivia timer: ");

        int screenWidth = client.getWindow().getScaledWidth();
        int x = screenWidth - 10;

        if (secondsLeft <= 5) renderTextLeft(context, actualTimer, x, y, 0xFFbf2222);
        else if (secondsLeft <= 30) renderTextLeft(context, actualTimer, x, y, 0xFFd6961a);
        else renderTextLeft(context, actualTimer, x, y, 0xFFffffff);

        renderTextLeft(context, timerText, x - client.textRenderer.getWidth(actualTimer), y);

        return -client.textRenderer.fontHeight-10;
    }

    public static int renderSuperpowerCooldown(DrawContext context, int y) {
        if (MainClient.SUPERPOWER_COOLDOWN_TIMESTAMP == 0) return 0;
        long currentMillis = System.currentTimeMillis();
        if (currentMillis >= MainClient.SUPERPOWER_COOLDOWN_TIMESTAMP) return 0;
        long millisLeft = MainClient.SUPERPOWER_COOLDOWN_TIMESTAMP - currentMillis;
        if (millisLeft > 10000000) return 0;
        MinecraftClient client = MinecraftClient.getInstance();

        Text timerText = Text.of("§7Superpower cooldown: §f"+OtherUtils.formatTimeNoHours((int) (millisLeft / 50.0)));

        int screenWidth = client.getWindow().getScaledWidth();
        int x = screenWidth - 10;
        renderTextLeft(context, timerText, x, y);

        return -client.textRenderer.fontHeight-10;
    }

    public static void renderTextLeft(DrawContext context, Text text, int x, int y) {
        renderTextLeft(context, text, x, y, 0x3c3c3c);
    }

    public static void renderTextLeft(DrawContext context, Text text, int x, int y, int color) {
        MinecraftClient client = MinecraftClient.getInstance();
        context.drawText(client.textRenderer, text, x - client.textRenderer.getWidth(text), y - client.textRenderer.fontHeight, color, false);
    }
}
