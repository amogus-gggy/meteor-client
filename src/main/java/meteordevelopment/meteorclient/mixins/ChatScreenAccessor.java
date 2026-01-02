/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package meteordevelopment.meteorclient.mixins;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(net.minecraft.client.gui.screen.ChatScreen.class)
public interface ChatScreenAccessor {
    @Invoker("sendMessage")
    void invokeSendMessage(String chatText, boolean addToHistory);
}
