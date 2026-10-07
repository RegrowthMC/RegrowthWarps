package org.lushplugins.regrowthwarps.warp;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;
import org.jooq.Record;
import org.lushplugins.lushlib.item.DisplayItemStack;
import org.lushplugins.regrowthwarps.RegrowthWarps;
import org.lushplugins.regrowthwarps.storage.UsersTable;
import org.lushplugins.regrowthwarps.storage.WarpsTable;
import org.lushplugins.regrowthwarps.user.WarpUser;
import org.lushplugins.regrowthwarps.util.Locations;
import org.lushplugins.regrowthwarps.util.WarpUtil;
import org.lushplugins.regrowthwarps.util.jooq.BinaryUUIDBinding;
import org.lushplugins.regrowthwarps.util.jooq.StringDisplayItemBinding;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.function.Predicate;

public class Warp {
    private String name;
    private String displayName;
    private DisplayItemStack icon;
    private String description;
    private String category;
    private Location location;
    private UUID owner;
    private Visibility visibility;
    /**
     * Epoch time (in seconds) that the public name has been reserved since (only applicable for public warps that
     * have been made private)
     */
    private Long nameReservedSince;
    /**
     * Total number of visits to the warp, excluding the warp's owner
     */
    private int visits;
    /**
     * Epoch day that the warp was last visited by any player, excluding the warp's owner
     */
    private Long lastVisitedDay;

    public Warp(
        String name,
        String displayName,
        @Nullable DisplayItemStack icon,
        @Nullable String description,
        @Nullable String category,
        Location location,
        @Nullable UUID owner,
        Visibility visibility,
        @Nullable Long nameReservedSince,
        int visits,
        @Nullable Long lastVisitedDay
    ) {
        this.name = name;
        this.displayName = displayName;
        this.icon = icon;
        this.description = description;
        this.category = category;
        this.location = location;
        this.owner = owner;
        this.visibility = visibility;
        this.nameReservedSince = nameReservedSince;
        this.visits = visits;
        this.lastVisitedDay = lastVisitedDay;
    }

    public String name() {
        return name;
    }

    public String displayName() {
        return displayName;
    }

    /**
     * @return a future defining whether the name was changed. This can return false in instances where
     * a warp name is not available
     */
    public CompletableFuture<Boolean> displayName(String displayName) {
        String name = displayName.toLowerCase();
        if (Objects.equals(name, this.name)) {
            // If name hasn't changed then we only need to update the display name
            this.displayName = displayName;
            save();

            return CompletableFuture.completedFuture(true);
        }

        return WarpUtil.isWarpNameAvailable(name, this.owner).thenApply((available) -> {
            if (available) {
                // As the name has changed we delete the old warp
                // the database will then treat this object as a new warp entry
                delete();
                String oldName = this.name;
                this.displayName = displayName;
                this.name = name;
                save();

                if (visibility == Visibility.PUBLIC) {
                    PublicWarpCache warpCache = RegrowthWarps.getInstance().getPublicWarpCache();
                    warpCache.uncacheWarp(oldName);
                    warpCache.cacheWarp(this);
                }
            }

            return available;
        });
    }

    public @Nullable DisplayItemStack icon() {
        return icon;
    }

    public void icon(DisplayItemStack icon) {
        this.icon = icon;
        save();
    }

    public @Nullable String description() {
        return description;
    }

    public void description(String description) {
        this.description = description;
        save();
    }

    public String category() {
        return category;
    }

