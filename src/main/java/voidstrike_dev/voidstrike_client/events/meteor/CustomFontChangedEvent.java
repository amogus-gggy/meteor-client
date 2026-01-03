/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package voidstrike_dev.voidstrike_client.events.meteor;

@SuppressWarnings("InstantiationOfUtilityClass")
public class CustomFontChangedEvent {
    private static final CustomFontChangedEvent INSTANCE = new CustomFontChangedEvent();

    public static CustomFontChangedEvent get() {
        return INSTANCE;
    }
}
