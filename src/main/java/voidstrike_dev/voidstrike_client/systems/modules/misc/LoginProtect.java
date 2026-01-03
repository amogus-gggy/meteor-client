/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package voidstrike_dev.voidstrike_client.systems.modules.misc;

import voidstrike_dev.voidstrike_client.events.game.SendMessageEvent;
import voidstrike_dev.voidstrike_client.events.game.ReceiveMessageEvent;
import voidstrike_dev.voidstrike_client.settings.BoolSetting;
import voidstrike_dev.voidstrike_client.settings.Setting;
import voidstrike_dev.voidstrike_client.settings.SettingGroup;
import voidstrike_dev.voidstrike_client.systems.modules.Categories;
import voidstrike_dev.voidstrike_client.systems.modules.Module;
import voidstrike_dev.voidstrike_client.utils.player.ChatUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.List;

public class LoginProtect extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    // Commands to hide arguments for
    private final List<String> protectedCommands = List.of(
        "l", "login", "lof", "logout", "r", "reg", "register"
    );

    private final Setting<Boolean> hideInChat = sgGeneral.add(new BoolSetting.Builder()
        .name("hide-in-chat")
        .description("Hide login commands in chat history.")
        .defaultValue(true)
        .build()
    );

    private final Setting<Boolean> replaceWithStars = sgGeneral.add(new BoolSetting.Builder()
        .name("replace-with-stars")
        .description("Replace command arguments with stars in chat.")
        .defaultValue(true)
        .visible(() -> hideInChat.get())
        .build()
    );

    private final Setting<Boolean> hideServerMessages = sgGeneral.add(new BoolSetting.Builder()
        .name("hide-server-messages")
        .description("Hide login success/failure messages from server.")
        .defaultValue(false)
        .build()
    );

    public LoginProtect() {
        super(Categories.Misc, "login-protect", "Hides arguments for login/register commands on pirate servers.");
    }

    @EventHandler
    private void onSendMessage(SendMessageEvent event) {
        String message = event.message.toLowerCase();

        if (isProtectedCommand(message)) {
            if (hideInChat.get()) {
                event.cancel();
                String modifiedMessage = modifyMessage(message);

                // Send the original message but show modified version in chat
                ChatUtils.sendPlayerMsg(event.message, false);

                if (replaceWithStars.get()) {
                    mc.player.sendMessage(Text.literal("-> " + modifiedMessage).formatted(Formatting.GRAY), false);
                }
            }
        }
    }

    @EventHandler
    private void onReceiveMessage(ReceiveMessageEvent event) {
        if (hideServerMessages.get()) {
            String message = event.getMessage().getString().toLowerCase();

            // Check for common login/register server responses
            if (isLoginMessage(message)) {
                event.cancel();
            }
        }
    }

    public boolean isProtectedCommand(String message) {
        String[] parts = message.split(" ");
        if (parts.length == 0) return false;

        String command = parts[0].startsWith("/") ? parts[0].substring(1) : parts[0];
        return protectedCommands.contains(command);
    }

    public String modifyMessage(String originalMessage) {
        if (!replaceWithStars.get()) return originalMessage;

        String[] parts = originalMessage.split(" ");
        if (parts.length <= 1) return originalMessage;

        StringBuilder modified = new StringBuilder(parts[0]);
        for (int i = 1; i < parts.length; i++) {
            modified.append(" ");
            // Replace each character with star
            modified.append("*".repeat(parts[i].length()));
        }

        return modified.toString();
    }

    private boolean isLoginMessage(String message) {
        String lowerMessage = message.toLowerCase();

        // Common login/register success/failure messages
        return lowerMessage.contains("logged in") ||
               lowerMessage.contains("logged out") ||
               lowerMessage.contains("login") ||
               lowerMessage.contains("register") ||
               lowerMessage.contains("password") ||
               lowerMessage.contains("incorrect") ||
               lowerMessage.contains("success") ||
               lowerMessage.contains("failed") ||
               lowerMessage.contains("authentication") ||
               lowerMessage.contains("auth");
    }
}
