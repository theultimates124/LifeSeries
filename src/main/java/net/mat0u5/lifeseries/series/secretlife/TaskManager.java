package net.mat0u5.lifeseries.series.secretlife;

import net.mat0u5.lifeseries.config.StringListManager;
import net.mat0u5.lifeseries.series.SessionAction;
import net.mat0u5.lifeseries.utils.*;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.WrittenBookContentComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.command.SetBlockCommand;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.RawFilteredPair;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static net.mat0u5.lifeseries.Main.currentSeries;
import static net.mat0u5.lifeseries.Main.server;

public class TaskManager {
    public static BlockPos successButtonPos;
    public static BlockPos rerollButtonPos;
    public static BlockPos failButtonPos;
    public static BlockPos itemSpawnerPos;

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
        ItemStackUtils.setCustomComponentBoolean(book, "KillPermitted", task.killPermitted());
        return book;
    }

    public static void assignRandomTaskToPlayer(ServerPlayerEntity player, TaskType type) {
        if (!currentSeries.isAlive(player)) return;
        Task task = getRandomTask(type);
        ItemStack book = getTaskBook(player, task);
        //TODO if player doesnt have space
        player.giveItemStack(book);
    }

    public static void assignRandomTasks() {
        for (ServerPlayerEntity player : PlayerUtils.getAllPlayers()) {
            if (!currentSeries.isAlive(player)) continue;
            TaskType type = TaskType.EASY;
            if (currentSeries.isOnLastLife(player)) type = TaskType.RED;
            removePlayersTaskBook(player);
            assignRandomTaskToPlayer(player, type);
        }
    }

    public static void chooseTasks(List<ServerPlayerEntity> allowedPlayers) {
        PlayerUtils.sendTitleToPlayers(allowedPlayers, Text.literal("Your secret is...").formatted(Formatting.RED),20,35,0);

        TaskScheduler.scheduleTask(40, () -> {
            PlayerUtils.playSoundToPlayers(allowedPlayers, SoundEvents.UI_BUTTON_CLICK.value());
            PlayerUtils.sendTitleToPlayers(allowedPlayers, Text.literal("3").formatted(Formatting.RED),0,35,0);
        });
        TaskScheduler.scheduleTask(70, () -> {
            PlayerUtils.sendTitleToPlayers(allowedPlayers, Text.literal("2").formatted(Formatting.RED),0,35,0);
            PlayerUtils.playSoundToPlayers(allowedPlayers, SoundEvent.of(Identifier.of("minecraft","secretlife_task")));
        });
        TaskScheduler.scheduleTask(105, () -> {
            PlayerUtils.sendTitleToPlayers(allowedPlayers, Text.literal("1").formatted(Formatting.RED),0,35,0);
        });
        TaskScheduler.scheduleTask(130, () -> {
            for (ServerPlayerEntity player : allowedPlayers) {
                AnimationUtils.playTotemAnimation(player);
            }
        });
        TaskScheduler.scheduleTask(165, TaskManager::assignRandomTasks);
    }

    public static ItemStack getPlayersTaskBook(ServerPlayerEntity player) {
        for (ItemStack item : PlayerUtils.getPlayerInventory(player)) {
            if (ItemStackUtils.hasCustomComponentEntry(item,"SecretTask")) return item;
        }
        return null;
    }

    public static void removePlayersTaskBook(ServerPlayerEntity player) {
        for (ItemStack item : PlayerUtils.getPlayerInventory(player)) {
            if (ItemStackUtils.hasCustomComponentEntry(item,"SecretTask")) {
                PlayerUtils.clearItemStack(player, item);
            }
        }
    }

    public static boolean getPlayerKillPermitted(ServerPlayerEntity player) {
        ItemStack item = getPlayersTaskBook(player);
        if (item == null) return false;
        if (!ItemStackUtils.hasCustomComponentEntry(item,"SecretTask")) return false;
        if (!ItemStackUtils.hasCustomComponentEntry(item,"TaskDifficulty")) return false;
        if (!ItemStackUtils.hasCustomComponentEntry(item,"KillPermitted")) return false;
        return ItemStackUtils.getCustomComponentBoolean(item, "KillPermitted");
    }

    public static TaskType getPlayersTaskType(ServerPlayerEntity player) {
        ItemStack item = getPlayersTaskBook(player);
        if (item == null) return null;
        if (!ItemStackUtils.hasCustomComponentEntry(item,"SecretTask")) return null;
        if (!ItemStackUtils.hasCustomComponentEntry(item,"TaskDifficulty")) return null;
        int difficulty = ItemStackUtils.getCustomComponentInt(item, "TaskDifficulty");
        if (difficulty == 1) return TaskType.EASY;
        if (difficulty == 2) return TaskType.HARD;
        if (difficulty == 3) return TaskType.RED;
        return null;
    }

    public static void addHealthThenItems(ServerPlayerEntity player, int addHealth) {
        SecretLife series = (SecretLife) currentSeries;
        double currentHealth = series.getPlayerHealth(player);
        if (currentHealth > SecretLife.MAX_HEALTH) currentHealth = SecretLife.MAX_HEALTH;
        int rounded = (int) Math.floor(currentHealth);
        int remainderToMax = (int) SecretLife.MAX_HEALTH - rounded;

        if (addHealth <= remainderToMax && remainderToMax != 0) {
            series.addPlayerHealth(player, addHealth);
        }
        else {
            if (remainderToMax != 0) series.setPlayerHealth(player, SecretLife.MAX_HEALTH);
            int itemsNum = (addHealth - remainderToMax)/2;
            if (itemsNum == 0) return;
            Vec3d spawnPos = itemSpawnerPos.toCenterPos();
            for (int i = 0; i <= itemsNum; i++) {
                if (i == 0) continue;
                TaskScheduler.scheduleTask(3*i, () -> {
                    server.getOverworld().playSound(null, spawnPos.getX(), spawnPos.getY(), spawnPos.getZ(), SoundEvents.ENTITY_ITEM_PICKUP, SoundCategory.PLAYERS, 1.0F, 1.0F);
                    series.itemSpawner.spawnRandomItemForPlayer(server.getOverworld(), spawnPos, player);
                });
            }
        }
    }

    public static void succeedTask(ServerPlayerEntity player) {
        //TODO animations
        SecretLife series = (SecretLife) currentSeries;
        TaskType type = getPlayersTaskType(player);
        if (type == null) return;
        removePlayersTaskBook(player);

        if (type == TaskType.EASY) {
            addHealthThenItems(player, 20);
            return;
        }
        if (type == TaskType.HARD) {
            addHealthThenItems(player, 40);
            return;
        }
        if (type == TaskType.RED) {
            addHealthThenItems(player, 10);
            return;
        }

        if (series.isOnLastLife(player)) {
            //TODO
            assignRandomTaskToPlayer(player, TaskType.RED);
        }
    }

    public static void rerollTask(ServerPlayerEntity player) {
        //TODO animations
        SecretLife series = (SecretLife) currentSeries;
        TaskType type = getPlayersTaskType(player);
        if (type == null) return;
        if (type == TaskType.EASY) {
            removePlayersTaskBook(player);
            TaskType newType = TaskType.HARD;
            if (series.isOnLastLife(player)) newType = TaskType.RED;
            assignRandomTaskToPlayer(player, newType);
        }
    }

    public static void failTask(ServerPlayerEntity player) {
        //TODO animations
        SecretLife series = (SecretLife) currentSeries;
        TaskType type = getPlayersTaskType(player);
        if (type == null) return;
        removePlayersTaskBook(player);

        if (type == TaskType.HARD) {
            series.removePlayerHealth(player, 20);
            return;
        }
        if (type == TaskType.RED) {
            series.removePlayerHealth(player, 5);
            return;
        }

        if (series.isOnLastLife(player)) {
            //TODO
            assignRandomTaskToPlayer(player, TaskType.RED);
        }
    }

    public static void positionFound(BlockPos pos) {
        if (successButtonPos == null) {
            successButtonPos = pos;
            OtherUtils.broadcastMessage(Text.literal("§a[SecretLife setup 1/4] Location set."));
        }
        else if (rerollButtonPos == null) {
            rerollButtonPos = pos;
            OtherUtils.broadcastMessage(Text.literal("§a[SecretLife setup 2/4] Location set."));
        }
        else if (failButtonPos == null) {
            failButtonPos = pos;
            OtherUtils.broadcastMessage(Text.literal("§a[SecretLife setup 3/4] Location set."));
        }
        else if (itemSpawnerPos == null) {
            itemSpawnerPos = pos;
            OtherUtils.broadcastMessage(Text.literal("§a[SecretLife] All locations have been set. If you wish to change them in the future, use §2'/secretlife changeLocations'"));
        }
        SecretLifeDatabase.saveLocations();
        checkSecretLifePositions();
    }

    public static boolean searchingForLocations = false;
    public static boolean checkSecretLifePositions() {
        if (successButtonPos == null) {
            OtherUtils.broadcastMessage(Text.literal("§c[SecretLife setup 1/4] Location for the secret keeper task §4success button§c was not found. The next button you click will be set as the location."));
            searchingForLocations = true;
            return false;
        }
        if (rerollButtonPos == null) {
            OtherUtils.broadcastMessage(Text.literal("§c[SecretLife setup 2/4] Location for the secret keeper task §4re-roll button§c was not found. The next button you click will be set as the location."));
            searchingForLocations = true;
            return false;
        }
        if (failButtonPos == null) {
            OtherUtils.broadcastMessage(Text.literal("§c[SecretLife setup 3/4] Location for the secret keeper task §4fail button§c was not found. The next button you click will be set as the location."));
            searchingForLocations = true;
            return false;
        }
        if (itemSpawnerPos == null) {
            OtherUtils.broadcastMessage(Text.literal("§c[SecretLife setup 4/4] Location for the secret keeper task §4item spawn block§c was not found. Please place a bedrock block at the desired spot to mark it."));
            searchingForLocations = true;
            return false;
        }
        searchingForLocations = false;
        return true;
    }

    public static void onBlockUse(ServerPlayerEntity player, ServerWorld world, BlockHitResult hitResult) {
        BlockPos pos = hitResult.getBlockPos();
        if (world.getBlockState(pos).getBlock().getName().getString().contains("Button")) {
            if (searchingForLocations) {
                positionFound(pos);
            }
            else {
                if (pos.equals(successButtonPos)) {
                    succeedTask(player);
                }
                else if (pos.equals(rerollButtonPos)) {
                    rerollTask(player);
                }
                else if (pos.equals(failButtonPos)) {
                    failTask(player);
                }
            }
        }
        if (!searchingForLocations) return;
        BlockPos placePos = pos.offset(hitResult.getSide());
        TaskScheduler.scheduleTask(1, () -> {
            if (world.getBlockState(placePos).getBlock().getName().getString().equalsIgnoreCase("Bedrock")) {
                positionFound(placePos);
                world.breakBlock(placePos, false);
            }
        });
    }
}
