/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package voidstrike_dev.voidstrike_client.gui.screens.settings;

import voidstrike_dev.voidstrike_client.gui.GuiTheme;
import voidstrike_dev.voidstrike_client.gui.screens.settings.base.CollectionListSettingScreen;
import voidstrike_dev.voidstrike_client.gui.widgets.WWidget;
import voidstrike_dev.voidstrike_client.settings.ItemListSetting;
import voidstrike_dev.voidstrike_client.utils.misc.Names;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;

import java.util.function.Predicate;

public class ItemListSettingScreen extends CollectionListSettingScreen<Item> {
    public ItemListSettingScreen(GuiTheme theme, ItemListSetting setting) {
        super(theme, "Select Items", setting, setting.get(), Registries.ITEM);
    }

    @Override
    protected boolean includeValue(Item value) {
        Predicate<Item> filter = ((ItemListSetting) setting).filter;
        if (filter != null && !filter.test(value)) return false;

        return value != Items.AIR;
    }

    @Override
    protected WWidget getValueWidget(Item value) {
        return theme.itemWithLabel(value.getDefaultStack());
    }

    @Override
    protected String[] getValueNames(Item value) {
        return new String[]{
            Names.get(value),
            Registries.ITEM.getId(value).toString()
        };
    }
}
