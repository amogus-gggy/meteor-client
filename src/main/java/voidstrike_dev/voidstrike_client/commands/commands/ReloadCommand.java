/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package voidstrike_dev.voidstrike_client.commands.commands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import voidstrike_dev.voidstrike_client.commands.Command;
import voidstrike_dev.voidstrike_client.renderer.Fonts;
import voidstrike_dev.voidstrike_client.systems.Systems;
import voidstrike_dev.voidstrike_client.systems.friends.Friend;
import voidstrike_dev.voidstrike_client.systems.friends.Friends;
import voidstrike_dev.voidstrike_client.utils.network.Capes;
import voidstrike_dev.voidstrike_client.utils.network.MeteorExecutor;
import net.minecraft.command.CommandSource;

public class ReloadCommand extends Command {
    public ReloadCommand() {
        super("reload", "Reloads many systems.");
    }

    @Override
    public void build(LiteralArgumentBuilder<CommandSource> builder) {
        builder.executes(context -> {
            warning("Reloading systems, this may take a while.");

            Systems.load();
            Capes.init();
            Fonts.refresh();
            MeteorExecutor.execute(() -> Friends.get().forEach(Friend::updateInfo));

            return SINGLE_SUCCESS;
        });
    }
}
