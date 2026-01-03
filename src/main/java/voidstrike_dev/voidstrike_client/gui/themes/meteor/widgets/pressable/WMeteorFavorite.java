/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package voidstrike_dev.voidstrike_client.gui.themes.meteor.widgets.pressable;

import voidstrike_dev.voidstrike_client.gui.themes.meteor.MeteorWidget;
import voidstrike_dev.voidstrike_client.gui.widgets.pressable.WFavorite;
import voidstrike_dev.voidstrike_client.utils.render.color.Color;

public class WMeteorFavorite extends WFavorite implements MeteorWidget {
    public WMeteorFavorite(boolean checked) {
        super(checked);
    }

    @Override
    protected Color getColor() {
        return theme().favoriteColor.get();
    }
}
