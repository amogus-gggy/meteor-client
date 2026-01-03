/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package voidstrike_dev.voidstrike_client.systems.modules.player;

import voidstrike_dev.voidstrike_client.events.entity.player.SendMovementPacketsEvent;
import voidstrike_dev.voidstrike_client.events.packets.PacketEvent;
import voidstrike_dev.voidstrike_client.mixin.PlayerMoveC2SPacketAccessor;
import voidstrike_dev.voidstrike_client.settings.BoolSetting;
import voidstrike_dev.voidstrike_client.settings.Setting;
import voidstrike_dev.voidstrike_client.settings.SettingGroup;
import voidstrike_dev.voidstrike_client.systems.modules.Categories;
import voidstrike_dev.voidstrike_client.systems.modules.Module;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;

public class AntiHunger extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<Boolean> sprint = sgGeneral.add(new BoolSetting.Builder()
        .name("sprint")
        .description("Spoofs sprinting packets.")
        .defaultValue(true)
        .build()
    );

    private final Setting<Boolean> onGround = sgGeneral.add(new BoolSetting.Builder()
        .name("on-ground")
        .description("Spoofs the onGround flag.")
        .defaultValue(true)
        .build()
    );

    private boolean lastOnGround, ignorePacket;

    public AntiHunger() {
        super(Categories.Player, "anti-hunger", "Reduces (does NOT remove) hunger consumption.");
    }

    @Override
    public void onActivate() {
        lastOnGround = mc.player.isOnGround();
    }

    @EventHandler
    private void onSendPacket(PacketEvent.Send event) {
        if (ignorePacket && event.packet instanceof PlayerMoveC2SPacket) {
            ignorePacket = false;
            return;
        }

        if (mc.player.hasVehicle() || mc.player.isTouchingWater() || mc.player.isSubmergedInWater()) return;

        if (event.packet instanceof ClientCommandC2SPacket packet && sprint.get()) {
            if (packet.getMode() == ClientCommandC2SPacket.Mode.START_SPRINTING) event.cancel();
        }

        if (event.packet instanceof PlayerMoveC2SPacket packet && onGround.get() && mc.player.isOnGround() && mc.player.fallDistance <= 0.0 && !mc.interactionManager.isBreakingBlock()) {
            ((PlayerMoveC2SPacketAccessor) packet).meteor$setOnGround(false);
        }
    }

    @EventHandler
    private void onTick(SendMovementPacketsEvent.Pre event) {
        if (mc.player.isOnGround() && !lastOnGround && onGround.get()) {
            ignorePacket = true; // prevents you from not taking fall damage
        }

        lastOnGround = mc.player.isOnGround();
    }
}
