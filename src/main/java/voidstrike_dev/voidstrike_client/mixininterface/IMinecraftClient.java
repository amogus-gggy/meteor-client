/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package voidstrike_dev.voidstrike_client.mixininterface;

import net.minecraft.client.gl.Framebuffer;

public interface IMinecraftClient {
    void meteor$rightClick();

    void meteor$setFramebuffer(Framebuffer framebuffer);
}
