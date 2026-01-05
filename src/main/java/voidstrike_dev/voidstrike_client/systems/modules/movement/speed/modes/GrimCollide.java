/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package voidstrike_dev.voidstrike_client.systems.modules.movement.speed.modes;

import voidstrike_dev.voidstrike_client.events.entity.player.PlayerMoveEvent;
import voidstrike_dev.voidstrike_client.systems.modules.movement.speed.SpeedMode;
import voidstrike_dev.voidstrike_client.systems.modules.movement.speed.SpeedModes;
import voidstrike_dev.voidstrike_client.utils.player.PlayerUtils;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.vehicle.BoatEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

public class GrimCollide extends SpeedMode {

    public GrimCollide() {
        super(SpeedModes.GrimCollide);
    }

    @Override
    public void onMove(PlayerMoveEvent event) {
        if (mc.player == null || mc.world == null) return;

        int collisions = 0;
        Box expandedBox = mc.player.getBoundingBox().expand(settings.grimCollideExpand.get().floatValue());

        for (Entity entity : mc.world.getEntities()) {
            if (entity == mc.player) continue;
            if (canCauseSpeed(entity) && expandedBox.intersects(entity.getBoundingBox())) {
                collisions++;
            }
        }

        double finalSpeed = settings.grimCollideSpeed.get() * collisions;
        if (finalSpeed <= 0.0) return;

        // Check if movement is required
        if (settings.grimCollideRequireMoving.get() && !PlayerUtils.isMoving()) return;

        // Check if upward movement is required
        if (settings.grimCollideRequireUpward.get() && mc.player.getVelocity().y <= 0) return;

        if (collisions > 0) {
            Entity nearest = findNearestEntity(settings.grimCollideRange.get());
            if (nearest != null) {
                Vec3d from = new Vec3d(mc.player.getX(), mc.player.getY(), mc.player.getZ());
                Vec3d to = new Vec3d(nearest.getX(), nearest.getY(), nearest.getZ());
                double[] direction = getDirectionToPoint(from, to, finalSpeed);
                mc.player.addVelocity(direction[0], 0.0, direction[1]);
            } else {
                // Fallback to original yaw-based movement
                float yaw = mc.player.getYaw() * MathHelper.RADIANS_PER_DEGREE;
                double motionX = -MathHelper.sin(yaw) * finalSpeed;
                double motionZ = MathHelper.cos(yaw) * finalSpeed;
                mc.player.addVelocity(motionX, 0.0, motionZ);
            }
        }
    }

    private Entity findNearestEntity(double maxRange) {
        Entity nearest = null;
        double bestSq = Double.MAX_VALUE;
        double maxRangeSq = maxRange * maxRange;

        for (Entity entity : mc.world.getEntities()) {
            if (entity == mc.player) continue;
            if (!canCauseSpeed(entity)) continue;

            double dx = entity.getX() - mc.player.getX();
            double dz = entity.getZ() - mc.player.getZ();
            double sq = dx * dx + dz * dz;
            
            if (sq <= maxRangeSq && sq < bestSq) {
                bestSq = sq;
                nearest = entity;
            }
        }

        return nearest;
    }

    private double[] getDirectionToPoint(Vec3d from, Vec3d to, double speed) {
        double dx = to.x - from.x;
        double dz = to.z - from.z;
        double len = Math.sqrt(dx * dx + dz * dz);
        if (len == 0) return new double[]{0.0, 0.0};
        return new double[]{dx / len * speed, dz / len * speed};
    }

    private boolean canCauseSpeed(Entity entity) {
        if (settings.grimCollideOnlyPlayers.get()) {
            return entity instanceof PlayerEntity;
        }
        return (entity instanceof LivingEntity) || 
               (entity instanceof BoatEntity) ||
               (entity instanceof PlayerEntity);
    }
}
