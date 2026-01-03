/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package voidstrike_dev.voidstrike_client.mixininterface;

import com.mojang.authlib.GameProfile;

public interface IChatHudLine {
    String meteor$getText();

    int meteor$getId();

    void meteor$setId(int id);

    GameProfile meteor$getSender();

    void meteor$setSender(GameProfile profile);
}
