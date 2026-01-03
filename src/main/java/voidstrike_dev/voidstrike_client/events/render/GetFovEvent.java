/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package voidstrike_dev.voidstrike_client.events.render;

public class GetFovEvent {
    private static final GetFovEvent INSTANCE = new GetFovEvent();

    public float fov;

    public static GetFovEvent get(float fov) {
        INSTANCE.fov = fov;
        return INSTANCE;
    }
}
