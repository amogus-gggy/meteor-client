/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package voidstrike_dev.voidstrike_client.events.world;

import net.minecraft.world.chunk.WorldChunk;
import voidstrike_dev.voidstrike_client.utils.misc.Pool;

/**
 * @implNote Shouldn't be put in a {@link Pool} to avoid a race-condition, or in a {@link ThreadLocal} as it is shared between threads.
 * @author Crosby
 */
public record ChunkDataEvent(WorldChunk chunk) {}
