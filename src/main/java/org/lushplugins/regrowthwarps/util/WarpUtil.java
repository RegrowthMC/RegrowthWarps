package org.lushplugins.regrowthwarps.util;

import org.lushplugins.lushlib.utils.StringUtils;
import org.lushplugins.regrowthwarps.warp.Warp;

public class WarpUtil {

    public static String parsePlaceholders(Warp warp, String string) {
        return string
            .replace("%warp_name%", warp.name())
            .replace("%warp_display_name%", warp.displayName())
            .replace("%warp_description%", warp.description() != null ? warp.description() : "<i>No Description</i>")
            .replace("%warp_location%", Locations.serializeFriendly(warp.location()))
            .replace("%warp_visibility%", StringUtils.makeFriendly(warp.visibility().name()));
    }
}
