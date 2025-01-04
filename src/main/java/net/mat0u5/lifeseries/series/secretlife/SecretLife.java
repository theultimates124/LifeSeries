package net.mat0u5.lifeseries.series.secretlife;

import net.mat0u5.lifeseries.config.ConfigManager;
import net.mat0u5.lifeseries.series.*;
import net.mat0u5.lifeseries.utils.ItemStackUtils;
import net.mat0u5.lifeseries.utils.OtherUtils;
import net.mat0u5.lifeseries.utils.PlayerUtils;
import net.mat0u5.lifeseries.utils.TaskScheduler;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.component.type.PotionContentsComponent;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.potion.Potions;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static net.mat0u5.lifeseries.Main.currentSeries;
import static net.mat0u5.lifeseries.Main.seriesConfig;

public class SecretLife extends Series {
    public static double MAX_HEALTH = 60.0d;
    public ItemSpawner itemSpawner;
    SessionAction taskWarningAction = new SessionAction(OtherUtils.minutesToTicks(-5)+1) {
        @Override
        public void trigger() {
            OtherUtils.broadcastMessage(Text.literal("Go submit / fail your secret tasks if you haven't!").formatted(Formatting.GRAY));
        }
    };
    SessionAction taskWarningAction2 = new SessionAction(OtherUtils.minutesToTicks(-30)+1) {
        @Override
        public void trigger() {
            OtherUtils.broadcastMessage(Text.literal("You better start finishing your secret tasks if you haven't already!").formatted(Formatting.GRAY));
        }
    };

    @Override
    public SeriesList getSeries() {
        return SeriesList.SECRET_LIFE;
    }

    @Override
    public ConfigManager getConfig() {
        return new SecretLifeConfig();
    }

    @Override
    public void initialize() {
        super.initialize();
        NO_HEALING = true;
        TaskManager.initialize();
        SecretLifeDatabase.loadLocations();
        initializeItemSpawner();
    }

    @Override
    public void reload() {
        MAX_HEALTH = seriesConfig.getOrCreateDouble("max_player_health", 60.0d);
        TaskManager.EASY_SUCCESS = seriesConfig.getOrCreateInt("task_health_easy_pass", 20);
        TaskManager.EASY_FAIL = seriesConfig.getOrCreateInt("task_health_easy_fail", 0);
        TaskManager.HARD_SUCCESS = seriesConfig.getOrCreateInt("task_health_hard_pass", 40);
        TaskManager.HARD_FAIL = seriesConfig.getOrCreateInt("task_health_hard_fail", -20);
        TaskManager.RED_SUCCESS = seriesConfig.getOrCreateInt("task_health_red_pass", 10);
        TaskManager.RED_FAIL = seriesConfig.getOrCreateInt("task_health_red_fail", -5);
    }

    @Override
    public void onPlayerRespawn(ServerPlayerEntity oldPlayer) {
        TaskScheduler.scheduleTask(1, () -> {
            ServerPlayerEntity player = oldPlayer.server.getPlayerManager().getPlayer(oldPlayer.getUuid());
            TaskType type = TaskManager.getPlayersTaskType(player);
            if (isOnLastLife(player) && TaskManager.submittedOrFailed.contains(player.getUuid()) && type == null) {
                TaskManager.chooseTasks(List.of(player), TaskType.RED);
            }
        });
    }

