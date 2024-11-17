package net.mat0u5.lifeseries.series;

public abstract class SessionAction {
    public boolean hasTriggered = false;
    public int triggerAtTicks;
    public SessionAction(int triggerAtTicks) {
        this.triggerAtTicks = triggerAtTicks;
    }
    public boolean tick(int currentTick) {
        if (hasTriggered) return true;
        if (triggerAtTicks <= currentTick) {
            hasTriggered = true;
            trigger();
            return true;
        }
        return false;
    }
    public abstract void trigger();
}
