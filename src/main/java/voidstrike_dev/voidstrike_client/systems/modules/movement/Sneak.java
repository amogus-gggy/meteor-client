/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package voidstrike_dev.voidstrike_client.systems.modules.movement;

import voidstrike_dev.voidstrike_client.settings.EnumSetting;
import voidstrike_dev.voidstrike_client.settings.Setting;
import voidstrike_dev.voidstrike_client.settings.SettingGroup;
import voidstrike_dev.voidstrike_client.systems.modules.Categories;
import voidstrike_dev.voidstrike_client.systems.modules.Module;

public class Sneak extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<Mode> mode = sgGeneral.add(new EnumSetting.Builder<Mode>()
        .name("mode")
        .description("Which method to sneak.")
        .defaultValue(Mode.Vanilla)
        .build()
    );

    public Sneak() {
        super (Categories.Movement, "sneak", "Sneaks for you");
    }

    public boolean doPacket() {
        return isActive() && !mc.player.getAbilities().flying && mode.get() == Mode.Packet;
    }

    public boolean doVanilla() {
        return isActive() && !mc.player.getAbilities().flying && mode.get() == Mode.Vanilla;
    }

    public enum Mode {
        Packet,
        Vanilla
    }
}