    public void initializeItemSpawner() {
        itemSpawner = new ItemSpawner();
        itemSpawner.addItem(new ItemStack(Items.NETHERITE_UPGRADE_SMITHING_TEMPLATE), 10);
        itemSpawner.addItem(new ItemStack(Items.ANCIENT_DEBRIS), 10);
        itemSpawner.addItem(new ItemStack(Items.EXPERIENCE_BOTTLE, 16), 10);
        itemSpawner.addItem(new ItemStack(Items.PUFFERFISH_BUCKET), 10);
        itemSpawner.addItem(new ItemStack(Items.DIAMOND, 2), 20);
        itemSpawner.addItem(new ItemStack(Items.GOLD_BLOCK, 2), 20);
        itemSpawner.addItem(new ItemStack(Items.IRON_BLOCK, 2), 20);
        itemSpawner.addItem(new ItemStack(Items.COAL_BLOCK, 2), 10);
        itemSpawner.addItem(new ItemStack(Items.GOLDEN_APPLE), 10);
        itemSpawner.addItem(new ItemStack(Items.INFESTED_STONE, 16), 7);
        itemSpawner.addItem(new ItemStack(Items.SCULK_SHRIEKER, 2), 10);
        itemSpawner.addItem(new ItemStack(Items.SCULK_SENSOR, 8), 10);
        itemSpawner.addItem(new ItemStack(Items.TNT, 4), 10);
        itemSpawner.addItem(new ItemStack(Items.OBSIDIAN, 8), 10);
        itemSpawner.addItem(new ItemStack(Items.ARROW, 32), 10);
        itemSpawner.addItem(new ItemStack(Items.WOLF_ARMOR), 10);
        itemSpawner.addItem(new ItemStack(Items.BUNDLE), 10);
        itemSpawner.addItem(new ItemStack(Items.ENDER_PEARL, 2), 10);
        itemSpawner.addItem(new ItemStack(Items.BOOKSHELF, 4), 10);
        itemSpawner.addItem(new ItemStack(Items.SWEET_BERRIES, 16), 10);

        //Potions
        ItemStack pot = new ItemStack(Items.POTION);
        ItemStack pot2 = new ItemStack(Items.POTION);
        ItemStack pot3 = new ItemStack(Items.POTION);
        pot.set(DataComponentTypes.POTION_CONTENTS, new PotionContentsComponent(Potions.INVISIBILITY));
        pot2.set(DataComponentTypes.POTION_CONTENTS, new PotionContentsComponent(Potions.SLOW_FALLING));
        pot3.set(DataComponentTypes.POTION_CONTENTS, new PotionContentsComponent(Potions.FIRE_RESISTANCE));
        itemSpawner.addItem(pot, 10);
        itemSpawner.addItem(pot2, 10);
        itemSpawner.addItem(pot3, 10);

        //Enchanted Books
        itemSpawner.addItem(ItemStackUtils.createEnchantedBook(Enchantments.PROTECTION, 3), 10);
        itemSpawner.addItem(ItemStackUtils.createEnchantedBook(Enchantments.FEATHER_FALLING, 3), 10);
        itemSpawner.addItem(ItemStackUtils.createEnchantedBook(Enchantments.SILK_TOUCH, 1), 10);
        itemSpawner.addItem(ItemStackUtils.createEnchantedBook(Enchantments.FORTUNE, 3), 10);
        itemSpawner.addItem(ItemStackUtils.createEnchantedBook(Enchantments.LOOTING, 3), 10);
        itemSpawner.addItem(ItemStackUtils.createEnchantedBook(Enchantments.EFFICIENCY, 4), 10);


        //Spawn Eggs
        itemSpawner.addItem(new ItemStack(Items.WOLF_SPAWN_EGG), 15);
        itemSpawner.addItem(new ItemStack(Items.PANDA_SPAWN_EGG), 5);
        itemSpawner.addItem(new ItemStack(Items.SNIFFER_SPAWN_EGG), 3);
        itemSpawner.addItem(new ItemStack(Items.TURTLE_SPAWN_EGG), 5);

        ItemStack camel = new ItemStack(Items.CAMEL_SPAWN_EGG);
        ItemStack zombieHorse = new ItemStack(Items.ZOMBIE_HORSE_SPAWN_EGG);
        ItemStack skeletonHorse = new ItemStack(Items.SKELETON_HORSE_SPAWN_EGG);
        NbtCompound saddleItemComp = new NbtCompound();
        saddleItemComp.putInt("Count", 1);
        saddleItemComp.putString("id", "saddle");

        NbtCompound nbtCompSkeleton = new NbtCompound();
        nbtCompSkeleton.putInt("Tame", 1);
        nbtCompSkeleton.putString("id", "skeleton_horse");
        nbtCompSkeleton.put("SaddleItem", saddleItemComp);
        NbtComponent nbtSkeleton = NbtComponent.of(nbtCompSkeleton);

        NbtCompound nbtCompZombie= new NbtCompound();
        nbtCompZombie.putInt("Tame", 1);
        nbtCompZombie.putString("id", "zombie_horse");
        nbtCompZombie.put("SaddleItem", saddleItemComp);
        NbtComponent nbtZombie = NbtComponent.of(nbtCompZombie);

        NbtCompound nbtCompCamel = new NbtCompound();
        nbtCompCamel.putInt("Tame", 1);
        nbtCompCamel.putString("id", "camel");
        nbtCompCamel.put("SaddleItem", saddleItemComp);
        NbtComponent nbtCamel= NbtComponent.of(nbtCompCamel);

        zombieHorse.set(DataComponentTypes.ENTITY_DATA, nbtZombie);
        skeletonHorse.set(DataComponentTypes.ENTITY_DATA, nbtSkeleton);
        camel.set(DataComponentTypes.ENTITY_DATA, nbtCamel);
        itemSpawner.addItem(zombieHorse, 10);
        itemSpawner.addItem(skeletonHorse, 10);
        itemSpawner.addItem(camel, 10);

        //Other Stuff
        ItemStack endCrystal = new ItemStack(Items.END_CRYSTAL);
        ItemStackUtils.setCustomComponentBoolean(endCrystal, "IgnoreBlacklist", true);
        itemSpawner.addItem(endCrystal, 10);

        ItemStack mace = new ItemStack(Items.MACE);
        ItemStackUtils.setCustomComponentBoolean(mace, "IgnoreBlacklist", true);
        ItemStackUtils.setCustomComponentBoolean(mace, "NoMending", true);
        mace.setDamage(mace.getMaxDamage()-1);
        itemSpawner.addItem(mace, 1);

        ItemStack patat = new ItemStack(Items.POISONOUS_POTATO);
        patat.set(DataComponentTypes.CUSTOM_NAME,Text.of("§6§l§nThe Sacred Patat"));
        ItemStackUtils.addLoreToItemStack(patat,
                List.of(Text.of("§5§oEating this might help you. Or maybe not..."))
        );
        itemSpawner.addItem(patat, 1);
    }

