/*
 * This file is part of HuskTowns, licensed under the Apache License 2.0.
 *
 *  Copyright (c) William278 <will27528@gmail.com>
 *  Copyright (c) contributors
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package dev.xf3d3.ultimatereports.models;

import com.google.gson.annotations.Expose;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Represents a position in a claim world
 */
@SuppressWarnings("unused")
public class Position {

    @Getter @Setter
    @Expose
    private double x;

    @Getter @Setter
    @Expose
    private double y;

    @Getter @Setter
    @Expose
    private double z;

    @Getter @Setter
    @Expose
    private String world;

    @Getter @Setter
    @Expose
    private float yaw;

    @Getter @Setter
    @Expose
    private float pitch;

    @Getter @Setter
    @Expose @Nullable
    private String server;

    protected Position(double x, double y, double z, @NotNull String world, float yaw, float pitch,  @Nullable String server) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.world = world;
        this.yaw = yaw;
        this.pitch = pitch;
        this.server = server;
    }

    @SuppressWarnings("unused")
    private Position() {
    }

    @NotNull
    public static Position at(double x, double y, double z, @NotNull String world, float yaw, float pitch, @Nullable String server) {
        return new Position(x, y, z, world, yaw, pitch, server);
    }

    @NotNull
    public static Position at(Location location, @Nullable String server) {
        return new Position(location.getX(), location.getY(), location.getZ(), location.getWorld().getName(), location.getYaw(), location.getPitch(), server);
    }

    public Location getLocation() {
        return new Location(
                Bukkit.getWorld(this.world),
                this.x,
                this.y,
                this.z,
                this.yaw,
                this.pitch
        );
    }
}