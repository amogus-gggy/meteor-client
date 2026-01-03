/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package voidstrike_dev.voidstrike_client.commands.commands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import voidstrike_dev.voidstrike_client.commands.Command;
import voidstrike_dev.voidstrike_client.commands.arguments.ModuleArgumentType;
import voidstrike_dev.voidstrike_client.commands.arguments.SettingArgumentType;
import voidstrike_dev.voidstrike_client.commands.arguments.SettingValueArgumentType;
import voidstrike_dev.voidstrike_client.gui.GuiThemes;
import voidstrike_dev.voidstrike_client.gui.WidgetScreen;
import voidstrike_dev.voidstrike_client.gui.tabs.TabScreen;
import voidstrike_dev.voidstrike_client.gui.tabs.Tabs;
import voidstrike_dev.voidstrike_client.gui.tabs.builtin.HudTab;
import voidstrike_dev.voidstrike_client.settings.Setting;
import voidstrike_dev.voidstrike_client.systems.modules.Module;
import voidstrike_dev.voidstrike_client.utils.Utils;
import net.minecraft.command.CommandSource;

public class SettingCommand extends Command {
    public SettingCommand() {
        super("settings", "Allows you to view and change module settings.", "s");
    }

    @Override
    public void build(LiteralArgumentBuilder<CommandSource> builder) {
        builder.then(
            literal("hud")
                .executes(context -> {
                    TabScreen screen = Tabs.get(HudTab.class).createScreen(GuiThemes.get());
                    screen.parent = null;

                    Utils.screenToOpen = screen;
                    return SINGLE_SUCCESS;
                })
        );

        // Open module screen
        builder.then(
            argument("module", ModuleArgumentType.create())
                .executes(context -> {
                    Module module = context.getArgument("module", Module.class);

                    WidgetScreen screen = GuiThemes.get().moduleScreen(module);
                    screen.parent = null;

                    Utils.screenToOpen = screen;
                    return SINGLE_SUCCESS;
                })
        );

        // View or change settings
        builder.then(
                argument("module", ModuleArgumentType.create())
                .then(
                        argument("setting", SettingArgumentType.create())
                        .executes(context -> {
                            // Get setting value
                            Setting<?> setting = SettingArgumentType.get(context);

                            ModuleArgumentType.get(context).info("Setting (highlight)%s(default) is (highlight)%s(default).", setting.title, setting.get());

                            return SINGLE_SUCCESS;
                        })
                        .then(
                                argument("value", SettingValueArgumentType.create())
                                .executes(context -> {
                                    // Set setting value
                                    Setting<?> setting = SettingArgumentType.get(context);
                                    String value = SettingValueArgumentType.get(context);

                                    if (setting.parse(value)) {
                                        ModuleArgumentType.get(context).info("Setting (highlight)%s(default) changed to (highlight)%s(default).", setting.title, value);
                                    }

                                    return SINGLE_SUCCESS;
                                })
                        )
                )
        );
    }
}
