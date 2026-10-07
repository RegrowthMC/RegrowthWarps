package org.lushplugins.regrowthwarps.util;

import java.lang.reflect.Array;
import java.util.List;
import java.util.Objects;

public class Lists {

    public static <T> T findAdjacentValue(T[] array, T value, boolean forward) {
        if (value == null) {
            return array[0];
        }

        for (int i = 0; i < array.length; i++) {
            T current = array[i];
            int offset = forward ? 1 : -1;
            int index = i + offset;
            if (index < 0) {
                index = array.length - 1;
            }

            if (Objects.equals(value, current)) {
                return array[index % array.length];
            }
        }

        return value;
    }

    public static <T> T findAdjacentValue(List<T> list, T value, boolean forward) {
        if (value == null) {
            return list.getFirst();
        }

        //noinspection unchecked
        T[] array = (T[]) Array.newInstance(value.getClass(), list.size());
        return findAdjacentValue(list.toArray(array), value, forward);
    }
}
