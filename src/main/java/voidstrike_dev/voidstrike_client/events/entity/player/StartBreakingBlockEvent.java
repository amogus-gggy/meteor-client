/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package voidstrike_dev.voidstrike_client.events.entity.player;

import voidstrike_dev.voidstrike_client.events.Cancellable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

public class StartBreakingBlockEvent extends Cancellable {
    private static final StartBreakingBlockEvent INSTANCE = new StartBreakingBlockEvent();

    public BlockPos blockPos;
    public Direction direction;

    public static StartBreakingBlockEvent get(BlockPos blockPos, Direction direction) {
        INSTANCE.setCancelled(false);
        INSTANCE.blockPos = blockPos;
        INSTANCE.direction = direction;
        return INSTANCE;
    }
}
