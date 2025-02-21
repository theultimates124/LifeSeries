package net.mat0u5.lifeseries.entity.triviabot;

import de.tomalbrc.bil.api.AnimatedEntity;
import de.tomalbrc.bil.api.AnimatedEntityHolder;
import de.tomalbrc.bil.api.Animator;
import de.tomalbrc.bil.core.holder.entity.EntityHolder;
import de.tomalbrc.bil.core.holder.entity.living.LivingEntityHolder;
import de.tomalbrc.bil.core.model.Model;
import de.tomalbrc.bil.file.loader.BbModelLoader;
import eu.pb4.polymer.virtualentity.api.attachment.EntityAttachment;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.mat0u5.lifeseries.Main;
import net.mat0u5.lifeseries.entity.AnimationHandler;
import net.mat0u5.lifeseries.entity.snail.Snail;
import net.mat0u5.lifeseries.entity.triviabot.goal.TriviaBotGlideGoal;
import net.mat0u5.lifeseries.entity.triviabot.goal.TriviaBotLookAtPlayerGoal;
import net.mat0u5.lifeseries.entity.triviabot.goal.TriviaBotTeleportGoal;
import net.mat0u5.lifeseries.network.NetworkHandlerServer;
import net.mat0u5.lifeseries.network.packets.StringPayload;
import net.mat0u5.lifeseries.registries.MobRegistry;
import net.mat0u5.lifeseries.series.wildlife.wildcards.WildcardManager;
import net.mat0u5.lifeseries.series.wildlife.wildcards.Wildcards;
import net.mat0u5.lifeseries.series.wildlife.wildcards.wildcard.SizeShifting;
import net.mat0u5.lifeseries.series.wildlife.wildcards.wildcard.trivia.TriviaQuestion;
import net.mat0u5.lifeseries.series.wildlife.wildcards.wildcard.trivia.TriviaWildcard;
import net.mat0u5.lifeseries.utils.*;
import net.minecraft.block.BlockState;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.*;
import net.minecraft.entity.ai.control.MoveControl;
import net.minecraft.entity.ai.pathing.MobNavigation;
import net.minecraft.entity.ai.pathing.Path;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.mob.AmbientEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.passive.BeeEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.Fluid;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.packet.s2c.play.PositionFlag;
import net.minecraft.particle.EntityEffectParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ChunkTicketType;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.*;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.World;
import net.minecraft.world.explosion.Explosion;
import org.jetbrains.annotations.Nullable;

import java.util.*;

import static net.mat0u5.lifeseries.Main.server;

public class TriviaBot extends AmbientEntity implements AnimatedEntity {
    public static final Identifier ID = Identifier.of(Main.MOD_ID, "triviabot");
    public static final Model MODEL = BbModelLoader.load(ID);

    public static final int STATIONARY_TP_COOLDOWN = 400; // No movement for 20 seconds teleports the bot
    public static final float MOVEMENT_SPEED = 0.45f;
    public static final int MAX_DISTANCE = 100;
    public static boolean CAN_START_RIDING = true;
    public static ItemSpawner itemSpawner;
    public static int EASY_TIME = 180;
    public static int NORMAL_TIME = 240;
    public static int HARD_TIME = 300;

    public boolean gliding = false;

    public boolean interactedWith = false;
    public long interactedAt = 0;
    public int timeToComplete = 0;
    public int difficulty = 0;
    public boolean submittedAnswer = false;
    public Boolean answeredRight = null;
    public boolean ranOutOfTime = false;
    public int snailTransformation = 0;
    public TriviaQuestion question;

    public int nullPlayerChecks = 0;
    private long chunkTicketExpiryTicks = 0L;

    private final EntityHolder<TriviaBot> holder;
    public UUID boundPlayerUUID;

    public TriviaBot(EntityType<? extends AmbientEntity> entityType, World world) {
        super(entityType, world);
        this.holder = new LivingEntityHolder<>(this, MODEL);
        EntityAttachment.ofTicking(holder, this);
        setInvulnerable(true);
        setPersistent();
        updateNavigation();
    }

