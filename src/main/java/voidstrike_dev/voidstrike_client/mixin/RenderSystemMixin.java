/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package voidstrike_dev.voidstrike_client.mixin;

import com.mojang.blaze3d.systems.RenderSystem;
import voidstrike_dev.voidstrike_client.renderer.MeshUniforms;
import voidstrike_dev.voidstrike_client.utils.render.postprocess.ChamsShader;
import voidstrike_dev.voidstrike_client.utils.render.postprocess.OutlineUniforms;
import voidstrike_dev.voidstrike_client.utils.render.postprocess.PostProcessShader;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(RenderSystem.class)
public abstract class RenderSystemMixin {
    @Inject(method = "flipFrame", at = @At("TAIL"))
    private static void meteor$flipFrame(CallbackInfo info) {
        MeshUniforms.flipFrame();
        PostProcessShader.flipFrame();
        ChamsShader.flipFrame();
        OutlineUniforms.flipFrame();
    }
}
