/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package voidstrike_dev.voidstrike_client.systems.modules.misc;

import voidstrike_dev.voidstrike_client.systems.modules.Categories;
import voidstrike_dev.voidstrike_client.systems.modules.Module;

public class BetterBeacons extends Module {
    public BetterBeacons() {
        super(Categories.Misc, "better-beacons", "Select effects unaffected by beacon level.");
    }
}
