/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package voidstrike_dev.voidstrike_client.events.meteor;

import voidstrike_dev.voidstrike_client.events.Cancellable;
import voidstrike_dev.voidstrike_client.utils.misc.input.KeyAction;
import net.minecraft.client.input.KeyInput;

public class KeyEvent extends Cancellable {
    private static final KeyEvent INSTANCE = new KeyEvent();

    public KeyInput input;
    public KeyAction action;

    public static KeyEvent get(KeyInput input, KeyAction action) {
        INSTANCE.setCancelled(false);
        INSTANCE.input = input;
        INSTANCE.action = action;
        return INSTANCE;
    }

    public int key() {
        return INSTANCE.input.key();
    }

    public int modifiers() {
        return INSTANCE.input.modifiers();
    }
}