    @Override
    public AnimatedEntityHolder getHolder() {
        return holder;
    }

    @Override
    public void writeCustomDataToNbt(NbtCompound nbt) {
        super.writeCustomDataToNbt(nbt);
        if (boundPlayerUUID == null) return;
        nbt.putUuid("boundPlayer", boundPlayerUUID);
    }

    @Override
    public void readCustomDataFromNbt(NbtCompound nbt) {
        super.readCustomDataFromNbt(nbt);
        UUID newUUID = nbt.getUuid("boundPlayer");
        if (newUUID != null) {
            boundPlayerUUID = newUUID;
        }
    }

    public static DefaultAttributeContainer.Builder createAttributes() {
        //? if <= 1.21 {
        return MobEntity.createMobAttributes()
                .add(EntityAttributes.GENERIC_MAX_HEALTH, 10000)
                .add(EntityAttributes.GENERIC_MOVEMENT_SPEED, MOVEMENT_SPEED)
                .add(EntityAttributes.GENERIC_FLYING_SPEED, MOVEMENT_SPEED)
                .add(EntityAttributes.GENERIC_STEP_HEIGHT, 1)
                .add(EntityAttributes.GENERIC_FOLLOW_RANGE, 100)
                .add(EntityAttributes.GENERIC_WATER_MOVEMENT_EFFICIENCY, 1)
                .add(EntityAttributes.GENERIC_SAFE_FALL_DISTANCE, 100)
                .add(EntityAttributes.GENERIC_ATTACK_DAMAGE, 0);
        //?} else {
        /*return MobEntity.createMobAttributes()
                .add(EntityAttributes.MAX_HEALTH, 10000)
                .add(EntityAttributes.MOVEMENT_SPEED, MOVEMENT_SPEED)
                .add(EntityAttributes.FLYING_SPEED, MOVEMENT_SPEED)
                .add(EntityAttributes.STEP_HEIGHT, 1)
                .add(EntityAttributes.FOLLOW_RANGE, 100)
                .add(EntityAttributes.WATER_MOVEMENT_EFFICIENCY, 1)
                .add(EntityAttributes.SAFE_FALL_DISTANCE, 100)
                .add(EntityAttributes.ATTACK_DAMAGE, 0);
        *///?}
    }

    @Nullable
    public ServerPlayerEntity getBoundPlayer() {
        if (server == null) return null;
        ServerPlayerEntity player = server.getPlayerManager().getPlayer(boundPlayerUUID);
        if (player == null || (player.isSpectator() && player.isDead())) {
            nullPlayerChecks++;
            return null;
        }
        nullPlayerChecks = 0;
        if (player.isSpectator()) return null;
        if (player.isDead()) return null;
        return player;
    }

    public void setBoundPlayer(ServerPlayerEntity player) {
        if (player == null) return;
        boundPlayerUUID = player.getUuid();
        writeCustomDataToNbt(new NbtCompound());
    }

    @Override
    protected void initGoals() {
        goalSelector.add(0, new TriviaBotTeleportGoal(this));
        goalSelector.add(1, new TriviaBotGlideGoal(this));
        goalSelector.add(2, new TriviaBotLookAtPlayerGoal(this));
    }

    @Override
    public void tick() {
        super.tick();
        if (age % 100 == 0) {
            if (!TriviaWildcard.bots.containsValue(this) || !WildcardManager.isActiveWildcard(Wildcards.TRIVIA)) {
                despawn();
            }
        }

        if (submittedAnswer && answeredRight != null) {
            if (answeredRight) {
                if (analyzing < -80) {
                    noClip = true;
                    float velocity = Math.min(0.5f, 0.25f * Math.abs((analyzing+80) / (20.0f)));
                    setVelocity(0,velocity,0);
                    if (analyzing < -200) despawn();
                }
            }
            else {
                if (analyzing < -100) {
                    noClip = true;
                    float velocity = Math.min(0.5f, 0.25f * Math.abs((analyzing+100) / (20.0f)));
                    setVelocity(0,velocity,0);
                    if (analyzing < -200) despawn();
                }
            }
        }
        else {
            handleHighVelocity();
            if (!interactedWith) {
                ServerPlayerEntity boundPlayer = getBoundPlayer();
                if (boundPlayer != null) {
                    if (age % 5 == 0) {
                        updateNavigationTarget();
                    }
                }
            }
            if (interactedWith && getRemainingTime() <= 0) {
                ranOutOfTime = true;
            }
            if (snailTransformation > 33) {
                transformIntoSnail();
            }
        }

        if (nullPlayerChecks > 1000) {
            despawn();
        }

        if (age % 2 == 0) {
            updateAnimations();
        }


        chunkLoading();
        clearStatusEffects();
        playSounds();
    }

