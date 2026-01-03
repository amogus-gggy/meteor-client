/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package voidstrike_dev.voidstrike_client.systems.waypoints.events;

import voidstrike_dev.voidstrike_client.systems.waypoints.Waypoint;

public record WaypointRemovedEvent(Waypoint waypoint) {
}
