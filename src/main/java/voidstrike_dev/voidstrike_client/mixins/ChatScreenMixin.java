/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package voidstrike_dev.voidstrike_client.mixins;

import voidstrike_dev.voidstrike_client.systems.modules.Modules;
import voidstrike_dev.voidstrike_client.systems.modules.misc.LoginProtect;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ChatScreen.class)
public abstract class ChatScreenMixin {
    @Shadow
    protected TextFieldWidget chatField;

    @Shadow
    private String originalChatText;

    // Flag to prevent recursive calls when sending original message
    private boolean isSendingProtectedMessage = false;

    @Inject(method = "init", at = @At("TAIL"))
    private void onInit(CallbackInfo ci) {
        LoginProtect loginProtect = Modules.get().get(LoginProtect.class);
        if (loginProtect != null && loginProtect.isActive()) {
            // Добавляем listener для отслеживания изменений в поле ввода
            chatField.setChangedListener(this::onChatFieldChanged);
        }
    }

    private void onChatFieldChanged(String newText) {
        LoginProtect loginProtect = Modules.get().get(LoginProtect.class);
        if (loginProtect == null || !loginProtect.isActive()) return;

        String lowerText = newText.toLowerCase();
        if (loginProtect.isProtectedCommand(lowerText)) {
            // Заменяем текст на версию со звездочками
            String maskedText = loginProtect.modifyMessage(newText);

            // Обновляем текст в поле, но сохраняем оригинал для отправки
            chatField.setText(maskedText);

            // Сохраняем оригинальный текст для отправки
            originalChatText = newText;
        } else {
            // Если это не защищенная команда, используем оригинальный текст
            originalChatText = newText;
        }
    }

    @Inject(method = "sendMessage", at = @At("HEAD"), cancellable = true)
    private void onSendMessage(String chatText, boolean addToHistory, CallbackInfo ci) {
        LoginProtect loginProtect = Modules.get().get(LoginProtect.class);
        if (loginProtect == null || !loginProtect.isActive()) return;

        // Prevent recursive calls when sending protected message
        if (isSendingProtectedMessage) return;

        String lowerText = chatText.toLowerCase();
        if (loginProtect.isProtectedCommand(lowerText)) {
            // Отправляем оригинальный текст (сохраненный в originalChatText)
            if (originalChatText != null) {
                // Set flag to prevent recursion
                isSendingProtectedMessage = true;
                try {
                    // Используем оригинальный метод для отправки
                    ((ChatScreenAccessor) this).invokeSendMessage(originalChatText, addToHistory);
                } finally {
                    // Always reset the flag
                    isSendingProtectedMessage = false;
                }
            }
            ci.cancel();
        }
    }
}
