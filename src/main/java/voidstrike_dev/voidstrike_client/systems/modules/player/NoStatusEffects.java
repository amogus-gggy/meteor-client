/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package voidstrike_dev.voidstrike_client.systems.modules.player;

import voidstrike_dev.voidstrike_client.settings.*;
import voidstrike_dev.voidstrike_client.settings.Setting;
import voidstrike_dev.voidstrike_client.settings.SettingGroup;
import voidstrike_dev.voidstrike_client.settings.StatusEffectListSetting;
import voidstrike_dev.voidstrike_client.systems.modules.Categories;
import voidstrike_dev.voidstrike_client.systems.modules.Module;
import net.minecraft.entity.effect.StatusEffect;

import java.util.List;

import static net.minecraft.entity.effect.StatusEffects.*;

public class NoStatusEffects extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<List<StatusEffect>> blockedEffects = sgGeneral.add(new StatusEffectListSetting.Builder()
        .name("blocked-effects")
        .description("Effects to block.")
        .defaultValue(
            LEVITATION.value(),
            JUMP_BOOST.value(),
            SLOW_FALLING.value(),
            DOLPHINS_GRACE.value()
        )
        .build()
    );

    public NoStatusEffects() {
        super(Categories.Player, "no-status-effects", "Blocks specified status effects.");
    }

    public boolean shouldBlock(StatusEffect effect) {
        return isActive() && blockedEffects.get().contains(effect);
    }
}
