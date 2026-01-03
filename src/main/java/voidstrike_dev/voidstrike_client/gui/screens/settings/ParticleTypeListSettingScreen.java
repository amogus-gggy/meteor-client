/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package voidstrike_dev.voidstrike_client.gui.screens.settings;

import voidstrike_dev.voidstrike_client.gui.GuiTheme;
import voidstrike_dev.voidstrike_client.gui.screens.settings.base.CollectionListSettingScreen;
import voidstrike_dev.voidstrike_client.gui.widgets.WWidget;
import voidstrike_dev.voidstrike_client.settings.Setting;
import voidstrike_dev.voidstrike_client.utils.misc.Names;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleType;
import net.minecraft.registry.Registries;

import java.util.List;

public class ParticleTypeListSettingScreen extends CollectionListSettingScreen<ParticleType<?>> {
    public ParticleTypeListSettingScreen(GuiTheme theme, Setting<List<ParticleType<?>>> setting) {
        super(theme, "Select Particles", setting, setting.get(), Registries.PARTICLE_TYPE);
    }

    @Override
    protected boolean includeValue(ParticleType<?> value) {
        return value instanceof ParticleEffect;
    }

    @Override
    protected WWidget getValueWidget(ParticleType<?> value) {
        return theme.label(Names.get(value));
    }

    @Override
    protected String[] getValueNames(ParticleType<?> value) {
        return new String[]{
            Names.get(value),
            Registries.PARTICLE_TYPE.getId(value).toString()
        };
    }
}
