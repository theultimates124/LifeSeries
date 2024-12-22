package net.mat0u5.lifeseries.series.secretlife;

import net.mat0u5.lifeseries.config.StringListManager;
import net.mat0u5.lifeseries.series.SessionAction;
import net.mat0u5.lifeseries.utils.*;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.WrittenBookContentComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.RawFilteredPair;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static net.mat0u5.lifeseries.Main.currentSeries;

public class TaskManager {
    public static SessionAction actionChooseTasks = new SessionAction(OtherUtils.minutesToTicks(1)) {
        @Override
        public void trigger() {
            chooseTasks(currentSeries.getAlivePlayers());
        }
    };
    public static List<String> easyTasks;
    public static List<String> hardTasks;
    public static List<String> redTasks;
    public static Random rnd = new Random();
    public static void initialize() {
        StringListManager configEasyTasks = new StringListManager("./config/lifeseries/secretlife","easy-tasks.json");
        StringListManager configHardTasks = new StringListManager("./config/lifeseries/secretlife","hard-tasks.json");
        StringListManager configRedTasks = new StringListManager("./config/lifeseries/secretlife","red-tasks.json");
        easyTasks = configEasyTasks.loadStrings();
        hardTasks = configHardTasks.loadStrings();
        redTasks = configRedTasks.loadStrings();
    }
    public static Task getRandomTask(TaskType type) {
        //TODO don't give out tasks that have already been selected.
        String selectedTask = "";
        if (type == TaskType.EASY && !easyTasks.isEmpty()) {
            selectedTask = easyTasks.get(rnd.nextInt(easyTasks.size()));
        }
        else if (type == TaskType.HARD && !hardTasks.isEmpty()) {
            selectedTask = hardTasks.get(rnd.nextInt(hardTasks.size()));
        }
        else if (type == TaskType.RED && !redTasks.isEmpty()) {
            selectedTask = redTasks.get(rnd.nextInt(redTasks.size()));
        }
        return new Task(selectedTask, type);
    }
    public static List<Task> getAllTasks(TaskType type) {
        List<Task> result = new ArrayList<>();
        List<String> tasks = easyTasks;
        if (type == TaskType.HARD) tasks = hardTasks;
        else if (type == TaskType.RED) tasks = redTasks;
        for (String taskStr : tasks) {
            Task task = new Task(taskStr, type);
            result.add(task);
        }
        return result;
    }
    public static ItemStack getTaskBook(ServerPlayerEntity player, Task task) {
        ItemStack book = new ItemStack(Items.WRITTEN_BOOK);
        WrittenBookContentComponent bookContent = new WrittenBookContentComponent(
            RawFilteredPair.of("§c"+player.getNameForScoreboard()+"'s Secret Task"),
                "Secret Keeper",
                0,
                task.getBookLines(),
                true
        );
        book.set(DataComponentTypes.WRITTEN_BOOK_CONTENT, bookContent);

        ItemStackUtils.setCustomComponentBoolean(book, "SecretTask", true);
        ItemStackUtils.setCustomComponentInt(book, "TaskDifficulty", task.getDifficulty());
        return book;
    }
    public static void assignRandomTaskToPlayer(ServerPlayerEntity player) {
        if (!currentSeries.isAlive(player)) return;
        TaskType type = TaskType.EASY;
        if (currentSeries.isOnLastLife(player)) type = TaskType.RED;
        Task task = getRandomTask(type);
        ItemStack book = getTaskBook(player, task);
        //TODO if player doesnt have space
        player.giveItemStack(book);
    }
    public static void assignRandomTasks() {
        for (ServerPlayerEntity player : PlayerUtils.getAllPlayers()) {
            if (!currentSeries.isAlive(player)) continue;
            assignRandomTaskToPlayer(player);
        }
    }
    public static void chooseTasks(List<ServerPlayerEntity> allowedPlayers) {
        //TODO add the whispering sound
        PlayerUtils.sendTitleToPlayers(allowedPlayers, Text.literal("Your secret is...").formatted(Formatting.RED),20,35,0);

        TaskScheduler.scheduleTask(40, () -> {
            PlayerUtils.playSoundToPlayers(allowedPlayers, SoundEvents.UI_BUTTON_CLICK.value());
            PlayerUtils.sendTitleToPlayers(allowedPlayers, Text.literal("3").formatted(Formatting.RED),0,35,0);
        });
        TaskScheduler.scheduleTask(70, () -> {
            PlayerUtils.playSoundToPlayers(allowedPlayers, SoundEvents.UI_BUTTON_CLICK.value());
            PlayerUtils.sendTitleToPlayers(allowedPlayers, Text.literal("2").formatted(Formatting.RED),0,35,0);
        });
        TaskScheduler.scheduleTask(100, () -> {
            PlayerUtils.playSoundToPlayers(allowedPlayers, SoundEvents.UI_BUTTON_CLICK.value());
            PlayerUtils.sendTitleToPlayers(allowedPlayers, Text.literal("1").formatted(Formatting.RED),0,35,0);
        });
        TaskScheduler.scheduleTask(130, () -> {
            for (ServerPlayerEntity player : allowedPlayers) {
                AnimationUtils.playTotemAnimation(player);
            }
        });
        TaskScheduler.scheduleTask(170, TaskManager::assignRandomTasks);
    }
    public static void temp(ServerPlayerEntity player) {
        int i = 0;
        Task.checkPlayerColors();
        for (Task task : getAllTasks(TaskType.RED)) {
            if (!task.isValid()) continue;
            ItemStack book = getTaskBook(player, task);
            player.giveItemStack(book);
        }
    }
}
