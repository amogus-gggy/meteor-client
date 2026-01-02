/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package meteordevelopment.meteorclient.systems.modules.combat;

import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.friends.Friends;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.player.PlayerUtils;
import meteordevelopment.meteorclient.utils.world.TickRate;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.world.GameMode;

import java.util.Set;

public class TriggerBot extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<Set<EntityType<?>>> entities = sgGeneral.add(new EntityTypeListSetting.Builder()
        .name("entities")
        .description("Entities to attack.")
        .onlyAttackable()
        .defaultValue(EntityType.PLAYER)
        .build()
    );

    private final Setting<Boolean> ignoreFriends = sgGeneral.add(new BoolSetting.Builder()
        .name("ignore-friends")
        .description("Ignores friends when attacking.")
        .defaultValue(true)
        .build()
    );

    private final Setting<Boolean> pauseOnLag = sgGeneral.add(new BoolSetting.Builder()
        .name("pause-on-lag")
        .description("Pauses if the server is lagging.")
        .defaultValue(true)
        .build()
    );

    private final Setting<Boolean> onlyOnClick = sgGeneral.add(new BoolSetting.Builder()
        .name("only-on-click")
        .description("Only attacks when holding left click.")
        .defaultValue(false)
        .build()
    );

    private final Setting<Integer> delay = sgGeneral.add(new IntSetting.Builder()
        .name("delay")
        .description("Delay between attacks in ticks.")
        .defaultValue(2)
        .min(0)
        .sliderMax(20)
        .build()
    );

    private final Setting<Boolean> syncWithServerDelay = sgGeneral.add(new BoolSetting.Builder()
        .name("sync-with-server-delay")
        .description("Syncs attack delay with server tick rate to compensate for lag.")
        .defaultValue(false)
        .build()
    );

    private int hitTimer;

    public TriggerBot() {
        super(Categories.Combat, "trigger-bot", "Automatically attacks the entity you are looking at.");
    }

    @EventHandler
    private void onTick(TickEvent.Pre event) {
        if (!mc.player.isAlive() || PlayerUtils.getGameMode() == GameMode.SPECTATOR) return;
        if (onlyOnClick.get() && !mc.options.attackKey.isPressed()) return;
        if (pauseOnLag.get() && TickRate.INSTANCE.getTimeSinceLastTick() >= 1f) return;

        Entity target = mc.targetedEntity;
        if (target == null) return;
        if (!shouldAttack(target)) return;

        if (hitTimer <= 0) {
            // Only attack if weapon cooldown is recovered
            if (mc.player.getAttackCooldownProgress(0.5f) >= 1.0f) {
                mc.interactionManager.attackEntity(mc.player, target);
                mc.player.swingHand(mc.player.getActiveHand());
                
                int baseDelay = delay.get();
                if (syncWithServerDelay.get()) {
                    float timeSinceLastTick = TickRate.INSTANCE.getTimeSinceLastTick();
                    // Adjust delay based on server performance - add extra delay when server is lagging
                    baseDelay += (int) Math.max(0, timeSinceLastTick * 20);
                    baseDelay = Math.min(baseDelay, 60); // Cap at 60 ticks to prevent excessive delays
                }
                
                hitTimer = baseDelay;
            }
        } else {
            hitTimer--;
        }
    }

    private boolean shouldAttack(Entity entity) {
        if (entity == mc.player) return false;
        if (!(entity instanceof LivingEntity) || !entity.isAlive()) return false;
        if (!entities.get().contains(entity.getType())) return false;

        if (entity instanceof PlayerEntity player) {
            if (player.isCreative()) return false;
            if (ignoreFriends.get() && !Friends.get().shouldAttack(player)) return false;
        }

        return true;
    }
}