    public void category(String category) {
        this.category = category;
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

    /**
     * @return a future defining whether the visibility was changed. This can return false in instances where
     * a warp name is not available
     */
    public CompletableFuture<Boolean> visibility(Visibility visibility) {
        if (this.visibility == visibility) {
            return CompletableFuture.completedFuture(true);
        }

        Visibility oldVisibility = this.visibility;
        if (oldVisibility == Visibility.PUBLIC && visibility == Visibility.PRIVATE) {
            RegrowthWarps.getInstance().getPublicWarpCache().uncacheWarp(this.name);

            this.visibility = visibility;
            nameReservedSince(Instant.now().getEpochSecond());
            return CompletableFuture.completedFuture(true);
        } else if (oldVisibility == Visibility.PRIVATE && visibility == Visibility.PUBLIC) {
            return WarpUtil.isWarpNameAvailable(this.name, this.owner).thenApply((available) -> {
                if (available) {
                    this.visibility = visibility;
                    save();

                    RegrowthWarps.getInstance().getPublicWarpCache().cacheWarp(this);
                }

                return available;
            });
        }

        // This should never happen as all visibility change possibilities are covered above
        return CompletableFuture.completedFuture(false);
    }

    public boolean isNameReserved() {
        long nameReserveDuration = Duration.ofDays(RegrowthWarps.getInstance().getConfigManager().reserveNamesFor()).toSeconds();
        return nameReservedSince != null && nameReservedSince + nameReserveDuration > Instant.now().getEpochSecond();
    }

    public Long nameReservedSince() {
        return nameReservedSince;
    }

    public void nameReservedSince(Long nameReservedSince) {
        this.nameReservedSince = nameReservedSince;
        save();
    }

    public int visits() {
        return visits;
    }

    public Long lastVisitedDay() {
        return lastVisitedDay;
    }

    public void lastVisitedDay(Long lastVisitedDay) {
        this.lastVisitedDay = lastVisitedDay;
        save();
    }

    public void teleport(Player player) {
        player.teleportAsync(this.location);

        if (!player.getUniqueId().equals(this.owner)) {
            this.visits++;

            long epochDay = LocalDate.now().toEpochDay();
            if (epochDay != this.lastVisitedDay) {
                lastVisitedDay(epochDay);
            }
        }
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
        String rawIcon = StringDisplayItemBinding.to(icon);
        String rawLocation = Locations.serialize(location);

        RegrowthWarps.getInstance().getStorageHandler().execute(context -> context
            .insertInto(WarpsTable.TABLE)
            .set(WarpsTable.NAME, name)
            .set(WarpsTable.ICON, rawIcon)
            .set(WarpsTable.DISPLAY_NAME, displayName)
            .set(WarpsTable.DESCRIPTION, description)
            .set(WarpsTable.CATEGORY, category)
            .set(WarpsTable.LOCATION, rawLocation)
            .set(WarpsTable.OWNER_ID, owner != null ? UsersTable.getOrCreateUserId(context, owner) : -1)
            .set(WarpsTable.VISIBILITY, visibility.name())
            .set(WarpsTable.NAME_RESERVED_SINCE, nameReservedSince)
            .set(WarpsTable.VISITS, visits)
            .set(WarpsTable.LAST_VISITED_DAY, lastVisitedDay)
            .onConflict(WarpsTable.OWNER_ID, WarpsTable.NAME)
            .doUpdate()
            .set(WarpsTable.ICON, rawIcon)
            .set(WarpsTable.DISPLAY_NAME, displayName)
            .set(WarpsTable.DESCRIPTION, description)
            .set(WarpsTable.CATEGORY, category)
            .set(WarpsTable.LOCATION, rawLocation)
            .set(WarpsTable.VISIBILITY, visibility.name())
            .set(WarpsTable.NAME_RESERVED_SINCE, nameReservedSince)
            .set(WarpsTable.VISITS, visits)
            .set(WarpsTable.LAST_VISITED_DAY, lastVisitedDay)
            .execute()
        );
    }

    public void delete() {
        // We keep a copy of the parameters to ensure changes to the Warp object don't cause the incorrect
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

    public static Warp read(Record record) {
        String rawIcon = record.get(WarpsTable.ICON);
        byte[] rawUUID = record.get(UsersTable.UUID);
        return new Warp(
            record.get(WarpsTable.NAME),
            record.get(WarpsTable.DISPLAY_NAME),
            rawIcon != null ? StringDisplayItemBinding.from(rawIcon) : null,
            record.get(WarpsTable.DESCRIPTION),
            record.get(WarpsTable.CATEGORY),
            Locations.deserialize(record.get(WarpsTable.LOCATION)),
            rawUUID != null ? BinaryUUIDBinding.from(rawUUID) : null,
            Warp.Visibility.valueOf(record.get(WarpsTable.VISIBILITY)),
            record.get(WarpsTable.NAME_RESERVED_SINCE),
            record.get(WarpsTable.VISITS),
            record.get(WarpsTable.LAST_VISITED_DAY)
        );
    }

    public static Warp findOrRead(Record record) {
        String name = record.get(WarpsTable.NAME);
        if (record.get(WarpsTable.VISIBILITY).equals(Visibility.PUBLIC.name())) {
            Warp warp = RegrowthWarps.getInstance().getPublicWarpCache().getWarp(name);
            if (warp != null) {
                return warp;
            } else {
                Warp readWarp = Warp.read(record);
                readWarp.cache();
                return readWarp;
            }
        } else {
            byte[] rawUUID = record.get(UsersTable.UUID);
            UUID uuid = rawUUID != null ? BinaryUUIDBinding.from(rawUUID) : null;
            if (uuid != null) {
                WarpUser user = RegrowthWarps.getInstance().getUserCache().getCachedUser(uuid);
                if (user != null) {
                    return user.getWarp(name);
                }
            }
        }

        return Warp.read(record);
    }

    public enum Visibility {
        PUBLIC,
        PRIVATE
    }

    public enum SortingMethod {
        A_TO_Z("A to Z", Comparator.comparing(Warp::name)),
        Z_TO_A("Z to A", Comparator.comparing(Warp::name).reversed()),
        MOST_VISITS("Most Visits", Comparator.comparing(Warp::visits)),
        LEAST_VISITS("Least Visits", Comparator.comparing(Warp::visits).reversed());

        private final String friendlyName;
        private final Comparator<Warp> comparator;

        SortingMethod(String friendlyName, Comparator<Warp> comparator) {
            this.friendlyName = friendlyName;
            this.comparator = comparator;
        }

        public String friendlyName() {
            return friendlyName;
        }

        public Comparator<Warp> comparator() {
            return comparator;
        }
    }

    public interface Filter extends Predicate<Warp> {}
}
