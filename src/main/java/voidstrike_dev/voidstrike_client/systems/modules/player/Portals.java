/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package voidstrike_dev.voidstrike_client.systems.modules.player;

import voidstrike_dev.voidstrike_client.systems.modules.Categories;
import voidstrike_dev.voidstrike_client.systems.modules.Module;

public class Portals extends Module {
    public Portals() {
        super(Categories.Player, "portals", "Allows you to use GUIs normally while in a Nether Portal.");
    }
}
