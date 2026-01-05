/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package voidstrike_dev.voidstrike_client.systems.modules.movement.speed;

import voidstrike_dev.voidstrike_client.events.entity.player.PlayerMoveEvent;
import voidstrike_dev.voidstrike_client.events.packets.PacketEvent;
import voidstrike_dev.voidstrike_client.events.world.TickEvent;
import voidstrike_dev.voidstrike_client.settings.*;
import voidstrike_dev.voidstrike_client.settings.*;
import voidstrike_dev.voidstrike_client.systems.modules.Categories;
import voidstrike_dev.voidstrike_client.systems.modules.Module;
import voidstrike_dev.voidstrike_client.systems.modules.Modules;
import voidstrike_dev.voidstrike_client.systems.modules.movement.speed.modes.GrimCollide;
import voidstrike_dev.voidstrike_client.systems.modules.movement.speed.modes.Strafe;
import voidstrike_dev.voidstrike_client.systems.modules.movement.speed.modes.Vanilla;
import voidstrike_dev.voidstrike_client.systems.modules.world.Timer;
import voidstrike_dev.voidstrike_client.utils.player.PlayerUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.entity.MovementType;
import net.minecraft.network.packet.s2c.play.PlayerPositionLookS2CPacket;

public class Speed extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    public final Setting<SpeedModes> speedMode = sgGeneral.add(new EnumSetting.Builder<SpeedModes>()
        .name("mode")
        .description("The method of applying speed.")
        .defaultValue(SpeedModes.Vanilla)
        .onModuleActivated(speedModesSetting -> onSpeedModeChanged(speedModesSetting.get()))
        .onChanged(this::onSpeedModeChanged)
        .build()
    );

    public final Setting<Double> vanillaSpeed = sgGeneral.add(new DoubleSetting.Builder()
        .name("vanilla-speed")
        .description("The speed in blocks per second.")
        .defaultValue(5.6)
        .min(0)
        .sliderMax(20)
        .visible(() -> speedMode.get() == SpeedModes.Vanilla)
        .build()
    );

    public final Setting<Double> ncpSpeed = sgGeneral.add(new DoubleSetting.Builder()
        .name("strafe-speed")
        .description("The speed.")
        .visible(() -> speedMode.get() == SpeedModes.Strafe)
        .defaultValue(1.6)
        .min(0)
        .sliderMax(3)
        .build()
    );

    public final Setting<Boolean> ncpSpeedLimit = sgGeneral.add(new BoolSetting.Builder()
        .name("speed-limit")
        .description("Limits your speed on servers with very strict anticheats.")
        .visible(() -> speedMode.get() == SpeedModes.Strafe)
        .defaultValue(false)
        .build()
    );

    public final Setting<Double> grimCollideSpeed = sgGeneral.add(new DoubleSetting.Builder()
        .name("grim-collide-speed")
        .description("The boost speed when colliding with entities.")
        .visible(() -> speedMode.get() == SpeedModes.GrimCollide)
        .defaultValue(0.08)
        .min(0.01)
        .sliderMax(0.2)
        .build()
    );

    public final Setting<Double> grimCollideRange = sgGeneral.add(new DoubleSetting.Builder()
        .name("grim-collide-range")
        .description("Maximum range to find nearest entity for directional boost.")
        .visible(() -> speedMode.get() == SpeedModes.GrimCollide)
        .defaultValue(3.0)
        .min(0.5)
        .sliderMax(10.0)
        .build()
    );

    public final Setting<Double> grimCollideExpand = sgGeneral.add(new DoubleSetting.Builder()
        .name("grim-collide-expand")
        .description("Collision box expansion size.")
        .visible(() -> speedMode.get() == SpeedModes.GrimCollide)
        .defaultValue(0.5)
        .min(0.1)
        .sliderMax(2.0)
        .build()
    );

    public final Setting<Boolean> grimCollideOnlyPlayers = sgGeneral.add(new BoolSetting.Builder()
        .name("grim-collide-only-players")
        .description("Only boost when colliding with players.")
        .visible(() -> speedMode.get() == SpeedModes.GrimCollide)
        .defaultValue(false)
        .build()
    );

    public final Setting<Boolean> grimCollideRequireMoving = sgGeneral.add(new BoolSetting.Builder()
        .name("grim-collide-require-moving")
        .description("Require player movement to apply speed boost.")
        .visible(() -> speedMode.get() == SpeedModes.GrimCollide)
        .defaultValue(true)
        .build()
    );

    public final Setting<Boolean> grimCollideRequireUpward = sgGeneral.add(new BoolSetting.Builder()
        .name("grim-collide-require-upward")
        .description("Only apply boost when moving upward.")
        .visible(() -> speedMode.get() == SpeedModes.GrimCollide)
        .defaultValue(false)
        .build()
    );

    public final Setting<Double> timer = sgGeneral.add(new DoubleSetting.Builder()
        .name("timer")
        .description("Timer override.")
        .defaultValue(1)
        .min(0.01)
        .sliderMin(0.01)
        .sliderMax(10)
        .build()
    );

    public final Setting<Boolean> inLiquids = sgGeneral.add(new BoolSetting.Builder()
        .name("in-liquids")
        .description("Uses speed when in lava or water.")
        .defaultValue(false)
        .build()
    );

    public final Setting<Boolean> whenSneaking = sgGeneral.add(new BoolSetting.Builder()
        .name("when-sneaking")
        .description("Uses speed when sneaking.")
        .defaultValue(false)
        .build()
    );

    public final Setting<Boolean> vanillaOnGround = sgGeneral.add(new BoolSetting.Builder()
        .name("only-on-ground")
        .description("Uses speed only when standing on a block.")
        .visible(() -> speedMode.get() == SpeedModes.Vanilla)
        .defaultValue(false)
        .build()
    );

    private SpeedMode currentMode;

    public Speed() {
        super(Categories.Movement, "speed", "Modifies your movement speed when moving on the ground.");

        onSpeedModeChanged(speedMode.get());
    }

    @Override
    public void onActivate() {
        currentMode.onActivate();
    }

    @Override
    public void onDeactivate() {
        Modules.get().get(Timer.class).setOverride(Timer.OFF);
        currentMode.onDeactivate();
    }

    @EventHandler
    private void onPlayerMove(PlayerMoveEvent event) {
        if (event.type != MovementType.SELF || stopSpeed()) return;

        if (timer.get() != Timer.OFF) {
            Modules.get().get(Timer.class).setOverride(PlayerUtils.isMoving() ? timer.get() : Timer.OFF);
        }

        currentMode.onMove(event);
    }

    @EventHandler
    private void onPreTick(TickEvent.Pre event) {
        if (stopSpeed()) return;

        currentMode.onTick();
    }

    @EventHandler
    private void onPacketReceive(PacketEvent.Receive event) {
        if (event.packet instanceof PlayerPositionLookS2CPacket) currentMode.onRubberband();
    }

    private void onSpeedModeChanged(SpeedModes mode) {
        switch (mode) {
            case Vanilla -> currentMode = new Vanilla();
            case Strafe -> currentMode = new Strafe();
            case GrimCollide -> currentMode = new GrimCollide();
        }
    }

    private boolean stopSpeed() {
        if (mc.player.isGliding() || mc.player.isClimbing() || mc.player.getVehicle() != null) return true;
        if (!whenSneaking.get() && mc.player.isSneaking()) return true;
        if (vanillaOnGround.get() && !mc.player.isOnGround() && speedMode.get() == SpeedModes.Vanilla) return true;
        return !inLiquids.get() && (mc.player.isTouchingWater() || mc.player.isInLava());
    }

    @Override
    public String getInfoString() {
        return currentMode.getHudString();
    }
}
