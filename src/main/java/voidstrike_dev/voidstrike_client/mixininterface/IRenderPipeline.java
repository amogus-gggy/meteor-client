/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package voidstrike_dev.voidstrike_client.mixininterface;

public interface IRenderPipeline {
    void meteor$setLineSmooth(boolean lineSmooth);

    boolean meteor$getLineSmooth();
}
