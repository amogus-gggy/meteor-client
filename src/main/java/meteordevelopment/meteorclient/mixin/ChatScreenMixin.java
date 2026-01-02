/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package meteordevelopment.meteorclient.mixin;

import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.systems.modules.misc.BetterChat;
import meteordevelopment.meteorclient.systems.modules.misc.LoginProtect;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = ChatScreen.class, priority = 1001)
public abstract class ChatScreenMixin {
    @Shadow protected TextFieldWidget chatField;
    @Shadow protected String originalChatText;

    @Inject(method = "init", at = @At(value = "RETURN"))
    private void onInit(CallbackInfo info) {
        if (Modules.get().get(BetterChat.class).isInfiniteChatBox()) chatField.setMaxLength(Integer.MAX_VALUE);

        // Set up text change listener for LoginProtect
        LoginProtect loginProtect = Modules.get().get(LoginProtect.class);
        if (loginProtect != null && loginProtect.isActive()) {
            chatField.setChangedListener(this::onTextChanged);
        }
    }

    @Unique
    private void onTextChanged(String newText) {
        LoginProtect loginProtect = Modules.get().get(LoginProtect.class);
        if (loginProtect == null || !loginProtect.isActive()) return;

        String lowerText = newText.toLowerCase();
        if (loginProtect.isProtectedCommand(lowerText)) {
            String maskedText = loginProtect.modifyMessage(newText);

            if (!maskedText.equals(newText)) {
                // Save original text for sending
                originalChatText = newText;

                // Update text field
                chatField.setText(maskedText);
            }
        } else {
            originalChatText = newText;
        }
    }

    @Inject(method = "sendMessage", at = @At("HEAD"), cancellable = true)
    private void onSendMessage(String chatText, boolean addToHistory, CallbackInfo ci) {
        LoginProtect loginProtect = Modules.get().get(LoginProtect.class);
        if (loginProtect == null || !loginProtect.isActive()) return;

        if (loginProtect.isProtectedCommand(chatText.toLowerCase())) {
            if (originalChatText != null) {
                MinecraftClient.getInstance().getNetworkHandler().sendChatMessage(originalChatText);
                if (addToHistory) {
                    // опционально: добавить в историю вручную
                    // chatField.addToHistory(originalChatText);
                }
            }
            ci.cancel();
        }
    }
}
