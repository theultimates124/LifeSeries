package net.mat0u5.lifeseries.series.wildlife.wildcards.wildcard.trivia.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import net.mat0u5.lifeseries.series.wildlife.wildcards.wildcard.trivia.Trivia;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.render.DiffuseLighting;
import net.minecraft.client.render.entity.EntityRenderDispatcher;
import net.minecraft.entity.Entity;
import net.minecraft.entity.decoration.DisplayEntity;
import net.minecraft.text.MutableText;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class QuizScreen extends Screen {
    private static final Identifier BACKGROUND_TEXTURE_LEFT = Identifier.of("lifeseries","textures/gui/trivia_question1.png");
    private static final Identifier BACKGROUND_TEXTURE_RIGHT = Identifier.of("lifeseries","textures/gui/trivia_question2.png");

    private static final int BG_WIDTH = 320;
    private static final int BG_HEIGHT = 180;

    public static final int TEXT_COLOR = 0x3c3c3c;
    public static final int TEXT_COLOR_HIGHLIGHTED = 0xffffff;
    private static final int[] ANSWER_COLORS = {
            0xFF5c57f3, 0xFFf8aa13, 0xFF5ef961, 0xFFf5fd6e, 0xFFed5b64
    };

    private final List<List<OrderedText>> answers = new ArrayList<>();
    private String DIFFICULTY = "Difficulty: null";
    private long timerSeconds = 120;
    private final List<Rectangle> answerRects = new ArrayList<>();

    public QuizScreen() {
        super(Text.literal("Quiz Screen"));
    }

    @Override
    public void renderBackground(DrawContext context, int mouseX, int mouseY, float delta) {}

    @Override
    protected void init() {
        super.init();
        timerSeconds = Trivia.getRemainingTime();

        int startX = (this.width - BG_WIDTH) / 2;
        int endX = startX + BG_WIDTH;
        int startY = (this.height - BG_HEIGHT) / 2;
        int fifth3 = startX + (BG_WIDTH / 5) * 3;
        int answersStartX = fifth3 + 15;
        int answersStopX = endX - 15;

        int maxWidth = answersStopX - answersStartX;

        int currentYPos = startY + 30;
        int gap = 8;
        answers.clear();
        answerRects.clear();
        for (int i = 0; i < Trivia.answers.size(); i++) {
            char answerIndex = (char) (i+65);
            MutableText label = Text.literal(answerIndex + ": ").formatted(Formatting.BOLD);
            MutableText answerText = Text.literal(Trivia.answers.get(i));
            answerText.setStyle(answerText.getStyle().withBold(false));
            Text text = label.append(answerText);
            List<OrderedText> answer = this.textRenderer.wrapLines(text, maxWidth);
            answers.add(answer);
            int answerBoxHeight = this.textRenderer.fontHeight * answer.size()+2;
            int answerBoxWidth = 0;
            for (OrderedText line : answer) {
                int lineWidth = this.textRenderer.getWidth(line);
                if (lineWidth > answerBoxWidth) answerBoxWidth = lineWidth;
            }
            answerBoxWidth += 2;

            Rectangle rect = new Rectangle(answersStartX, currentYPos, answerBoxWidth, answerBoxHeight);
            answerRects.add(rect);
            currentYPos += answerBoxHeight + gap;
        }
        switch (Trivia.difficulty) {
            case 1:
                DIFFICULTY = "Difficulty: Easy";
                break;
            case 2:
                DIFFICULTY = "Difficulty: Medium";
                break;
            case 3:
                DIFFICULTY = "Difficulty: Hard";
                break;
            default:
                DIFFICULTY = "Difficulty: null";
        }
    }

    @Override
    public void tick() {
        super.tick();
        timerSeconds = Trivia.getRemainingTime();
        if (timerSeconds <= 0) {
            this.close();
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0) { // Left-click
            for (int i = 0; i < answerRects.size(); i++) {
                if (answerRects.get(i).contains(mouseX, mouseY)) {
                    this.client.setScreen(new ConfirmAnswerScreen(this, i));
                    return true;
                }
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        this.renderBackground(context, mouseX, mouseY, delta);

        // X
        int startX = (this.width - BG_WIDTH) / 2;
        int endX = startX + BG_WIDTH;

        int centerX = (startX + endX) / 2;
        int fifth1 = startX + (BG_WIDTH / 5);
        int fifth2 = startX + (BG_WIDTH / 5) * 2;
        int fifth3 = startX + (BG_WIDTH / 5) * 3;
        int fifth4 = startX + (BG_WIDTH / 5) * 4;
        int questionX = startX + 10;
        int questionWidth = (fifth2-10) - questionX;

        // Y
        int startY = (this.height - BG_HEIGHT) / 2;
        int endY = startY + BG_HEIGHT;

        int minY = startY + 9;
        int centerY = (startY + endY) / 2;
        int maxY = endY - 23;
        int questionY = startY + 30;



        // Background
        //? if <= 1.21 {
        context.drawTexture(BACKGROUND_TEXTURE_LEFT, startX, startY, 0, 0, BG_WIDTH/2, BG_HEIGHT);
        context.drawTexture(BACKGROUND_TEXTURE_RIGHT, startX+BG_WIDTH/2, startY, 0, 0, BG_WIDTH/2, BG_HEIGHT);
         //?} else {
        /*context.drawTexture(RenderLayer::getGuiTextured, BACKGROUND_TEXTURE_LEFT, startX, startY, 0, 0, BG_WIDTH/2, BG_HEIGHT, 256, 256);
        context.drawTexture(RenderLayer::getGuiTextured, BACKGROUND_TEXTURE_RIGHT, startX+BG_WIDTH/2, startY, 0, 0, BG_WIDTH/2, BG_HEIGHT, 256, 256);
        *///?}

        /*
        testDrawX(context, 0);
        testDrawX(context, this.width-1);
        testDrawY(context, 0);
        testDrawY(context, this.height-1);
        testDrawX(context, startX);
        testDrawY(context, startY);
        testDrawX(context, endX);
        testDrawY(context, endY);
        testDrawX(context, fifth1);
        testDrawX(context, fifth2);
        testDrawX(context, fifth3);
        testDrawX(context, fifth4);
        */

        // Timer
        long minutes = timerSeconds / 60;
        long seconds = timerSeconds - minutes * 60;
        String secondsStr = String.valueOf(seconds);
        String minutesStr = String.valueOf(minutes);
        while (secondsStr.length() < 2) secondsStr = "0" + secondsStr;
        while (minutesStr.length() < 2) minutesStr = "0" + minutesStr;

        if (timerSeconds <= 5) drawTextCenter(context, Text.of(minutesStr + ":" + secondsStr), centerX, minY, 0xFFbf2222);
        else if (timerSeconds <= 30) drawTextCenter(context, Text.of(minutesStr + ":" + secondsStr), centerX, minY, 0xFFd6961a);
        else drawTextCenter(context, Text.of(minutesStr + ":" + secondsStr), centerX, minY);

        // Difficulty
        drawTextCenter(context, Text.of(DIFFICULTY), centerX, maxY);

        // Questions
        drawTextCenter(context, Text.literal("Question").formatted(Formatting.UNDERLINE), fifth1, minY);
        List<OrderedText> wrappedQuestion = this.textRenderer.wrapLines(Text.literal(Trivia.question), questionWidth);
        for (int i = 0; i < wrappedQuestion.size(); i++) {
            context.drawText(this.textRenderer, wrappedQuestion.get(i), questionX, questionY + i * this.textRenderer.fontHeight, TEXT_COLOR, false);
        }

        // Answers
        drawTextCenter(context, Text.literal("Answers").formatted(Formatting.UNDERLINE), fifth4, minY);
        for (int i = 0; i < Trivia.answers.size(); i++) {
            Rectangle rect = answerRects.get(i);
            int borderColor = ANSWER_COLORS[i % ANSWER_COLORS.length];
            context.fill(rect.x - 1, rect.y - 1, rect.x + rect.width + 1, rect.y, borderColor); // top border
            context.fill(rect.x - 1, rect.y + rect.height, rect.x + rect.width + 2, rect.y + rect.height + 2, borderColor); // bottom
            context.fill(rect.x - 1, rect.y, rect.x, rect.y + rect.height, borderColor); // left
            context.fill(rect.x + rect.width, rect.y-1, rect.x + rect.width + 2, rect.y + rect.height, borderColor); // right

            // Check if the mouse is hovering over this answer
            boolean hovered = rect.contains(mouseX, mouseY);
            int textColor = hovered ? TEXT_COLOR_HIGHLIGHTED : TEXT_COLOR;

            // Draw each line
            int lineY = rect.y + 2;
            for (OrderedText line : answers.get(i)) {
                context.drawText(this.textRenderer, line, rect.x+1, lineY, textColor, false);
                lineY += this.textRenderer.fontHeight;
            }
        }

        // Entity in the middle
        context.fill(centerX-33, centerY-55, centerX+33, centerY+55, 0xFF000000);
        drawEntity(context, startX, startY, mouseX, mouseY, centerX, centerY - 50, 40);

        super.render(context, mouseX, mouseY, delta);
    }

    public void drawTextCenter(DrawContext context, Text text, int x, int y) {
        drawTextCenter(context, text, x, y, TEXT_COLOR);
    }

    public void drawTextCenter(DrawContext context, Text text, int x, int y, int color) {
        context.drawText(this.textRenderer, text, x - this.textRenderer.getWidth(text)/2, y, color, false);
    }

    public void testDrawX(DrawContext context, int x) {
        context.fill(x, 0, x+1, this.height, 0xFFff00f2);
    }

    public void testDrawY(DrawContext context, int y) {
        context.fill(0, y, this.width, y+1, 0xFFff00f2);
    }

    private void drawEntity(DrawContext context, int i, int j, int mouseX, int mouseY, int x, int y, int size) {
        if (client == null) return;
        if (client.world == null) return;
        if (client.player == null) return;
        for (DisplayEntity.ItemDisplayEntity entity : client.world.getEntitiesByClass(DisplayEntity.ItemDisplayEntity.class, client.player.getBoundingBox().expand(5), entity->true)) {
            //TODO this draws all
            drawEntity(context, x-30, y-55, x+30, y+85, size, 0.0625F, mouseX, mouseY, entity);
        }
    }
    public static void drawEntity(DrawContext context, int x1, int y1, int x2, int y2, int size, float f, float mouseX, float mouseY, Entity entity) {
        float g = (float)(x1 + x2) / 2.0F;
        float h = (float)(y1 + y2) / 2.0F;
        context.enableScissor(x1, y1, x2, y2);
        Quaternionf quaternionf = (new Quaternionf()).rotateZ(3.1415927F);
        Quaternionf quaternionf2 = (new Quaternionf()).rotateX(0);
        quaternionf.mul(quaternionf2);
        float p = 1;//entity.getScale
        float l = entity.getYaw();
        float m = entity.getPitch();
        entity.setYaw(180);
        entity.setPitch(0);
        Vector3f vector3f = new Vector3f(0.0F, entity.getHeight() / 2.0F + f * p, 0.0F);
        float q = (float)size / p;
        drawEntity(context, g, h, q, vector3f, quaternionf, quaternionf2, entity);
        entity.setYaw(l);
        entity.setPitch(m);
        context.disableScissor();
    }

    public static void drawEntity(DrawContext context, float x, float y, float size, Vector3f vector3f, Quaternionf quaternionf, @Nullable Quaternionf quaternionf2, Entity entity) {
        context.getMatrices().push();
        context.getMatrices().translate((double)x, (double)y, 50.0);
        context.getMatrices().scale(size, size, -size);
        context.getMatrices().translate(vector3f.x, vector3f.y, vector3f.z);
        context.getMatrices().multiply(quaternionf);
        DiffuseLighting.method_34742();
        EntityRenderDispatcher entityRenderDispatcher = MinecraftClient.getInstance().getEntityRenderDispatcher();
        if (quaternionf2 != null) {
            entityRenderDispatcher.setRotation(quaternionf2.conjugate(new Quaternionf()).rotateY(3.1415927F));
        }

        entityRenderDispatcher.setRenderShadows(false);
        //? if <= 1.21 {
        RenderSystem.runAsFancy(() -> {
            entityRenderDispatcher.render(entity, 0.0, 0.0, 0.0, 0.0F, 1.0F, context.getMatrices(), context.getVertexConsumers(), 15728880);
        });
         //?} else {
        /*context.draw((vertexConsumers) -> {
            entityRenderDispatcher.render(entity, 0.0, 0.0, 0.0, 1.0F, context.getMatrices(), vertexConsumers, 15728880);
        });
        *///?}
        context.draw();
        entityRenderDispatcher.setRenderShadows(true);
        context.getMatrices().pop();
        DiffuseLighting.enableGuiDepthLighting();
    }
}
