/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package voidstrike_dev.voidstrike_client.gui.screens.settings;

import voidstrike_dev.voidstrike_client.gui.GuiTheme;
import voidstrike_dev.voidstrike_client.gui.screens.settings.base.CollectionListSettingScreen;
import voidstrike_dev.voidstrike_client.gui.widgets.WWidget;
import voidstrike_dev.voidstrike_client.settings.PacketListSetting;
import voidstrike_dev.voidstrike_client.settings.Setting;
import voidstrike_dev.voidstrike_client.utils.network.PacketUtils;
import net.minecraft.network.packet.Packet;

import java.util.Set;
import java.util.function.Predicate;

public class PacketBoolSettingScreen extends CollectionListSettingScreen<Class<? extends Packet<?>>> {
    public PacketBoolSettingScreen(GuiTheme theme, Setting<Set<Class<? extends Packet<?>>>> setting) {
        super(theme, "Select Packets", setting, setting.get(), PacketUtils.PACKETS);
    }

    @Override
    protected boolean includeValue(Class<? extends Packet<?>> value) {
        Predicate<Class<? extends Packet<?>>> filter = ((PacketListSetting) setting).filter;

        if (filter == null) return true;
        return filter.test(value);
    }

    @Override
    protected WWidget getValueWidget(Class<? extends Packet<?>> value) {
        return theme.label(PacketUtils.getName(value));
    }

    @Override
    protected String[] getValueNames(Class<? extends Packet<?>> value) {
        return new String[]{
            PacketUtils.getName(value),
            value.getSimpleName()
        };
    }
}
