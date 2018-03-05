

package com.praveen.pilani.workout.util;

/**
 * Generic interface for a function.
 */
public interface Function<S, T> {
    T apply(S source);
}
