/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package voidstrike_dev.voidstrike_client.systems.modules.world;

//Created by squidoodly 16/07/2020

import voidstrike_dev.voidstrike_client.events.world.TickEvent;
import voidstrike_dev.voidstrike_client.settings.BoolSetting;
import voidstrike_dev.voidstrike_client.settings.EntityTypeListSetting;
import voidstrike_dev.voidstrike_client.settings.Setting;
import voidstrike_dev.voidstrike_client.settings.SettingGroup;
import voidstrike_dev.voidstrike_client.systems.modules.Categories;
import voidstrike_dev.voidstrike_client.systems.modules.Module;
import voidstrike_dev.voidstrike_client.utils.entity.EntityUtils;
import voidstrike_dev.voidstrike_client.utils.player.PlayerUtils;
import voidstrike_dev.voidstrike_client.utils.player.Rotations;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.mob.SkeletonHorseEntity;
import net.minecraft.entity.mob.ZombieHorseEntity;
import net.minecraft.entity.passive.LlamaEntity;
import net.minecraft.entity.passive.PigEntity;
import net.minecraft.entity.passive.StriderEntity;
import net.minecraft.item.SpawnEggItem;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.EntityHitResult;

import java.util.Set;

public class AutoMount extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<Boolean> checkSaddle = sgGeneral.add(new BoolSetting.Builder()
        .name("check-saddle")
        .description("Checks if the entity contains a saddle before mounting.")
        .defaultValue(false)
        .build()
    );

    private final Setting<Boolean> rotate = sgGeneral.add(new BoolSetting.Builder()
        .name("rotate")
        .description("Faces the entity you mount.")
        .defaultValue(true)
        .build()
    );

    private final Setting<Set<EntityType<?>>> entities = sgGeneral.add(new EntityTypeListSetting.Builder()
        .name("entities")
        .description("Rideable entities.")
        .filter(EntityUtils::isRideable)
        .build()
    );

    public AutoMount() {
        super(Categories.World, "auto-mount", "Automatically mounts entities.");
    }

    @EventHandler
    private void onTick(TickEvent.Pre event) {
        if (mc.player.hasVehicle()) return;
        if (mc.player.isSneaking()) return;
        if (mc.player.getMainHandStack().getItem() instanceof SpawnEggItem) return;

        for (Entity entity : mc.world.getEntities()) {
            if (!entities.get().contains(entity.getType())) continue;
            if (!PlayerUtils.isWithin(entity, 4)) continue;
            if ((entity instanceof PigEntity || entity instanceof SkeletonHorseEntity || entity instanceof StriderEntity || entity instanceof ZombieHorseEntity) && !((MobEntity) entity).hasSaddleEquipped()) continue;
            if (!(entity instanceof LlamaEntity) && entity instanceof MobEntity mobEntity && checkSaddle.get() && !mobEntity.hasSaddleEquipped()) continue;
            interact(entity, rotate.get());
            return;
        }
    }

    private void interact(Entity entity, boolean rotate) {
        if (rotate) {
            Rotations.rotate(Rotations.getYaw(entity), Rotations.getPitch(entity), -100, () -> interact(entity));
        } else {
            interact(entity);
        }
    }

    private void interact(Entity entity) {
        EntityHitResult location = new EntityHitResult(entity, entity.getBoundingBox().getCenter());
        mc.interactionManager.interactEntityAtLocation(mc.player, entity, location, Hand.MAIN_HAND);
        mc.interactionManager.interactEntity(mc.player, entity, Hand.MAIN_HAND);
    }
}
