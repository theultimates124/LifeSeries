package net.mat0u5.lifeseries.series.wildlife.wildcards.wildcard;

import net.mat0u5.lifeseries.series.wildlife.wildcards.Wildcard;
import net.mat0u5.lifeseries.series.wildlife.wildcards.WildcardManager;
import net.mat0u5.lifeseries.series.wildlife.wildcards.Wildcards;
import net.mat0u5.lifeseries.utils.OtherUtils;
import net.mat0u5.lifeseries.utils.PlayerUtils;
import net.mat0u5.lifeseries.utils.TaskScheduler;
import net.minecraft.entity.*;
import net.minecraft.entity.ai.brain.MemoryModuleType;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.mob.WardenEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Unit;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.GameRules;
import net.minecraft.world.chunk.Chunk;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.*;

import static net.mat0u5.lifeseries.Main.*;

public class MobSwap extends Wildcard {
    public static int activatedAt = -1;
    public static int lastDiv0 = 0;
    public static int lastDiv = 0;
    public static int mobsLeftDiv = 0;
    public static int swaps = -1;

    public static double BOSS_CHANCE_MULTIPLIER = 1;
    public static int MIN_DELAY = 2400;
    public static int MAX_DELAY = 7200;
    public static int SPAWN_MOBS = 250;

    public static int mobcapMonster = -1;
    public static int mobcapAnimal = -1;
    public static double bossChance = 0;
    public static boolean fastAnimalSpawn = false;
    public static List<Integer> eggSounds = List.of(0, 20, 35, 48, 59, 70, 80, 89, 97, 104, 110, 115, 119, 122, 124, 126, 128, 129, 130, 131, 132, 133, 134, 135, 136, 137, 138, 139, 140);
    private static Random rnd = new Random();

    public static HashMap<EntityType<?>, Integer> entityEntries = new HashMap<>() {{
        put(EntityType.ALLAY, 1);
        put(EntityType.BAT, 2);
        put(EntityType.CAT, 3);
        put(EntityType.CHICKEN, 4);
        put(EntityType.RABBIT, 5);
        put(EntityType.SQUID, 6);
        put(EntityType.TROPICAL_FISH, 7);
        put(EntityType.TURTLE, 8);
        put(EntityType.COD, 9);
        put(EntityType.COW, 10);

        put(EntityType.DONKEY, 11);
        put(EntityType.GLOW_SQUID, 12);
        put(EntityType.MOOSHROOM, 13);
        put(EntityType.MULE, 14);
        put(EntityType.PIG, 15);
        put(EntityType.SHEEP, 16);
        put(EntityType.SNIFFER, 17);
        put(EntityType.WANDERING_TRADER, 19);

        put(EntityType.FROG, 20);
        put(EntityType.CAMEL, 20);
        put(EntityType.HORSE, 22);
        put(EntityType.OCELOT, 24);
        put(EntityType.PARROT, 26);
        put(EntityType.AXOLOTL, 28);
        put(EntityType.FOX, 30);
        put(EntityType.GOAT, 32);
        put(EntityType.PANDA, 34);
        put(EntityType.LLAMA, 36);
        put(EntityType.DOLPHIN, 38);
        put(EntityType.BEE, 40);
        put(EntityType.WOLF, 42);
        put(EntityType.TRADER_LLAMA, 44);

        put(EntityType.POLAR_BEAR, 45);
        put(EntityType.PIGLIN, 48);
        put(EntityType.ZOMBIFIED_PIGLIN, 50);
        put(EntityType.SILVERFISH, 52);
        put(EntityType.SLIME, 54);
        put(EntityType.SPIDER, 56);
        put(EntityType.ENDERMAN, 58);
        put(EntityType.PHANTOM, 60);
        put(EntityType.PILLAGER, 62);

        put(EntityType.CAVE_SPIDER, 64);
        put(EntityType.DROWNED, 66);
        put(EntityType.HOGLIN, 68);
        put(EntityType.HUSK, 70);
        put(EntityType.SKELETON, 72);
        put(EntityType.STRAY, 74);
        put(EntityType.ZOMBIE, 76);
        put(EntityType.ZOMBIE_VILLAGER, 78);

        put(EntityType.CREEPER, 80);
        put(EntityType.GUARDIAN, 82);
        put(EntityType.WITCH, 84);
        put(EntityType.EVOKER, 86);
        put(EntityType.BLAZE, 88);
        put(EntityType.ENDERMITE, 90);
        put(EntityType.GHAST, 92);
        put(EntityType.MAGMA_CUBE, 94);
        put(EntityType.VEX, 96);
        put(EntityType.WITHER_SKELETON, 98);
        put(EntityType.ILLUSIONER, 100);
        put(EntityType.PIGLIN_BRUTE, 102);
        put(EntityType.SHULKER, 104);
        put(EntityType.VINDICATOR, 106);
        put(EntityType.ZOGLIN, 108);
        put(EntityType.RAVAGER, 110);
    }};

