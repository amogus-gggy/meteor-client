/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package voidstrike_dev.voidstrike_client.gui.screens.accounts;

import voidstrike_dev.voidstrike_client.gui.GuiTheme;
import voidstrike_dev.voidstrike_client.gui.WindowScreen;
import voidstrike_dev.voidstrike_client.gui.widgets.containers.WHorizontalList;
import voidstrike_dev.voidstrike_client.gui.widgets.pressable.WButton;
import voidstrike_dev.voidstrike_client.systems.accounts.Account;
import voidstrike_dev.voidstrike_client.systems.accounts.AccountType;
import voidstrike_dev.voidstrike_client.systems.accounts.TokenAccount;
import voidstrike_dev.voidstrike_client.utils.render.color.Color;

import static voidstrike_dev.voidstrike_client.MeteorClient.mc;

public class AccountInfoScreen extends WindowScreen {
    private final Account<?> account;

    public AccountInfoScreen(GuiTheme theme, Account<?> account) {
        super(theme, account.getUsername() + " details");
        this.account = account;
    }

    @Override
    public void initWidgets() {
        TokenAccount e = (TokenAccount) account;
        WHorizontalList l = add(theme.horizontalList()).expandX().widget();

        String tokenLabel = account.getType() + " token:";
        if (account.getType() == AccountType.Session) tokenLabel = "";

        WButton copy = theme.button("Copy");
        copy.action = () -> mc.keyboard.setClipboard(e.getToken());

        l.add(theme.label(tokenLabel));
        l.add(theme.label(account.getType() == AccountType.Session ? "Click to copy Token" : e.getToken()).color(Color.GRAY)).pad(5);
        l.add(copy);
    }
}