    public void handleHighVelocity() {
        Vec3d velocity = getVelocity();
        if (velocity.y > 0.15) {
            setVelocity(velocity.x,0.15,velocity.z);
        }
        else if (velocity.y < -0.15) {
            setVelocity(velocity.x,-0.15,velocity.z);
        }
    }

    public void chunkLoading() {
        if (getWorld() instanceof ServerWorld world) {
            if ((--this.chunkTicketExpiryTicks <= 0L)) {
                world.resetIdleTimeout();
                this.chunkTicketExpiryTicks = addTicket(world) - 20L;
            }
        }
    }

    public long addTicket(ServerWorld world) {
        world.getChunkManager().addTicket(ChunkTicketType.PORTAL, new ChunkPos(getBlockPos()), 2, getBlockPos());
        return ChunkTicketType.PORTAL.getExpiryTicks();
    }

    public void despawn() {
        if (boundPlayerUUID != null) {
            TriviaWildcard.bots.remove(boundPlayerUUID);
        }
        //? if <= 1.21 {
        this.kill();
        //?} else {
        /*this.kill((ServerWorld) getWorld());
         *///?}
        this.discard();
    }

    public void transformIntoSnail() {
        if (getBoundPlayer() != null) {
            Snail triviaSnail = MobRegistry.SNAIL.spawn((ServerWorld) getWorld(), this.getBlockPos(), SpawnReason.COMMAND);
            if (triviaSnail != null) {
                triviaSnail.setBoundPlayer(getBoundPlayer());
                triviaSnail.setFromTrivia();
                triviaSnail.playSound(SoundEvents.ENTITY_GENERIC_EXPLODE.value(), 0.5f, 2);
                ServerWorld world = (ServerWorld) triviaSnail.getWorld();
                world.spawnParticles(
                        ParticleTypes.EXPLOSION,
                        this.getPos().getX(), this.getPos().getY(), this.getPos().getZ(),
                        10, 0.5, 0.5, 0.5, 0.5
                );
            }
        }
        despawn();
    }

    private int analyzing = -1;
    public void updateAnimations() {
        AnimationHandler.updateHurtVariant(this, holder);
        Animator animator = holder.getAnimator();
        if (ranOutOfTime) {
            if (snailTransformation == 0) {
                pauseAllAnimations("snail_transform");
                animator.playAnimation("snail_transform", 8);
            }
            snailTransformation++;
        }
        else if (analyzing > 0) {
            analyzing--;
            pauseAllAnimations("analyzing");
        }
        else if (submittedAnswer && answeredRight != null) {
            if (analyzing == 0) {
                if (answeredRight) {
                    pauseAllAnimations("answer_correct");
                    animator.playAnimation("answer_correct", 7);
                }
                else {
                    pauseAllAnimations("answer_incorrect");
                    animator.playAnimation("answer_incorrect", 6);
                }
            }
            analyzing--;
        }
        else if (interactedWith) {
            pauseAllAnimations("countdown");
            animator.playAnimation("countdown", 4);
        }
        else if (this.gliding) {
            pauseAllAnimations("glide");
            animator.playAnimation("glide", 3);
        }
        else if (this.limbAnimator.isLimbMoving() && this.limbAnimator.getSpeed() > 0.02) {
            pauseAllAnimations("walk");
            animator.playAnimation("walk", 1);
        }
        else {
            pauseAllAnimations("idle");
            animator.playAnimation("idle", 0, true);
        }
    }

