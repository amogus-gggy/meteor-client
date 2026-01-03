/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package voidstrike_dev.voidstrike_client.gui.themes.meteor.widgets;

import voidstrike_dev.voidstrike_client.gui.WidgetScreen;
import voidstrike_dev.voidstrike_client.gui.themes.meteor.MeteorWidget;
import voidstrike_dev.voidstrike_client.gui.widgets.WAccount;
import voidstrike_dev.voidstrike_client.systems.accounts.Account;
import voidstrike_dev.voidstrike_client.utils.render.color.Color;

public class WMeteorAccount extends WAccount implements MeteorWidget {
    public WMeteorAccount(WidgetScreen screen, Account<?> account) {
        super(screen, account);
    }

    @Override
    protected Color loggedInColor() {
        return theme().loggedInColor.get();
    }

    @Override
    protected Color accountTypeColor() {
        return theme().textSecondaryColor.get();
    }
}
