package net.mat0u5.lifeseries.utils;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class TaskScheduler {

    // List to hold scheduled tasks
    private static final List<Task> tasks = new ArrayList<>();
    // Temporary list to queue tasks for the next tick
    private static final List<Task> newTasks = new ArrayList<>();

    // Method to schedule a task after a certain number of ticks
    public static void scheduleTask(int tickNumber, Runnable goal) {
        Task task = new Task(tickNumber, goal);
        newTasks.add(task); // Queue the task to be added later
    }

    // Register server tick event to check and run tasks
    public static void registerTickHandler() {
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            try {
                Iterator<Task> iterator = tasks.iterator(); // Create iterator for safe removal

                // Process the tasks for this tick
                while (iterator.hasNext()) {
                    Task task = iterator.next();
                    task.tickCount--;

                    // When tick count reaches 0, run the goal and mark as completed
                    if (task.tickCount <= 0) {
                        task.goal.run();
                        iterator.remove(); // Safely remove task after it has been executed
                    }
                }

                // Add new tasks that were queued for the next tick
                tasks.addAll(newTasks);
                newTasks.clear(); // Clear the temporary list
            }catch(Exception e){
                e.printStackTrace();
            }
        });
    }

    // Task class to hold information about each scheduled task
    public static class Task {
        private int tickCount; // Number of ticks until the task should run
        private final Runnable goal; // The goal (task) to execute

        public Task(int tickCount, Runnable goal) {
            this.tickCount = tickCount;
            this.goal = goal;
        }
    }
}