    public void playAnalyzingAnimation() {
        Animator animator = holder.getAnimator();
        pauseAllAnimations("analyzing");
        animator.playAnimation("analyzing", 5);
        analyzing = 42;
    }

    public void pauseAllAnimations(String except) {
        Animator animator = holder.getAnimator();
        if (!except.equalsIgnoreCase("glide")) animator.pauseAnimation("glide");
        if (!except.equalsIgnoreCase("walk")) animator.pauseAnimation("walk");
        if (!except.equalsIgnoreCase("idle")) animator.pauseAnimation("idle");
        if (!except.equalsIgnoreCase("countdown")) animator.pauseAnimation("countdown");
        if (!except.equalsIgnoreCase("analyzing")) animator.pauseAnimation("analyzing");
        if (!except.equalsIgnoreCase("answer_incorrect")) animator.pauseAnimation("answer_incorrect");
        if (!except.equalsIgnoreCase("answer_correct")) animator.pauseAnimation("answer_correct");
        if (!except.equalsIgnoreCase("snail_transform")) animator.pauseAnimation("snail_transform");
    }

    public void teleportToPlayer() {
        ServerPlayerEntity player = getBoundPlayer();
        if (player == null) return;
        teleportTo(player.getServerWorld(), player.getX(), player.getY(), player.getZ());
    }

    public void teleportAbovePlayer(double minDistanceFromPlayer, int distanceAbove) {
        ServerPlayerEntity player = getBoundPlayer();
        if (player == null) return;
        if (getWorld() instanceof ServerWorld world) {
            BlockPos tpTo = getBlockPosNearTarget(world, player.getBlockPos().add(0, distanceAbove, 0), minDistanceFromPlayer);
            teleportTo(player.getServerWorld(), tpTo.getX(), tpTo.getY(), tpTo.getZ());
        }
    }

    public void teleportTo(ServerWorld world, double x, double y, double z) {
        //TODO make sure sounds still work after teleporting
        this.playSound(SoundEvents.ENTITY_PLAYER_TELEPORT);
        AnimationUtils.spawnTeleportParticles((ServerWorld) getWorld(), getPos());
        Set<PositionFlag> flags = EnumSet.noneOf(PositionFlag.class);
        //? if <= 1.21 {
        teleport(world, x, y, z, flags, getYaw(), getPitch());
        //?} else {
        /*teleport(world, x, y, z, flags, getYaw(), getPitch(), false);
         *///?}
        this.playSound(SoundEvents.ENTITY_PLAYER_TELEPORT);
        AnimationUtils.spawnTeleportParticles(world, getPos());
        this.chunkTicketExpiryTicks = 0L;
    }

    public int getRemainingTime() {
        int timeSinceStart = (int) Math.ceil((System.currentTimeMillis() - interactedAt) / 1000.0);
        return timeToComplete - timeSinceStart;
    }

    public BlockPos getBlockPosNearTarget(ServerWorld world, BlockPos targetPos, double minDistanceFromTarget) {
        if (getBoundPlayer() == null) return getBlockPos();

        for (int attempts = 0; attempts < 10; attempts++) {
            Vec3d offset = new Vec3d(
                    random.nextDouble() * 2 - 1,
                    1,
                    random.nextDouble() * 2 - 1
            ).normalize().multiply(minDistanceFromTarget);

            BlockPos pos = targetPos.add((int) offset.getX(), 0, (int) offset.getZ());

            BlockPos validPos = findNearestAirBlock(pos, world);
            if (validPos != null) {
                return validPos;
            }
        }

        return targetPos;
    }

    private BlockPos findNearestAirBlock(BlockPos pos, World world) {
        for (int yOffset = -5; yOffset <= 5; yOffset++) {
            BlockPos newPos = pos.up(yOffset);
            if (world.getBlockState(newPos).isAir() && world.getBlockState(pos.up(yOffset+1)).isAir()) {
                return newPos;
            }
        }
        return null;
    }

    public void updateNavigation() {
        moveControl = new MoveControl(this);
        navigation = new MobNavigation(this, getWorld());
        updateNavigationTarget();
    }

