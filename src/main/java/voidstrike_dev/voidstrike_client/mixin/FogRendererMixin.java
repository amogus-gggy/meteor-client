/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package voidstrike_dev.voidstrike_client.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import voidstrike_dev.voidstrike_client.systems.modules.Modules;
import voidstrike_dev.voidstrike_client.systems.modules.render.NoRender;
import voidstrike_dev.voidstrike_client.systems.modules.world.Ambience;
import net.minecraft.client.render.fog.FogRenderer;
import org.joml.Vector4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(FogRenderer.class)
public abstract class FogRendererMixin {
    @ModifyVariable(method = "applyFog(Ljava/nio/ByteBuffer;ILorg/joml/Vector4f;FFFFFF)V", at = @At("HEAD"), argsOnly = true)
    private Vector4f modifyFogDistance(Vector4f original) {
        if (Modules.get() == null) return original;

        Ambience ambience = Modules.get().get(Ambience.class);
        if (ambience.isActive() && ambience.customFogColor.get()) {
            return ambience.fogColor.get().getVec4f();
        }

        return original;
    }

    @ModifyExpressionValue(method = "getFogBuffer", at = @At(value = "FIELD", target = "Lnet/minecraft/client/render/fog/FogRenderer;fogEnabled:Z"))
    private boolean modifyFogEnabled(boolean original) {
        if (Modules.get() == null) return original;

        return original && !Modules.get().get(NoRender.class).noFog();
    }
}
