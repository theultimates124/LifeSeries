package net.mat0u5.lifeseries.series.secretlife;

import net.mat0u5.lifeseries.series.*;
import net.mat0u5.lifeseries.utils.ItemStackUtils;
import net.mat0u5.lifeseries.utils.OtherUtils;
import net.mat0u5.lifeseries.utils.PlayerUtils;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.List;
import java.util.Objects;

import static net.mat0u5.lifeseries.Main.currentSeries;

public class SecretLife extends Series {
    public static final double MAX_HEALTH = 60.0d;
    public ItemSpawner itemSpawner;
    SessionAction taskWarningAction = new SessionAction(OtherUtils.minutesToTicks(-5)) {
        @Override
        public void trigger() {
            OtherUtils.broadcastMessage(Text.literal("Session ends in 5 minutes, you better finish your secret tasks if you haven't!").formatted(Formatting.GOLD));
        }
    };

    @Override
    public SeriesList getSeries() {
        return SeriesList.SECRET_LIFE;
    }

    @Override
    public Blacklist createBlacklist() {
        return new SecretLifeBlacklist();
    }

    @Override
    public void initialize() {
        super.initialize();
        CUSTOM_ENCHANTMENT_TABLE_ALGORITHM = true;
        NO_HEALING = true;
        TaskManager.initialize();
        SecretLifeDatabase.loadLocations();
        initializeItemSpawner();
    }

    public void initializeItemSpawner() {
        itemSpawner = new ItemSpawner();
        itemSpawner.addItem(new ItemStack(Items.NETHERITE_UPGRADE_SMITHING_TEMPLATE), 10);
        itemSpawner.addItem(new ItemStack(Items.ANCIENT_DEBRIS), 10);
        itemSpawner.addItem(new ItemStack(Items.EXPERIENCE_BOTTLE, 16), 10);
        itemSpawner.addItem(new ItemStack(Items.PUFFERFISH_BUCKET), 10);
        itemSpawner.addItem(new ItemStack(Items.DIAMOND, 2), 10);
        itemSpawner.addItem(new ItemStack(Items.GOLD_BLOCK, 2), 10);
        itemSpawner.addItem(new ItemStack(Items.IRON_BLOCK, 2), 10);
        itemSpawner.addItem(new ItemStack(Items.COAL_BLOCK, 2), 10);
        itemSpawner.addItem(new ItemStack(Items.GOLDEN_APPLE), 10);
        itemSpawner.addItem(new ItemStack(Items.INFESTED_STONE, 16), 10);
        itemSpawner.addItem(new ItemStack(Items.SCULK_SHRIEKER, 2), 10);
        itemSpawner.addItem(new ItemStack(Items.SCULK_SENSOR, 8), 10);
        itemSpawner.addItem(new ItemStack(Items.TNT, 8), 10);
        itemSpawner.addItem(new ItemStack(Items.OBSIDIAN, 8), 10);
        itemSpawner.addItem(new ItemStack(Items.ARROW, 32), 10);

        //Potions
        //Enchanted Books
        //Spawn Eggs

        ItemStack endCrystal = new ItemStack(Items.END_CRYSTAL);
        ItemStackUtils.setCustomComponentBoolean(endCrystal, "IgnoreBlacklist", true);
        itemSpawner.addItem(endCrystal, 10);

        ItemStack mace = new ItemStack(Items.MACE);
        ItemStackUtils.setCustomComponentBoolean(mace, "IgnoreBlacklist", true);
        mace.setDamage(mace.getMaxDamage()-1);
        itemSpawner.addItem(mace, 3);

        ItemStack patat = new ItemStack(Items.POISONOUS_POTATO);
        patat.set(DataComponentTypes.CUSTOM_NAME,Text.of("§6§l§nThe Sacred Patat"));
        ItemStackUtils.addLoreToItemStack(patat,
                List.of(Text.of("§5§oEating this might help you. Or maybe not..."))
        );
        itemSpawner.addItem(patat, 100);
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
            setPlayerLives(player,3);
            setPlayerHealth(player, MAX_HEALTH);
            player.setHealth((float) MAX_HEALTH);
        }
        TaskManager.checkSecretLifePositions();
    }

    @Override
    public void sessionStart() {
        if (TaskManager.checkSecretLifePositions()) {
            super.sessionStart();
            activeActions.clear(); // To remove default 5 min warning
            activeActions.addAll(
                    List.of(TaskManager.actionChooseTasks, taskWarningAction)
            );
        }
    }

    @Override
    public void sessionEnd() {
        super.sessionEnd();
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

    public void removePlayerHealth(ServerPlayerEntity player, double health) {
        addPlayerHealth(player,-health);
    }

    public void removePlayerHealthClamped(ServerPlayerEntity player, double health) {
        addPlayerHealthClamped(player,-health);
    }

    public void addPlayerHealth(ServerPlayerEntity player, double health) {
        double currentHealth = player.getAttributeBaseValue(EntityAttributes.GENERIC_MAX_HEALTH);
        setPlayerHealth(player, currentHealth + health);
    }

    public void addPlayerHealthClamped(ServerPlayerEntity player, double health) {
        double currentHealth = player.getAttributeBaseValue(EntityAttributes.GENERIC_MAX_HEALTH);
        setPlayerHealthClamped(player, currentHealth + health);
    }

    public void setPlayerHealth(ServerPlayerEntity player, double health) {
        Objects.requireNonNull(player.getAttributeInstance(EntityAttributes.GENERIC_MAX_HEALTH)).setBaseValue(health);
        if (player.getMaxHealth() > player.getHealth() && !player.isDead()) {
            player.setHealth(player.getMaxHealth());
        }
    }

    public void setPlayerHealthClamped(ServerPlayerEntity player, double health) {
        if (health > MAX_HEALTH) health = MAX_HEALTH;
        setPlayerHealth(player, health);
    }

    public double getPlayerHealth(ServerPlayerEntity player) {
        return player.getAttributeBaseValue(EntityAttributes.GENERIC_MAX_HEALTH);
    }

    public double getRoundedHealth(ServerPlayerEntity player) {
        return Math.floor(player.getAttributeBaseValue(EntityAttributes.GENERIC_MAX_HEALTH)*10)/10.0;
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