    public void updateNavigationTarget() {
        if (getBoundPlayer() == null) return;
        if (this.distanceTo(getBoundPlayer()) > MAX_DISTANCE) return;
        navigation.setSpeed(MOVEMENT_SPEED);
        Path path = navigation.findPathTo(getBoundPlayer(), 3);
        if (path != null) navigation.startMovingAlong(path, MOVEMENT_SPEED);
    }

    @Nullable
    public BlockPos getGroundBlock() {
        Vec3d startPos = getPos();
        Vec3d endPos = startPos.add(0, getWorld().getBottomY(), 0);

        BlockHitResult result = getWorld().raycast(
                new RaycastContext(
                        startPos,
                        endPos,
                        RaycastContext.ShapeType.COLLIDER,
                        RaycastContext.FluidHandling.NONE,
                        this
                )
        );
        if (result.getType() == HitResult.Type.MISS) return null;
        return result.getBlockPos();
    }

    public double getDistanceToGroundBlock() {
        BlockPos belowBlock = getGroundBlock();
        if (belowBlock == null) return Double.NEGATIVE_INFINITY;
        return getY() - belowBlock.getY() - 1;
    }

    public void playSounds() {
        //TODO
    }

    /*
        Trivia stuff
     */

    @Override
    public ActionResult interactMob(PlayerEntity player, Hand hand) {
        ServerPlayerEntity boundPlayer = getBoundPlayer();
        if (boundPlayer == null) return ActionResult.PASS;
        if (boundPlayer.getUuid() != player.getUuid()) return ActionResult.PASS;
        if (submittedAnswer) return ActionResult.PASS;
        if (interactedWith && getRemainingTime() <= 0) return ActionResult.PASS;

        if (!interactedWith || question == null) {
            interactedAt = System.currentTimeMillis();
            difficulty = 1+getRandom().nextInt(3);
            timeToComplete = difficulty * 60 + 120;
            if (difficulty == 1) timeToComplete = EASY_TIME;
            if (difficulty == 2) timeToComplete = NORMAL_TIME;
            if (difficulty == 3) timeToComplete = HARD_TIME;
            question = TriviaWildcard.getTriviaQuestion(difficulty);
        }
        NetworkHandlerServer.sendTriviaPacket(boundPlayer, question.getQuestion(), difficulty, interactedAt, timeToComplete, question.getAnswers());
        interactedWith = true;

        return ActionResult.PASS;
    }

    public void handleAnswer(int answer) {
        if (submittedAnswer) return;
        submittedAnswer = true;
        if (answer == question.getCorrectAnswerIndex()) {
            answeredCorrect();
        }
        else {
            answeredIncorrect();
        }
    }

    public void answeredCorrect() {
        answeredRight = true;
        playAnalyzingAnimation();
        TaskScheduler.scheduleTask(145, this::spawnItemForPlayer);
        TaskScheduler.scheduleTask(170, this::spawnItemForPlayer);
        TaskScheduler.scheduleTask(198, this::spawnItemForPlayer);
        TaskScheduler.scheduleTask(213, this::blessPlayer);
    }

    public void answeredIncorrect() {
        answeredRight = false;
        playAnalyzingAnimation();
        TaskScheduler.scheduleTask(210, this::cursePlayer);
    }

    public void cursePlayer() {
        ServerPlayerEntity player = getBoundPlayer();
        if (player == null) return;
        player.playSoundToPlayer(SoundEvents.ENTITY_ELDER_GUARDIAN_CURSE, SoundCategory.MASTER, 0.2f, 1f);
        ServerWorld world = (ServerWorld) getWorld();
        Vec3d pos = getPos();

        world.spawnParticles(
                EntityEffectParticleEffect.create(ParticleTypes.ENTITY_EFFECT, 0xFFa61111),
                pos.getX(), pos.getY()+1, pos.getZ(),
                40, 0.1, 0.25, 0.1, 0.035
        );
        int curse = world.random.nextInt(9);
        switch (curse) {
            case 0:
                curse_hunger(player);
                break;
            case 1:
                curse_ravager(player);
                break;
            case 2:
                curse_infestation(player);
                break;
            case 3:
                curse_gigantification(player);
                break;
            case 4:
                curse_slippery_ground(player);
                break;
            case 5:
                curse_binding_armor(player);
                break;
            case 6:
                curse_hearts(player);
                break;
            case 7:
                curse_moonjump(player);
                break;
            case 8:
                curse_beeswarm(player);
                break;
            /* TODO
            case 9:
                curse_robotic_voice(player);
                break;
             */
        }
    }