    @Override
    public String getResourcepackURL() {
        return "https://github.com/Mat0u5/LifeSeries-Resources/releases/download/release-secretlife-88c7bc3cf13c71a3325103851754755e11ef3280/RP.zip";
    }

    @Override
    public String getResourcepackSHA1() {
        return "10cafd881ae1c8cdbc76b2627e175657cf3c7b52";
    }

    @Override
    public void onPlayerDamage(ServerPlayerEntity player, DamageSource source, float amount) {
        syncPlayerHealth(player);
    }

    @Override
    public void onPlayerDeath(ServerPlayerEntity player, DamageSource source) {
        super.onPlayerDeath(player, source);
        setPlayerHealth(player, MAX_HEALTH);
    }

    @Override
    public void onPlayerJoin(ServerPlayerEntity player) {
        super.onPlayerJoin(player);
        if (!hasAssignedLives(player)) {
            int lives = seriesConfig.getOrCreateInt("default_lives", 3);
            setPlayerLives(player, lives);
            setPlayerHealth(player, MAX_HEALTH);
            player.setHealth((float) MAX_HEALTH);
        }
        TaskManager.checkSecretLifePositions();
        if (TaskManager.tasksChosen && !TaskManager.tasksChosenFor.contains(player.getUuid())) {
            TaskScheduler.scheduleTask(100, () -> {
                TaskManager.chooseTasks(List.of(player), null);
            });
        }
    }

    @Override
    public boolean sessionStart() {
        if (TaskManager.checkSecretLifePositions()) {
            if (super.sessionStart()) {
                activeActions.addAll(
                        List.of(TaskManager.actionChooseTasks, taskWarningAction, taskWarningAction2)
                );
                SecretLifeCommands.playersGiven.clear();
                TaskManager.tasksChosen = false;
                TaskManager.tasksChosenFor.clear();
                TaskManager.submittedOrFailed.clear();
                return true;
            }
            return false;
        }
        return false;
    }

