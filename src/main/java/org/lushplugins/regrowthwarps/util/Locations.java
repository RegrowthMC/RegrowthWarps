package org.lushplugins.regrowthwarps.util;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

public class Locations {

    public static String serializeFriendly(Location location) {
        World world = location.getWorld();
        String string = "%s, %s, %s".formatted(
            location.getBlockX(),
            location.getBlockY(),
            location.getBlockZ()
        );

        if (world != null) {
            return string + " (%s)".formatted(world.getName());
        } else {
            return string;
        }
    }

    public static String serialize(Location location) {
        World world = location.getWorld();
        return "%s,%s,%s,%s,%s,%s".formatted(
            location.getX(),
            location.getY(),
            location.getZ(),
            location.getYaw(),
            location.getPitch(),
            world != null ? world.getName() : "world"
        );
    }

    public static Location deserialize(String rawLocation) {
        String[] content = rawLocation.split(",", 6);
        return new Location(
            Bukkit.getWorld(content[5]),
            Double.parseDouble(content[0]),
            Double.parseDouble(content[1]),
            Double.parseDouble(content[2]),
            Float.parseFloat(content[3]),
            Float.parseFloat(content[4])
        );
    }
}
