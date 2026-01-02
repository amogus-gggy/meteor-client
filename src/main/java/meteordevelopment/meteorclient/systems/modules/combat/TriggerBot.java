/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package meteordevelopment.meteorclient.systems.modules.combat;

import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.player.PlayerUtils;
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
        .defaultValue(EntityType.PLAYER)
        .build()
    );

    private final Setting<Boolean> onlyCrits = sgGeneral.add(new BoolSetting.Builder()
        .name("only-crits")
        .description("Only attacks when critical hit is possible.")
        .defaultValue(false)
        .build()
    );

    public TriggerBot() {
        super(Categories.Combat, "trigger-bot", "Automatically attacks the entity you are looking at.");
    }

    @EventHandler
    private void onTick(TickEvent.Pre event) {
        if (!mc.player.isAlive() || PlayerUtils.getGameMode() == GameMode.SPECTATOR) return;

        Entity target = mc.targetedEntity;
        if (target == null) return;
        if (!shouldAttack(target)) return;

        // Only attack if weapon cooldown is recovered
        if (mc.player.getAttackCooldownProgress(0.5f) >= 1.0f) {
            // Check for critical hit if only crits mode is enabled
            if (onlyCrits.get() && !isCriticalHit()) {
                return;
            }
            
            mc.interactionManager.attackEntity(mc.player, target);
            mc.player.swingHand(mc.player.getActiveHand());
        }
    }

    private boolean shouldAttack(Entity entity) {
        if (entity == mc.player) return false;
        if (!(entity instanceof LivingEntity) || !entity.isAlive()) return false;
        if (!entities.get().contains(entity.getType())) return false;

        if (entity instanceof PlayerEntity player) {
            if (player.isCreative()) return false;
        }

        return true;
    }

    private boolean isCriticalHit() {
        return mc.player.fallDistance > 0.0f && 
               !mc.player.isOnGround() && 
               !mc.player.isClimbing() && 
               !mc.player.isInFluid() && 
               !mc.player.hasStatusEffect(net.minecraft.entity.effect.StatusEffects.BLINDNESS) &&
               !mc.player.hasStatusEffect(net.minecraft.entity.effect.StatusEffects.SLOW_FALLING);
    }
}