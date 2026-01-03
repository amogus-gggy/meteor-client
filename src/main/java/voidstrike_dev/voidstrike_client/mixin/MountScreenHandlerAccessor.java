/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package voidstrike_dev.voidstrike_client.mixin;

import net.minecraft.entity.LivingEntity;
import net.minecraft.screen.MountScreenHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(MountScreenHandler.class)
public interface MountScreenHandlerAccessor {
    @Accessor("mount")
    LivingEntity meteor$getMount();
}