    private static final List<RegistryEntry<StatusEffect>> blessEffects = List.of(
            StatusEffects.SPEED,
            StatusEffects.HASTE,
            StatusEffects.STRENGTH,
            StatusEffects.JUMP_BOOST,
            StatusEffects.REGENERATION,
            StatusEffects.RESISTANCE,
            StatusEffects.FIRE_RESISTANCE,
            StatusEffects.WATER_BREATHING,
            StatusEffects.NIGHT_VISION,
            StatusEffects.HEALTH_BOOST,
            StatusEffects.ABSORPTION
    );
    public void blessPlayer() {
        ServerPlayerEntity player = getBoundPlayer();
        if (player == null) return;
        player.sendMessage(Text.of(""));
        for (int i = 0; i < 3; i++) {
            RegistryEntry<StatusEffect> effect = blessEffects.get(player.getRandom().nextInt(blessEffects.size()));
            int amplifier;
            if (effect == StatusEffects.FIRE_RESISTANCE || effect == StatusEffects.WATER_BREATHING || effect == StatusEffects.NIGHT_VISION) {
                amplifier = 0;
            }
            else if (effect == StatusEffects.REGENERATION || effect == StatusEffects.STRENGTH || effect == StatusEffects.HEALTH_BOOST) {
                amplifier = player.getRandom().nextInt(1);
            }
            else {
                amplifier = player.getRandom().nextInt(4);
            }
            player.addStatusEffect(new StatusEffectInstance(effect, 36000, amplifier));

            String romanNumeral = TextUtils.toRomanNumeral(amplifier + 1);
            MutableText effectName = Text.translatable(effect.value().getTranslationKey()).formatted(Formatting.GRAY);
            player.sendMessage(Text.literal(" §a§l+ ").append(effectName).append(Text.of(" §6"+romanNumeral)));
        }
        player.sendMessage(Text.of(""));
    }

    public void spawnItemForPlayer() {
        if (itemSpawner == null) return;
        if (getBoundPlayer() == null) return;
        Vec3d playerPos = getBoundPlayer().getPos();
        Vec3d pos = getPos().add(0,1,0);
        Vec3d relativeTargetPos = new Vec3d(
                playerPos.getX() - pos.getX(),
                0,
                playerPos.getZ() - pos.getZ()
        );
        Vec3d vector = Vec3d.ZERO;
        if (relativeTargetPos.lengthSquared() > 0.0001) {
            vector = relativeTargetPos.normalize().multiply(0.3).add(0,0.1,0);
        }
        itemSpawner.spawnRandomItemForPlayerWithVelocity((ServerWorld) getWorld(), pos, vector, getBoundPlayer());
    }

