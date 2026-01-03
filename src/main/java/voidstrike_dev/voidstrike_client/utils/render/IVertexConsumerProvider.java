/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package voidstrike_dev.voidstrike_client.utils.render;

import net.minecraft.client.render.VertexConsumerProvider;

public interface IVertexConsumerProvider extends VertexConsumerProvider {
    void setOffset(int offsetX, int offsetY, int offsetZ);
}
