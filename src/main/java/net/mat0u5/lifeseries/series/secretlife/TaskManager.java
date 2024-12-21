package net.mat0u5.lifeseries.series.secretlife;

import net.mat0u5.lifeseries.config.StringListManager;
import net.mat0u5.lifeseries.utils.ItemStackUtils;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.component.type.WrittenBookContentComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.filter.FilteredMessage;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.RawFilteredPair;

import java.util.List;
import java.util.Random;

import static net.mat0u5.lifeseries.Main.currentSeries;

public class TaskManager {
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
    public static ItemStack getTaskBook(ServerPlayerEntity player, Task task) {
        ItemStack book = new ItemStack(Items.WRITTEN_BOOK);
        WrittenBookContentComponent bookContent = new WrittenBookContentComponent(
            RawFilteredPair.of("§c"+player.getNameForScoreboard()+"'s Secret Task"),
                "Mat0u5",
                0,
                task.getBookLines(),
                true
        );
        book.set(DataComponentTypes.WRITTEN_BOOK_CONTENT, bookContent);

        ItemStackUtils.setCustomComponentBoolean(book, "SecretTask", true);
        ItemStackUtils.setCustomComponentInt(book, "TaskDifficulty", task.getDifficulty());
        return book;
    }
    public static void assignRandomTask(ServerPlayerEntity player) {
        if (!currentSeries.isAlive(player)) return;
        TaskType type = TaskType.EASY;
        if (currentSeries.isOnLastLife(player)) type = TaskType.RED;
        Task task = getRandomTask(type);
        ItemStack book = getTaskBook(player, task);
        //TODO if player doesnt have space
        player.giveItemStack(book);
    }
}
