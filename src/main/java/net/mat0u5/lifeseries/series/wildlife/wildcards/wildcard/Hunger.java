package net.mat0u5.lifeseries.series.wildlife.wildcards.wildcard;

import net.mat0u5.lifeseries.series.wildlife.wildcards.Wildcard;
import net.mat0u5.lifeseries.series.wildlife.wildcards.Wildcards;
import net.mat0u5.lifeseries.utils.OtherUtils;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;

import static net.mat0u5.lifeseries.Main.currentSession;

public class Hunger extends Wildcard {
    public static int SWITCH_DELAY = 100;
    private int currentDelay = 0;

    @Override
    public Wildcards getType() {
        return Wildcards.HUNGER;
    }

    @Override
    public void tick() {
        if (currentSession.statusStarted()) {
            currentDelay--;
        }
        if (currentDelay <= 0) {
            currentDelay = SWITCH_DELAY;
            newFoodRules();
        }
    }

    public void newFoodRules() {
        OtherUtils.broadcastMessage(Text.of("newFoodRules"));
    }

    public static void handItemStack(ItemStack itemStack) {

    }
}
