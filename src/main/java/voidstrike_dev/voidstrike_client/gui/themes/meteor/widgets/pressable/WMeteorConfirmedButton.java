/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package voidstrike_dev.voidstrike_client.gui.themes.meteor.widgets.pressable;

import voidstrike_dev.voidstrike_client.gui.renderer.GuiRenderer;
import voidstrike_dev.voidstrike_client.gui.renderer.packer.GuiTexture;
import voidstrike_dev.voidstrike_client.gui.themes.meteor.MeteorTheme;
import voidstrike_dev.voidstrike_client.gui.themes.meteor.MeteorWidget;
import voidstrike_dev.voidstrike_client.gui.widgets.pressable.WConfirmedButton;
import voidstrike_dev.voidstrike_client.utils.render.color.Color;

public class WMeteorConfirmedButton extends WConfirmedButton implements MeteorWidget {
    public WMeteorConfirmedButton(String text, String confirmText, GuiTexture texture) {
        super(text, confirmText, texture);
    }

    @Override
    protected void onRender(GuiRenderer renderer, double mouseX, double mouseY, double delta) {
        MeteorTheme theme = theme();
        double pad = pad();

        Color outline = theme.outlineColor.get(pressed, mouseOver);
        Color fg = pressedOnce ? theme.backgroundColor.get(pressed, mouseOver) : theme.textColor.get();
        Color bg = pressedOnce ? theme.textColor.get() : theme.backgroundColor.get(pressed, mouseOver);

        renderBackground(renderer, this, outline, bg);

        String text = getText();

        if (text != null) {
            renderer.text(text, x + width / 2 - textWidth / 2, y + pad, fg, false);
        }
        else {
            double ts = theme.textHeight();
            renderer.quad(x + width / 2 - ts / 2, y + pad, ts, ts, texture, fg);
        }
    }
}
