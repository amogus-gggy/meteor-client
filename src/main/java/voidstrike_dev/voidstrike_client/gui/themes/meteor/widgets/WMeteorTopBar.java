/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package voidstrike_dev.voidstrike_client.gui.themes.meteor.widgets;

import voidstrike_dev.voidstrike_client.gui.themes.meteor.MeteorWidget;
import voidstrike_dev.voidstrike_client.gui.widgets.WTopBar;
import voidstrike_dev.voidstrike_client.utils.render.color.Color;

public class WMeteorTopBar extends WTopBar implements MeteorWidget {
    @Override
    protected Color getButtonColor(boolean pressed, boolean hovered) {
        return theme().backgroundColor.get(pressed, hovered);
    }

    @Override
    protected Color getNameColor() {
        return theme().textColor.get();
    }
}
