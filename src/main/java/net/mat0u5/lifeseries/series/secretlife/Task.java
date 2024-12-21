package net.mat0u5.lifeseries.series.secretlife;

import net.minecraft.text.RawFilteredPair;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.List;

public class Task {
    public String task;
    public TaskType type;
    public Task(String task, TaskType type) {
        this.task = task;
        this.type = type;
    }
    public boolean isValid() {
        if (task == null) return false;
        if (task.isEmpty()) return false;
        return true;
    }
    public List<RawFilteredPair<Text>> getBookLines() {
        //TODO actually split the lines into pages.
        List<RawFilteredPair<Text>> lines = new ArrayList<>();
        lines.add(RawFilteredPair.of(Text.of(task)));
        return lines;
    }
    public int getDifficulty() {
        if (type == TaskType.EASY) return 1;
        if (type == TaskType.HARD) return 2;
        if (type == TaskType.RED) return 3;
        return 0;
    }
}
