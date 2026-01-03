/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package voidstrike_dev.voidstrike_client.utils.render.postprocess;

import voidstrike_dev.voidstrike_client.renderer.MeshRenderer;
import voidstrike_dev.voidstrike_client.renderer.MeteorRenderPipelines;
import voidstrike_dev.voidstrike_client.systems.modules.Modules;
import voidstrike_dev.voidstrike_client.systems.modules.render.ESP;
import net.minecraft.entity.Entity;

public class EntityOutlineShader extends EntityShader {
    private static ESP esp;

    public EntityOutlineShader() {
        super(MeteorRenderPipelines.POST_OUTLINE);
    }

    @Override
    protected boolean shouldDraw() {
        if (esp == null) esp = Modules.get().get(ESP.class);
        return esp.isShader();
    }

    @Override
    public boolean shouldDraw(Entity entity) {
        if (!shouldDraw()) return false;
        return !esp.shouldSkip(entity);
    }

    @Override
    protected void setupPass(MeshRenderer renderer) {
        renderer.uniform("OutlineData", OutlineUniforms.write(
            esp.outlineWidth.get(),
            esp.fillOpacity.get().floatValue(),
            esp.shapeMode.get().ordinal(),
            esp.glowMultiplier.get().floatValue()
        ));
    }
}
