/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package voidstrike_dev.voidstrike_client.mixin;

import voidstrike_dev.voidstrike_client.mixininterface.IAbstractFurnaceScreenHandler;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.AbstractFurnaceScreenHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(AbstractFurnaceScreenHandler.class)
public abstract class AbstractFurnaceScreenHandlerMixin implements IAbstractFurnaceScreenHandler {
    @Shadow
    protected abstract boolean isSmeltable(ItemStack itemStack);

    @Override
    public boolean meteor$isItemSmeltable(ItemStack itemStack) {
        return isSmeltable(itemStack);
    }
}
