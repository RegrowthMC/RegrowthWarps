package org.lushplugins.regrowthwarps.util;

import java.util.ArrayList;
import java.util.List;

public class StringUtil {

    /**
     * Splits a string into multiple strings of count + 10
     * @param string string to split
     * @param count amount to split by
     * @return List of split strings
     */
    public static List<String> splitByCount(String string, int count) {
        List<String> strings = new ArrayList<>();

        String copy = string;
        while (copy.length() > count) {
            int lastIndex = copy.lastIndexOf(" ", count);
            int index = lastIndex >= 0 ? Math.min(lastIndex, count + 10) : count + 10;
            strings.add(copy.substring(0, index).strip());
            copy = copy.substring(index);
        }

        if (!copy.isBlank()) {
            strings.add(copy.strip());
        }

        return strings;
    }
}