    @Override
    public void sessionEnd() {
        super.sessionEnd();
        List<String> playersWithTaskBooks = new ArrayList<>();
        for (ServerPlayerEntity player : getNonRedPlayers()) {
            if (!isAlive(player)) continue;
            if (TaskManager.submittedOrFailed.contains(player.getUuid())) continue;
            playersWithTaskBooks.add(player.getNameForScoreboard());
        }
        if (!playersWithTaskBooks.isEmpty()) {
            boolean isOne = playersWithTaskBooks.size() == 1;
            String playerNames = String.join(", ", playersWithTaskBooks);
            OtherUtils.broadcastMessageToAdmins(Text.of("§4"+playerNames+"§c still " + (isOne?"has":"have") + " not submitted / failed any tasks this session."));
        }
    }

    @Override
    public void onPlayerKilledByPlayer(ServerPlayerEntity victim, ServerPlayerEntity killer) {
        if (isAllowedToAttack(killer, victim)) {
            if (currentSeries.isOnLastLife(killer)) {
                addPlayerHealth(killer, 20);
                PlayerUtils.sendTitle(killer, Text.literal("+10 Hearts").formatted(Formatting.RED), 0, 40, 20);
            }
            return;
        }
        OtherUtils.broadcastMessageToAdmins(Text.of("§c [Unjustified Kill?] §f"+victim.getNameForScoreboard() + " was killed by "
                +killer.getNameForScoreboard() + ". The kill was not permitted with a task."));
    }

    @Override
    public boolean isAllowedToAttack(ServerPlayerEntity attacker, ServerPlayerEntity victim) {
        if (currentSeries.isOnLastLife(attacker)) return true;
        if (attacker.getPrimeAdversary() == victim && (currentSeries.isOnLastLife(victim))) return true;
        return false;
    }

    @Override
    public void tick(MinecraftServer server) {
        super.tick(server);
        TaskManager.tick();
    }

    public void removePlayerHealth(ServerPlayerEntity player, double health) {
        addPlayerHealth(player,-health);
    }

    public void addPlayerHealth(ServerPlayerEntity player, double health) {
        //? if <=1.21 {
        double currentHealth = player.getAttributeBaseValue(EntityAttributes.GENERIC_MAX_HEALTH);
         //?} else
        /*double currentHealth = player.getAttributeBaseValue(EntityAttributes.MAX_HEALTH);*/
        setPlayerHealth(player, currentHealth + health);
    }

    public void setPlayerHealth(ServerPlayerEntity player, double health) {
        if (health < 0.1) health = 0.1;
        //? if <=1.21 {
        Objects.requireNonNull(player.getAttributeInstance(EntityAttributes.GENERIC_MAX_HEALTH)).setBaseValue(health);
         //?} else
        /*Objects.requireNonNull(player.getAttributeInstance(EntityAttributes.MAX_HEALTH)).setBaseValue(health);*/

        if (player.getMaxHealth() > player.getHealth() && !player.isDead()) {
            player.setHealth(player.getMaxHealth());
        }
    }

    public double getPlayerHealth(ServerPlayerEntity player) {
        //? if <=1.21 {
        return player.getAttributeBaseValue(EntityAttributes.GENERIC_MAX_HEALTH);
         //?} else
        /*return player.getAttributeBaseValue(EntityAttributes.MAX_HEALTH);*/
    }

    public double getRoundedHealth(ServerPlayerEntity player) {
        //? if <=1.21 {
        return Math.floor(player.getAttributeBaseValue(EntityAttributes.GENERIC_MAX_HEALTH)*10)/10.0;
         //?} else
        /*return Math.floor(player.getAttributeBaseValue(EntityAttributes.MAX_HEALTH)*10)/10.0;*/
    }

    public void syncPlayerHealth(ServerPlayerEntity player) {
        setPlayerHealth(player, player.getHealth());
    }

    public void syncAllPlayerHealth() {
        for (ServerPlayerEntity player : PlayerUtils.getAllPlayers()) {
            setPlayerHealth(player, player.getHealth());
        }
    }

    public void resetPlayerHealth(ServerPlayerEntity player) {
        setPlayerHealth(player, MAX_HEALTH);
    }

    public void resetAllPlayerHealth() {
        for (ServerPlayerEntity player : PlayerUtils.getAllPlayers()) {
            resetPlayerHealth(player);
        }
    }
}
