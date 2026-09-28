package org.lushplugins.regrowthwarps.warp;

import org.bukkit.Location;
import org.jetbrains.annotations.Nullable;
import org.lushplugins.lushlib.item.DisplayItemStack;
import org.lushplugins.regrowthwarps.RegrowthWarps;
import org.lushplugins.regrowthwarps.storage.UsersTable;
import org.lushplugins.regrowthwarps.storage.WarpsTable;
import org.lushplugins.regrowthwarps.user.WarpUser;
import org.lushplugins.regrowthwarps.util.Locations;

import java.util.Objects;
import java.util.UUID;

public class Warp {
    private String name;
    private DisplayItemStack icon;
    private String displayName;
    private String description;
    private Location location;
    private UUID owner;
    private Visibility visibility;
    /**
     * Epoch time that the public name is reserved until (only applicable for public warps that
     * have been made private)
     */
    private Long nameReservedUntil;
    /**
     * Epoch day that the warp was last visited by any player, except the warp's owner
     */
    private Long lastVisitedDay;

    public Warp(
        String name,
        @Nullable DisplayItemStack icon,
        String displayName,
        @Nullable String description,
        Location location,
        @Nullable UUID owner,
        Visibility visibility,
        @Nullable Long nameReservedUntil,
        @Nullable Long lastVisitedDay
    ) {
        this.name = name;
        this.icon = icon;
        this.displayName = displayName;
        this.description = description;
        this.location = location;
        this.owner = owner;
        this.visibility = visibility;
        this.nameReservedUntil = nameReservedUntil;
        this.lastVisitedDay = lastVisitedDay;
    }

    public String name() {
        return name;
    }

    public @Nullable DisplayItemStack icon() {
        return icon;
    }

    public void icon(DisplayItemStack icon) {
        this.icon = icon;
        save();
    }

    public String displayName() {
        return displayName;
    }

    public void displayName(String displayName) {
        this.displayName = displayName;

        String name = displayName.toLowerCase();
        if (Objects.equals(name, this.name)) {
            // If name hasn't changed then we just need to save the display name change
            save();
            return;
        }

        // As the name has changed we delete the old warp
        // the database will then treat this object as a new warp entry
        delete();
        String oldName = this.name;
        this.name = name;
        save();

        if (visibility == Visibility.PUBLIC) {
            PublicWarpCache warpCache = RegrowthWarps.getInstance().getPublicWarpCache();
            warpCache.uncacheWarp(oldName);
            warpCache.cacheWarp(this);
        }
    }

    public String description() {
        return description;
    }

    public void description(String description) {
        this.description = description;
        save();
    }

    public Location location() {
        return location;
    }

    public void location(Location location) {
        this.location = location;
        save();
    }

    public boolean isAdminWarp() {
        return owner == null;
    }

    public @Nullable UUID owner() {
        return owner;
    }

    public void owner(@Nullable UUID owner) {
        this.owner = owner;
        save();
    }

    public Visibility visibility() {
        return visibility;
    }

    public void visibility(Visibility visibility) {
        if (this.visibility == visibility) {
            return;
        }

        Visibility oldVisibility = this.visibility;
        this.visibility = visibility;

        if (oldVisibility == Visibility.PUBLIC && visibility == Visibility.PRIVATE) {
            RegrowthWarps.getInstance().getPublicWarpCache().uncacheWarp(this.name);
        } else if (oldVisibility == Visibility.PRIVATE && visibility == Visibility.PUBLIC) {
            RegrowthWarps.getInstance().getPublicWarpCache().cacheWarp(this);
        }

        save();
    }

    public boolean isNameReserved() {
        return nameReservedUntil != null && nameReservedUntil > System.currentTimeMillis();
    }

    public Long nameReservedUntil() {
        return nameReservedUntil;
    }

    public void nameReservedUntil(Long nameReservedUntil) {
        this.nameReservedUntil = nameReservedUntil;
        save();
    }

    public Long lastVisitedDay() {
        return lastVisitedDay;
    }

    public void lastVisitedDay(Long lastVisitedDay) {
        this.lastVisitedDay = lastVisitedDay;
        save();
    }

    public void cache() {
        if (this.visibility == Visibility.PUBLIC) {
            RegrowthWarps.getInstance().getPublicWarpCache().cacheWarp(this);
        }

        if (this.owner != null) {
            WarpUser user = RegrowthWarps.getInstance().getUserCache().getCachedUser(this.owner);
            if (user != null) {
                user.addWarp(this);
            }
        }
    }

    public void invalidateCache() {
        if (this.visibility == Visibility.PUBLIC) {
            RegrowthWarps.getInstance().getPublicWarpCache().uncacheWarp(this.name);
        }

        if (this.owner != null) {
            WarpUser user = RegrowthWarps.getInstance().getUserCache().getCachedUser(this.owner);
            if (user != null) {
                user.removeWarp(this.name);
            }
        }
    }

    public void save() {
        // TODO: Save icon
        RegrowthWarps.getInstance().getStorageHandler().execute(context -> context
            .insertInto(WarpsTable.TABLE)
            .set(WarpsTable.NAME, name)
            .set(WarpsTable.DISPLAY_NAME, displayName())
            .set(WarpsTable.DESCRIPTION, description)
            .set(WarpsTable.LOCATION, Locations.serialize(location))
            .set(WarpsTable.OWNER_ID, owner != null ? UsersTable.getOrCreateUserId(context, owner) : -1)
            .set(WarpsTable.VISIBILITY, visibility.name())
            .set(WarpsTable.NAME_RESERVED_UNTIL, nameReservedUntil)
            .set(WarpsTable.LAST_VISITED_DAY, lastVisitedDay)
            .onConflict(WarpsTable.OWNER_ID, WarpsTable.NAME)
            .doUpdate()
            .set(WarpsTable.DISPLAY_NAME, displayName)
            .set(WarpsTable.DESCRIPTION, description)
            .set(WarpsTable.LOCATION, Locations.serialize(location))
            .set(WarpsTable.VISIBILITY, visibility.name())
            .set(WarpsTable.NAME_RESERVED_UNTIL, nameReservedUntil)
            .set(WarpsTable.LAST_VISITED_DAY, lastVisitedDay)
            .execute()
        );
    }

    public void delete() {
        // We save a copy of each instance to ensure changes to the Warp object don't cause the incorrect
        // database row to be deleted
        String name = this.name;
        UUID owner = this.owner;
        RegrowthWarps.getInstance().getStorageHandler().execute(context -> context
            .deleteFrom(WarpsTable.TABLE)
            .where(WarpsTable.OWNER_ID.eq(owner != null ? UsersTable.getOrCreateUserId(context, owner): -1))
            .and(WarpsTable.NAME.eq(name))
            .execute()
        );
    }

    public enum Visibility {
        PUBLIC,
        PRIVATE
    }
}
