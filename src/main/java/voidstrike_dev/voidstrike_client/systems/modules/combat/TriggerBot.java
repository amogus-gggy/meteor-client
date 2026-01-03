/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package voidstrike_dev.voidstrike_client.systems.modules.combat;

import voidstrike_dev.voidstrike_client.events.world.TickEvent;

import voidstrike_dev.voidstrike_client.settings.EntityTypeListSetting;
import voidstrike_dev.voidstrike_client.settings.EnumSetting;
import voidstrike_dev.voidstrike_client.settings.Setting;
import voidstrike_dev.voidstrike_client.settings.SettingGroup;
import voidstrike_dev.voidstrike_client.systems.modules.Categories;
import voidstrike_dev.voidstrike_client.systems.modules.Module;
import voidstrike_dev.voidstrike_client.utils.player.PlayerUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.world.GameMode;

import java.util.Set;

public class TriggerBot extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    public enum CritMode {
        Normal,
        OnlyCrits,
        PrioritizeCrits
    }

    private final Setting<Set<EntityType<?>>> entities = sgGeneral.add(new EntityTypeListSetting.Builder()
        .name("entities")
        .description("Entities to attack.")
        .defaultValue(EntityType.PLAYER)
        .build()
    );

    private final Setting<CritMode> critMode = sgGeneral.add(new EnumSetting.Builder<CritMode>()
        .name("crit-mode")
        .description("Critical hit behavior mode.")
        .defaultValue(CritMode.Normal)
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
            boolean shouldAttack = true;

            // Handle crit modes
            switch (critMode.get()) {
                case OnlyCrits:
                    shouldAttack = isCriticalHit();
                    break;
                case PrioritizeCrits:
                    if (canCritInFuture() && !isCriticalHit()) {
                        shouldAttack = false; // Wait for crit opportunity
                    }
                    break;
                case Normal:
                default:
                    // Always attack
                    break;
            }

            if (shouldAttack) {
                mc.interactionManager.attackEntity(mc.player, target);
                mc.player.swingHand(mc.player.getActiveHand());
            }
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

    private boolean canCritInFuture() {
        return !mc.player.isClimbing() &&
               !mc.player.isInFluid() &&
               !mc.player.hasStatusEffect(net.minecraft.entity.effect.StatusEffects.BLINDNESS) &&
               !mc.player.hasStatusEffect(net.minecraft.entity.effect.StatusEffects.SLOW_FALLING);
    }
}
