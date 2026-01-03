/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package voidstrike_dev.voidstrike_client.gui.widgets;

import voidstrike_dev.voidstrike_client.gui.WidgetScreen;
import voidstrike_dev.voidstrike_client.gui.screens.accounts.AccountInfoScreen;
import voidstrike_dev.voidstrike_client.gui.widgets.containers.WHorizontalList;
import voidstrike_dev.voidstrike_client.gui.widgets.pressable.WButton;
import voidstrike_dev.voidstrike_client.gui.widgets.pressable.WConfirmedMinus;
import voidstrike_dev.voidstrike_client.systems.accounts.Account;
import voidstrike_dev.voidstrike_client.systems.accounts.Accounts;
import voidstrike_dev.voidstrike_client.systems.accounts.TokenAccount;
import voidstrike_dev.voidstrike_client.utils.network.MeteorExecutor;
import voidstrike_dev.voidstrike_client.utils.render.color.Color;

import static voidstrike_dev.voidstrike_client.MeteorClient.mc;

public abstract class WAccount extends WHorizontalList {
    public Runnable refreshScreenAction;
    private final WidgetScreen screen;
    private final Account<?> account;

    public WAccount(WidgetScreen screen, Account<?> account) {
        this.screen = screen;
        this.account = account;
    }

    protected abstract Color loggedInColor();
    protected abstract Color accountTypeColor();

    @Override
    public void init() {
        // Head
        add(theme.texture(32, 32, account.getCache().getHeadTexture().needsRotate() ? 90 : 0, account.getCache().getHeadTexture()));

        // Name
        WLabel name = add(theme.label(account.getUsername())).widget();
        if (mc.getSession().getUsername().equalsIgnoreCase(account.getUsername())) name.color = loggedInColor();

        // Type
        WLabel label = add(theme.label("(" + account.getType() + ")")).expandCellX().right().widget();
        label.color = accountTypeColor();

        // Info
        if (account instanceof TokenAccount) {
            WButton info = add(theme.button("Info")).widget();
            info.action = () -> mc.setScreen(new AccountInfoScreen(theme, account));
        }

        // Login
        WButton login = add(theme.button("Login")).widget();
        login.action = () -> {
            login.minWidth = login.width;
            login.set("...");
            screen.locked = true;

            MeteorExecutor.execute(() -> {
                if (account.fetchInfo() && account.login()) {
                    name.set(account.getUsername());

                    Accounts.get().save();

                    screen.taskAfterRender = refreshScreenAction;
                }

                login.minWidth = 0;
                login.set("Login");
                screen.locked = false;
            });
        };

        // Remove
        WConfirmedMinus remove = add(theme.confirmedMinus()).widget();
        remove.action = () -> {
            Accounts.get().remove(account);
            if (refreshScreenAction != null) refreshScreenAction.run();
        };
    }
}
