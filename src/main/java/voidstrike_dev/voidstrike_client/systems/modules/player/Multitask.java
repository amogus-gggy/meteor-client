/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package voidstrike_dev.voidstrike_client.systems.modules.player;

import voidstrike_dev.voidstrike_client.settings.BoolSetting;
import voidstrike_dev.voidstrike_client.settings.Setting;
import voidstrike_dev.voidstrike_client.settings.SettingGroup;
import voidstrike_dev.voidstrike_client.systems.modules.Categories;
import voidstrike_dev.voidstrike_client.systems.modules.Module;

public class Multitask extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<Boolean> attackingEntities = sgGeneral.add(new BoolSetting.Builder()
        .name("attacking-entities")
        .description("Lets you attack entities while using an item.")
        .defaultValue(true)
        .build()
    );

    public Multitask() {
        super(Categories.Player, "multitask", "Lets you use items and attack at the same time.");
    }

    public boolean attackingEntities() {
        return isActive() && attackingEntities.get();
    }
}