    public static void initializeItemSpawner() {
        itemSpawner = new ItemSpawner();
        itemSpawner.addItem(new ItemStack(Items.GOLDEN_APPLE, 2), 20);
        itemSpawner.addItem(new ItemStack(Items.ENDER_PEARL, 2), 20);
        itemSpawner.addItem(new ItemStack(Items.TRIDENT), 10);
        itemSpawner.addItem(new ItemStack(Items.POWERED_RAIL, 16), 10);
        itemSpawner.addItem(new ItemStack(Items.DIAMOND, 4), 20);
        itemSpawner.addItem(new ItemStack(Items.CREEPER_SPAWN_EGG), 10);
        itemSpawner.addItem(new ItemStack(Items.GOLDEN_CARROT, 8), 10);
        itemSpawner.addItem(new ItemStack(Items.WIND_CHARGE, 16), 10);
        itemSpawner.addItem(new ItemStack(Items.SCULK_SHRIEKER, 2), 10);
        itemSpawner.addItem(new ItemStack(Items.SCULK_SENSOR, 8), 10);
        itemSpawner.addItem(new ItemStack(Items.TNT, 8), 20);
        itemSpawner.addItem(new ItemStack(Items.COBWEB, 8), 10);
        itemSpawner.addItem(new ItemStack(Items.OBSIDIAN, 8), 10);
        itemSpawner.addItem(new ItemStack(Items.PUFFERFISH_BUCKET), 10);
        itemSpawner.addItem(new ItemStack(Items.NETHERITE_CHESTPLATE), 10);
        itemSpawner.addItem(new ItemStack(Items.NETHERITE_LEGGINGS), 10);
        itemSpawner.addItem(new ItemStack(Items.NETHERITE_BOOTS), 10);

        ItemStack mace = new ItemStack(Items.MACE);
        ItemStackUtils.setCustomComponentBoolean(mace, "IgnoreBlacklist", true);
        ItemStackUtils.setCustomComponentBoolean(mace, "NoMending", true);
        mace.setDamage(mace.getMaxDamage()-1);
        itemSpawner.addItem(mace, 10);

        ItemStack endCrystal = new ItemStack(Items.END_CRYSTAL);
        ItemStackUtils.setCustomComponentBoolean(endCrystal, "IgnoreBlacklist", true);
        itemSpawner.addItem(endCrystal, 10);

        ItemStack patat = new ItemStack(Items.POISONOUS_POTATO);
        patat.set(DataComponentTypes.CUSTOM_NAME, Text.of("§6§l§nThe Sacred Patat"));
        ItemStackUtils.addLoreToItemStack(patat,
                List.of(Text.of("§5§oEating this might help you. Or maybe not..."))
        );
        itemSpawner.addItem(patat, 1);
    }

    /*
        Curses
     */

    public void curse_hunger(ServerPlayerEntity player) {
        StatusEffectInstance statusEffectInstance = new StatusEffectInstance(StatusEffects.HUNGER, 18000, 2);
        player.addStatusEffect(statusEffectInstance);
    }

    public void curse_ravager(ServerPlayerEntity player) {
        BlockPos spawnPos = getBlockPosNearTarget(player.getServerWorld(), getBlockPos(), 5);
        EntityType.RAVAGER.spawn(player.getServerWorld(), spawnPos, SpawnReason.COMMAND);
    }

    public void curse_infestation(ServerPlayerEntity player) {
        StatusEffectInstance statusEffectInstance = new StatusEffectInstance(StatusEffects.INFESTED, 18000, 0);
        player.addStatusEffect(statusEffectInstance);
    }

    public static List<UUID> cursedGigantificationPlayers = new ArrayList<>();
    public void curse_gigantification(ServerPlayerEntity player) {
        cursedGigantificationPlayers.add(player.getUuid());
        SizeShifting.setPlayerSizeUnchecked(player, 5);
    }

    public void curse_slippery_ground(ServerPlayerEntity player) {
        ServerPlayNetworking.send(player, new StringPayload("curse_sliding", "true"));
    }

    public void curse_binding_armor(ServerPlayerEntity player) {
        for (ItemStack item : player.getArmorItems()) {
            ItemStackUtils.spawnItemForPlayer(player.getServerWorld(), player.getPos(), item.copy(), player);
        }
        //ItemStack head = Items.LEATHER_HELMET.getDefaultStack();
        ItemStack chest = Items.LEATHER_CHESTPLATE.getDefaultStack();
        ItemStack legs = Items.LEATHER_LEGGINGS.getDefaultStack();
        ItemStack boots = Items.LEATHER_BOOTS.getDefaultStack();
        //head.addEnchantment(ItemStackUtils.getEnchantmentEntry(Enchantments.BINDING_CURSE), 1);
        chest.addEnchantment(ItemStackUtils.getEnchantmentEntry(Enchantments.BINDING_CURSE), 1);
        legs.addEnchantment(ItemStackUtils.getEnchantmentEntry(Enchantments.BINDING_CURSE), 1);
        boots.addEnchantment(ItemStackUtils.getEnchantmentEntry(Enchantments.BINDING_CURSE), 1);
        //player.equipStack(EquipmentSlot.HEAD, head);
        player.equipStack(EquipmentSlot.CHEST, chest);
        player.equipStack(EquipmentSlot.LEGS, legs);
        player.equipStack(EquipmentSlot.FEET, boots);
    }

