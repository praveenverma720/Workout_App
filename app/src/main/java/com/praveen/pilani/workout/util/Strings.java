

package com.praveen.pilani.workout.util;

import java.util.Iterator;

/**
 * Helper methods for {@link java.lang.String}
 */
public class Strings {
    private Strings() {
        // do not instantiate
    }

    public static String join(@SuppressWarnings("SameParameterValue") String separator, Iterable<String> parts) {
        Iterator<String> iterator = parts.iterator();
        if (!iterator.hasNext()) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        sb.append(iterator.next());
        while (iterator.hasNext()) {
            sb.append(separator).append(iterator.next());
        }
        return sb.toString();
    }
}
