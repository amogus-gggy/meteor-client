/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package voidstrike_dev.voidstrike_client.systems.modules.combat;

import voidstrike_dev.voidstrike_client.events.world.TickEvent;
import voidstrike_dev.voidstrike_client.settings.BoolSetting;
import voidstrike_dev.voidstrike_client.settings.Setting;
import voidstrike_dev.voidstrike_client.settings.SettingGroup;
import voidstrike_dev.voidstrike_client.systems.modules.Categories;
import voidstrike_dev.voidstrike_client.systems.modules.Module;
import voidstrike_dev.voidstrike_client.utils.player.InvUtils;
import voidstrike_dev.voidstrike_client.utils.world.BlockUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.SlabBlock;

public class AntiAnchor extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<Boolean> rotate = sgGeneral.add(new BoolSetting.Builder()
        .name("rotate")
        .description("Makes you rotate when placing.")
        .defaultValue(true)
        .build()
    );

    private final Setting<Boolean> swing = sgGeneral.add(new BoolSetting.Builder()
        .name("swing")
        .description("Swings your hand when placing.")
        .defaultValue(true)
        .build()
    );

    public AntiAnchor() {
        super(Categories.Combat, "anti-anchor", "Automatically prevents Anchor Aura by placing a slab on your head.");
    }

    @EventHandler
    private void onTick(TickEvent.Pre event) {
        if (mc.world.getBlockState(mc.player.getBlockPos().up(2)).getBlock() == Blocks.RESPAWN_ANCHOR
            && mc.world.getBlockState(mc.player.getBlockPos().up()).getBlock() == Blocks.AIR) {

            BlockUtils.place(
                mc.player.getBlockPos().add(0, 1, 0),
                InvUtils.findInHotbar(itemStack -> Block.getBlockFromItem(itemStack.getItem()) instanceof SlabBlock),
                rotate.get(),
                15,
                swing.get(),
                false,
                true
            );
        }
    }
}