    public static List<UUID> cursedHeartPlayers = new ArrayList<>();
    public void curse_hearts(ServerPlayerEntity player) {
        cursedHeartPlayers.add(player.getUuid());
        double newHealth = Math.max(player.getMaxHealth()-7, 1);
        //? if <=1.21 {
        Objects.requireNonNull(player.getAttributeInstance(EntityAttributes.GENERIC_MAX_HEALTH)).setBaseValue(newHealth);
        //?} else
        /*Objects.requireNonNull(player.getAttributeInstance(EntityAttributes.MAX_HEALTH)).setBaseValue(newHealth);*/
    }

    public static List<UUID> cursedMoonJumpPlayers = new ArrayList<>();
    public void curse_moonjump(ServerPlayerEntity player) {
        cursedMoonJumpPlayers.add(player.getUuid());
        //? if <=1.21 {
        Objects.requireNonNull(player.getAttributeInstance(EntityAttributes.GENERIC_JUMP_STRENGTH)).setBaseValue(0.76);
        //?} else
        /*Objects.requireNonNull(player.getAttributeInstance(EntityAttributes.JUMP_STRENGTH)).setBaseValue(0.76);*/
    }

    public void curse_beeswarm(ServerPlayerEntity player) {
        BlockPos spawnPos = getBlockPosNearTarget(player.getServerWorld(), getBlockPos(), 1);
        BeeEntity bee1 = EntityType.BEE.spawn((ServerWorld) getWorld(), spawnPos, SpawnReason.COMMAND);
        BeeEntity bee2 = EntityType.BEE.spawn((ServerWorld) getWorld(), spawnPos, SpawnReason.COMMAND);
        BeeEntity bee3 = EntityType.BEE.spawn((ServerWorld) getWorld(), spawnPos, SpawnReason.COMMAND);
        BeeEntity bee4 = EntityType.BEE.spawn((ServerWorld) getWorld(), spawnPos, SpawnReason.COMMAND);
        BeeEntity bee5 = EntityType.BEE.spawn((ServerWorld) getWorld(), spawnPos, SpawnReason.COMMAND);
        if (bee1 != null) bee1.setAngryAt(player.getUuid());
        if (bee2 != null) bee2.setAngryAt(player.getUuid());
        if (bee3 != null) bee3.setAngryAt(player.getUuid());
        if (bee4 != null) bee4.setAngryAt(player.getUuid());
        if (bee5 != null) bee5.setAngryAt(player.getUuid());
        if (bee1 != null) bee1.setAngerTime(1000000);
        if (bee2 != null) bee2.setAngerTime(1000000);
        if (bee3 != null) bee3.setAngerTime(1000000);
        if (bee4 != null) bee4.setAngerTime(1000000);
        if (bee5 != null) bee5.setAngerTime(1000000);
    }

    /*
        Override vanilla things
     */

    @Override
    public SoundCategory getSoundCategory() {
        return SoundCategory.PLAYERS;
    }
    
    @Override
    public Vec3d applyFluidMovingSpeed(double gravity, boolean falling, Vec3d motion) {
        return motion;
    }

    @Override
    protected boolean shouldSwimInFluids() {
        return false;
    }

    @Override
    public boolean isTouchingWater() {
        return false;
    }

    @Override
    public void setSwimming(boolean swimming) {
        this.setFlag(4, false);
    }

    @Override
    public boolean updateMovementInFluid(TagKey<Fluid> tag, double speed) {
        return false;
    }

    @Override
    protected boolean canStartRiding(Entity entity) {
        return CAN_START_RIDING;
    }

    @Override
    public void slowMovement(BlockState state, Vec3d multiplier) {
    }

    @Override
    public boolean isImmuneToExplosion(Explosion explosion) {
        return true;
    }

}
