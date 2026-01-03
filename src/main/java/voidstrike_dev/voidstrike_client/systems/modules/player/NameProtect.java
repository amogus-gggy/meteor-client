/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package voidstrike_dev.voidstrike_client.systems.modules.player;

import voidstrike_dev.voidstrike_client.settings.BoolSetting;
import voidstrike_dev.voidstrike_client.settings.Setting;
import voidstrike_dev.voidstrike_client.settings.SettingGroup;
import voidstrike_dev.voidstrike_client.settings.StringSetting;
import voidstrike_dev.voidstrike_client.systems.modules.Categories;
import voidstrike_dev.voidstrike_client.systems.modules.Module;

public class NameProtect extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<Boolean> nameProtect = sgGeneral.add(new BoolSetting.Builder()
        .name("name-protect")
        .description("Hides your name client-side.")
        .defaultValue(true)
        .build()
    );

    private final Setting<String> name = sgGeneral.add(new StringSetting.Builder()
        .name("name")
        .description("Name to be replaced with.")
        .defaultValue("seasnail") //Lets pay respect for his impact to project
        .visible(nameProtect::get)
        .build()
    );

    private final Setting<Boolean> skinProtect = sgGeneral.add(new BoolSetting.Builder()
        .name("skin-protect")
        .description("Make players become Steves.")
        .defaultValue(true)
        .build()
    );

    private String username = "";

    public NameProtect() {
        super(Categories.Player, "name-protect", "Hide player names and skins.");
    }

    @Override
    public void onActivate() {
        if (mc.getSession() != null) {
            username = mc.getSession().getUsername();
        }
    }

    public String replaceName(String string) {
        if (string != null && isActive() && nameProtect.get() && !username.isEmpty()) {
            return string.replace(username, name.get());
        }
        return string;
    }

    public String getName(String original) {
        if (isActive() && nameProtect.get() && !name.get().isEmpty()) {
            return name.get();
        }
        return original;
    }

    public boolean skinProtect() {
        return isActive() && skinProtect.get();
    }
}