    @Override
    public Wildcards getType() {
        return Wildcards.MOB_SWAP;
    }

    @Override
    public void tickSessionOn() {
        if (server == null) return;

        int currentDiv0 = (int) (((float) currentSession.passedTime - activatedAt) / 40.0);
        if (lastDiv0 != currentDiv0) {
            int currentDiv = getDiv();
            if (lastDiv != currentDiv) {
                mobSwap();
                lastDiv = currentDiv;
            }
            lastDiv0 = currentDiv0;
        }
    }

    @Override
    public void activate() {
        super.activate();
        activatedAt = (int) currentSession.passedTime;
        lastDiv = 0;
        mobsLeftDiv = 0;
        bossChance = 0;
        swaps = -1;
    }

    @Override
    public void deactivate() {
        super.deactivate();
        killMobSwapMobs();
    }

    public int getDiv() {
        List<Integer> triggerTimes = new ArrayList<>();
        triggerTimes.add(MAX_DELAY);
        int lastTime = 0;
        while (lastTime < currentSession.sessionLength) {
            float sessionProgress = ((float) lastTime) / (currentSession.sessionLength);
            sessionProgress = Math.max(0, Math.min(1, sessionProgress));
            lastTime += (int) (MAX_DELAY - sessionProgress * (MAX_DELAY - MIN_DELAY));
            if (lastTime > (currentSession.passedTime - activatedAt) && lastTime < (currentSeries.sessionLength-MIN_DELAY)) {
                triggerTimes.add(lastTime);
            }
        }
        return triggerTimes.size();
    }

    public void mobSwap() {
        swaps++;
        if (swaps < 1) return;


        float progress = ((float) currentSession.passedTime - activatedAt) / (currentSession.sessionLength - activatedAt);
        if (progress > 0.7) {
            if (mobsLeftDiv == 0) {
                mobsLeftDiv = lastDiv;
            }
            if (SPAWN_MOBS != 0) {
                int totalMobsLeft = mobsLeftDiv * SPAWN_MOBS;
                bossChance = (5.0 / totalMobsLeft) * BOSS_CHANCE_MULTIPLIER;
            }
        }

        if (swaps > 1) {
            PlayerUtils.playSoundToPlayers(PlayerUtils.getAllPlayers(), SoundEvents.BLOCK_BEACON_DEACTIVATE);
        }

        killNonNamedMobs();
        mobcapAnimal = 0;

        TaskScheduler.scheduleTask(120, () -> {
            mobcapAnimal = SPAWN_MOBS;
            fastAnimalSpawn = true;
        });

        int timeForSpawning = Math.max(120, (int)(SPAWN_MOBS/2.5));

        for (int i = timeForSpawning; i > 120; i -= 20) {
            TaskScheduler.scheduleTask(i, () -> {
                PlayerUtils.playSoundToPlayers(PlayerUtils.getAllPlayers(), SoundEvents.ENTITY_CHICKEN_EGG);
            });
        }

        for (int delay : eggSounds) {
            TaskScheduler.scheduleTask(timeForSpawning + delay, () -> {
                PlayerUtils.playSoundToPlayers(PlayerUtils.getAllPlayers(), SoundEvents.ENTITY_CHICKEN_EGG);
            });
        }

        TaskScheduler.scheduleTask(timeForSpawning + 140, () -> {
            mobcapAnimal = -1;
            fastAnimalSpawn = false;
            WildcardManager.showDots();
        });

        TaskScheduler.scheduleTask(timeForSpawning + 240, () -> {
            PlayerUtils.playSoundToPlayers(PlayerUtils.getAllPlayers(), SoundEvents.ENTITY_ELDER_GUARDIAN_CURSE, 0.2f, 1);
            PlayerUtils.playSoundToPlayers(PlayerUtils.getAllPlayers(), SoundEvents.ENTITY_ZOMBIE_VILLAGER_CURE, 0.2f, 1);
            transformNonNamedMobs(progress);
        });
    }

    private static void killNonNamedMobs() {
        if (server == null) return;
        for (ServerWorld world : server.getWorlds()) {
            List<Entity> toKill = new ArrayList<>();
            world.iterateEntities().forEach((entity) -> {
                if (!(entity instanceof LivingEntity)) return;
                if (entity instanceof PlayerEntity) return;
                if (entity.hasCustomName()) return;
                toKill.add(entity);
            });

            boolean mobLoot = world.getGameRules().getBoolean(GameRules.DO_MOB_LOOT);
            if (mobLoot) world.getGameRules().get(GameRules.DO_MOB_LOOT).set(false, world.getServer());
            for (Entity entity : toKill) {
                //? if <=1.21 {
                entity.kill();
                 //?} else {
                /*entity.kill((ServerWorld) entity.getWorld());
                *///?}
            }
            if (mobLoot) world.getGameRules().get(GameRules.DO_MOB_LOOT).set(true, world.getServer());
        }
    }

