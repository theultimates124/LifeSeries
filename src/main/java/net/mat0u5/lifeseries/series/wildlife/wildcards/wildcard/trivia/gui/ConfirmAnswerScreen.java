package net.mat0u5.lifeseries.series.wildlife.wildcards.wildcard.trivia.gui;


import net.mat0u5.lifeseries.series.wildlife.wildcards.wildcard.trivia.Trivia;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class ConfirmAnswerScreen extends Screen {
    private static final Identifier BACKGROUND_TEXTURE = Identifier.of("lifeseries","textures/gui/trivia_answer.png");

    public static final int TEXT_COLOR = 0x3c3c3c;

    private static final int BG_WIDTH = 148;
    private static final int BG_HEIGHT = 67;

    private final QuizScreen parent;
    private final int answerIndex;

    public ConfirmAnswerScreen(QuizScreen parent, int answerIndex) {
        super(Text.literal("Confirm Answer"));
        this.parent = parent;
        this.answerIndex = answerIndex;
    }

    @Override
    public void renderBackground(DrawContext context, int mouseX, int mouseY, float delta) {}

    @Override
    protected void init() {
        super.init();
        int centerX = (this.width - BG_WIDTH) / 2;
        int centerY = (this.height - BG_HEIGHT) / 2;

        // "Submit" button (using default button look)
        this.addDrawableChild(
                ButtonWidget.builder(Text.literal("Confirm"), btn -> {
                            // Handle answer submission logic here
                            this.client.setScreen(null);
                            Trivia.sendAnswer(answerIndex);
                        })
                        .position(centerX + 9, centerY + BG_HEIGHT - 25)
                        .size(60, 20)
                        .build()
        );

        // "Cancel" button returns to the quiz screen
        this.addDrawableChild(
                ButtonWidget.builder(Text.literal("Cancel"), btn -> {
                            this.client.setScreen(parent);
                        })
                        .position(centerX + 79, centerY + BG_HEIGHT - 25)
                        .size(60, 20)
                        .build()
        );
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        this.renderBackground(context, mouseX, mouseY, delta);
        int centerX = (this.width - BG_WIDTH) / 2;
        int centerY = (this.height - BG_HEIGHT) / 2;
        //? if <= 1.21 {
        context.drawTexture(BACKGROUND_TEXTURE, centerX, centerY, 0, 0, BG_WIDTH, BG_HEIGHT);
        //?} else {
        /*context.drawTexture(RenderLayer::getGuiTextured, BACKGROUND_TEXTURE, centerX, centerY, 0, 0, BG_WIDTH, BG_HEIGHT, 256, 256);
        *///?}

        // Draw the prompt text (centered)
        String prompt = "Submit answer?";
        int textWidth = textRenderer.getWidth(prompt);
        int textX = centerX + (BG_WIDTH - textWidth) / 2;
        int textY = centerY + 20;
        context.drawText(textRenderer, prompt, textX, textY, TEXT_COLOR, false);

        super.render(context, mouseX, mouseY, delta);
    }
}