/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package voidstrike_dev.voidstrike_client.systems.hud.elements;

import voidstrike_dev.voidstrike_client.settings.*;
import voidstrike_dev.voidstrike_client.systems.hud.Hud;
import voidstrike_dev.voidstrike_client.systems.hud.HudElement;
import voidstrike_dev.voidstrike_client.systems.hud.HudElementInfo;
import voidstrike_dev.voidstrike_client.systems.hud.HudRenderer;
import voidstrike_dev.voidstrike_client.systems.modules.Modules;
import voidstrike_dev.voidstrike_client.systems.modules.combat.KillAura2;
import voidstrike_dev.voidstrike_client.utils.entity.EntityUtils;
import voidstrike_dev.voidstrike_client.utils.render.color.Color;
import voidstrike_dev.voidstrike_client.utils.render.color.SettingColor;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;

public class TargetESP extends HudElement {
    public static final HudElementInfo<TargetESP> INFO = new HudElementInfo<>(Hud.GROUP, "target-esp", "Displays information about KillAura2 target near center of screen.", TargetESP::new);

    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<Boolean> active = sgGeneral.add(new BoolSetting.Builder()
        .name("active")
        .description("Whether to show target information.")
        .defaultValue(true)
        .build()
    );

    private final Setting<Boolean> showHealth = sgGeneral.add(new BoolSetting.Builder()
        .name("show-health")
        .description("Show target health information.")
        .defaultValue(true)
        .build()
    );

    private final Setting<Boolean> showDistance = sgGeneral.add(new BoolSetting.Builder()
        .name("show-distance")
        .description("Show distance to target.")
        .defaultValue(true)
        .build()
    );

    private final Setting<Boolean> showName = sgGeneral.add(new BoolSetting.Builder()
        .name("show-name")
        .description("Show target name.")
        .defaultValue(true)
        .build()
    );

    private final Setting<Double> scale = sgGeneral.add(new DoubleSetting.Builder()
        .name("scale")
        .description("Scale of the target information.")
        .defaultValue(1.0)
        .min(0.5)
        .max(3.0)
        .sliderMax(2.0)
        .build()
    );

    private final Setting<SettingColor> textColor = sgGeneral.add(new ColorSetting.Builder()
        .name("text-color")
        .description("Color of the target information text.")
        .defaultValue(new SettingColor(255, 255, 255, 255))
        .build()
    );

    private final Setting<SettingColor> backgroundColor = sgGeneral.add(new ColorSetting.Builder()
        .name("background-color")
        .description("Color of the background.")
        .defaultValue(new SettingColor(0, 0, 0, 150))
        .build()
    );

    private final Setting<Boolean> shadow = sgGeneral.add(new BoolSetting.Builder()
        .name("shadow")
        .description("Render text shadow.")
        .defaultValue(true)
        .build()
    );

    public TargetESP() {
        super(INFO);
    }

    @Override
    public void render(HudRenderer renderer) {
        if (!active.get()) return;
        
        KillAura2 killAura = Modules.get().get(KillAura2.class);
        if (killAura == null || !killAura.isActive()) {
            renderText(renderer, "None");
            return;
        }

        Entity target = killAura.getTarget();
        if (target == null || !(target instanceof LivingEntity livingTarget)) {
            renderText(renderer, "None");
            return;
        }

        // Build target information
        StringBuilder info = new StringBuilder();

        if (showName.get()) {
            info.append(EntityUtils.getName(target));
        }

        if (showHealth.get()) {
            if (!info.isEmpty()) info.append(" ");
            float health = livingTarget.getHealth();
            float maxHealth = livingTarget.getMaxHealth();
            info.append(String.format("%.1f/%.1f HP", health, maxHealth));
        }

        if (showDistance.get()) {
            if (!info.isEmpty()) info.append(" ");
            if (MinecraftClient.getInstance().player != null) {
                double distance = MinecraftClient.getInstance().player.distanceTo(target);
                info.append(String.format("%.1fm", distance));
            }
        }

        if (info.isEmpty()) {
            renderText(renderer, "None");
            return;
        }

        String text = info.toString();
        renderText(renderer, text);
    }

    private void renderText(HudRenderer renderer, String text) {
        double textWidth = renderer.textWidth(text, scale.get().floatValue());
        double textHeight = renderer.textHeight();

        // Position near center of screen
        double x = MinecraftClient.getInstance().getWindow().getScaledWidth() / 2.0 - textWidth / 2.0;
        double y = MinecraftClient.getInstance().getWindow().getScaledHeight() / 2.0 + 30; // 30 pixels below center

        // Update bounds for HUD editor
        setSize(textWidth + 4, textHeight + 4);
        this.x = (int) (x - 2);
        this.y = (int) (y - 2);

        // Draw background
        if (backgroundColor.get().a > 0) {
            renderer.quad(x - 2, y - 2, (float) textWidth + 4, (float) textHeight + 4, backgroundColor.get());
        }

        // Draw text
        renderer.text(text, x, y, textColor.get(), shadow.get(), scale.get().floatValue());
    }
}
