package net.mat0u5.lifeseries.entity.custom;

import de.tomalbrc.bil.api.AnimatedEntity;
import de.tomalbrc.bil.core.holder.entity.EntityHolder;
import de.tomalbrc.bil.core.holder.entity.living.LivingEntityHolder;
import de.tomalbrc.bil.core.model.Model;
import de.tomalbrc.bil.file.loader.BbModelLoader;
import eu.pb4.polymer.virtualentity.api.attachment.EntityAttachment;
import net.mat0u5.lifeseries.Main;
import net.mat0u5.lifeseries.entity.AnimationHelper;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ai.goal.*;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.mob.*;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;

public class Snail extends HostileEntity implements AnimatedEntity {
    public static final Identifier ID = Identifier.of(Main.MOD_ID, "snail");
    public static final Model MODEL = BbModelLoader.load(ID);
    private final EntityHolder<Snail> holder;

    public Snail(EntityType<? extends HostileEntity> entityType, World world) {
        super(entityType, world);
        this.holder = new LivingEntityHolder<>(this, MODEL);
        EntityAttachment.ofTicking(this.holder, this);
    }

    @Override
    public EntityHolder<Snail> getHolder() {
        return this.holder;
    }

    public static DefaultAttributeContainer.Builder createAttributes() {
        //? if <= 1.21 {
        return MobEntity.createMobAttributes()
                .add(EntityAttributes.GENERIC_MAX_HEALTH, 18)
                .add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.1)
                .add(EntityAttributes.GENERIC_ATTACK_DAMAGE, 1000)
                .add(EntityAttributes.GENERIC_FOLLOW_RANGE, 50);
        //?} else {
        /*return MobEntity.createMobAttributes()
                .add(EntityAttributes.MAX_HEALTH, 18)
                .add(EntityAttributes.MOVEMENT_SPEED, 0.1)
                .add(EntityAttributes.ATTACK_DAMAGE, 1000)
                .add(EntityAttributes.FOLLOW_RANGE, 50);
        *///?}
    }

    @Override
    protected void initGoals() {
        this.goalSelector.add(1, new SwimGoal(this));
        this.goalSelector.add(2, new AttackGoal(this));
        this.targetSelector.add(0, new ActiveTargetGoal<>(this, PlayerEntity.class, false));
    }


    @Override
    public void tick() {
        super.tick();

        if (this.age % 2 == 0) {
            AnimationHelper.updateHurtVariant(this, this.holder);
            AnimationHelper.updateWalkAnimation(this, this.holder);
        }
    }
}