    private static void transformNonNamedMobs(float progress) {
        int dangerThresholdMin = Math.min(50, (int) (progress * 70));
        int dangerThresholdMax = Math.max(80, Math.min(115, (int) (progress * 50) + 80));

        if (server == null) return;
        for (ServerWorld world : server.getWorlds()) {
            List<Entity> toKill = new ArrayList<>();
            world.iterateEntities().forEach((entity) -> {
                if (!(entity instanceof LivingEntity)) return;
                if (entity instanceof PlayerEntity) return;
                if (entity.hasCustomName()) return;

                //? if <=1.21 {
                Entity newMob = getRandomMob(progress, dangerThresholdMin, dangerThresholdMax).create(world);
                 //?} else {
                /*Entity newMob = getRandomMob(progress, dangerThresholdMin, dangerThresholdMax).create(world, SpawnReason.NATURAL);
                *///?}
                if (newMob != null) {
                    newMob.refreshPositionAndAngles(entity.getX(), entity.getY(), entity.getZ(), entity.getYaw(), entity.getPitch());
                    newMob.addCommandTag("mobswap");
                    if (newMob instanceof MobEntity mobEntity) {
                        mobEntity.setPersistent();
                    }
                    if (newMob instanceof WardenEntity wardenEntity) {
                        wardenEntity.getBrain().remember(MemoryModuleType.DIG_COOLDOWN, Unit.INSTANCE, 12000000L);
                    }
                    world.spawnEntity(newMob);
                    toKill.add(entity);
                }
            });

            toKill.forEach(Entity::discard);
        }
    }

    private static EntityType<?> getRandomMob(float progress, int dangerThresholdMin, int dangerThresholdMax) {
        List<EntityType<?>> possibleMobs = new ArrayList<>();

        for (Map.Entry<EntityType<?>, Integer> entry : entityEntries.entrySet()) {
            int dangerValue = entry.getValue();
            if (dangerValue >= dangerThresholdMin && dangerValue <= dangerThresholdMax) {
                possibleMobs.add(entry.getKey());
            }
        }

        if (progress > 0.7) {
            if (Math.random() < bossChance) {
                double random = Math.random();
                if (random < 0.33) {
                    return EntityType.WARDEN;
                }
                else if (random < 0.66) {
                    return EntityType.WITHER;
                }
                else {
                    return EntityType.ELDER_GUARDIAN;
                }
            }
        }

        return possibleMobs.get(rnd.nextInt(possibleMobs.size()));
    }

    public static void killMobSwapMobs() {
        if (server == null) return;
        for (ServerWorld world : server.getWorlds()) {
            List<Entity> toKill = new ArrayList<>();
            world.iterateEntities().forEach((entity) -> {
                if (!(entity instanceof LivingEntity)) return;
                if (entity instanceof PlayerEntity) return;
                if (entity.hasCustomName()) return;
                if (!entity.getCommandTags().contains("mobswap")) return;
                toKill.add(entity);
            });

            boolean mobLoot = world.getGameRules().getBoolean(GameRules.DO_MOB_LOOT);
            if (mobLoot) world.getGameRules().get(GameRules.DO_MOB_LOOT).set(false, world.getServer());
            for (Entity entity : toKill) {
                //? if <=1.21 {
                entity.kill();
                 //?} else {
                /*entity.kill((ServerWorld) entity.getWorld());
                *///?}
            }
            if (mobLoot) world.getGameRules().get(GameRules.DO_MOB_LOOT).set(true, world.getServer());
        }
    }

    public static void getSpawnCapacity(SpawnGroup group, int initialCapacity, CallbackInfoReturnable<Integer> cir) {
        if (group.getName().equalsIgnoreCase("monster") && mobcapMonster >= 0) {
            cir.setReturnValue(mobcapMonster);
        }
        else if (group.getName().equalsIgnoreCase("creature") && mobcapAnimal >= 0) {
            cir.setReturnValue(mobcapAnimal);
        }
    }

    public static void isRare(SpawnGroup group, CallbackInfoReturnable<Boolean> cir) {
        if (group.getName().equalsIgnoreCase("creature") && fastAnimalSpawn) {
            cir.setReturnValue(false);
        }
    }

    public static void isAcceptableSpawnPosition(ServerWorld world, Chunk chunk, BlockPos.Mutable pos, double squaredDistance, CallbackInfoReturnable<Boolean> cir) {
        if (!fastAnimalSpawn) return;
        if (squaredDistance < 4) {
            cir.setReturnValue(false);
        }
        cir.setReturnValue(Objects.equals(new ChunkPos(pos), chunk.getPos()) || world.shouldTick(pos));
    }
}
