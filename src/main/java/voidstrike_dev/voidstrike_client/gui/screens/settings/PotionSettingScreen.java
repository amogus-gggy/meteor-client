/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package voidstrike_dev.voidstrike_client.gui.screens.settings;

import voidstrike_dev.voidstrike_client.gui.GuiTheme;
import voidstrike_dev.voidstrike_client.gui.WindowScreen;
import voidstrike_dev.voidstrike_client.gui.widgets.containers.WTable;
import voidstrike_dev.voidstrike_client.gui.widgets.pressable.WButton;
import voidstrike_dev.voidstrike_client.settings.PotionSetting;
import voidstrike_dev.voidstrike_client.utils.misc.MyPotion;
import net.minecraft.client.resource.language.I18n;

public class PotionSettingScreen extends WindowScreen {
    private final PotionSetting setting;

    public PotionSettingScreen(GuiTheme theme, PotionSetting setting) {
        super(theme, "Select Potion");

        this.setting = setting;
    }

    @Override
    public void initWidgets() {
        WTable table = add(theme.table()).expandX().widget();

        for (MyPotion potion : MyPotion.values()) {
            table.add(theme.itemWithLabel(potion.potion, I18n.translate(potion.potion.getItem().getTranslationKey())));

            WButton select = table.add(theme.button("Select")).widget();
            select.action = () -> {
                setting.set(potion);
                close();
            };

            table.row();
        }
    }
}
