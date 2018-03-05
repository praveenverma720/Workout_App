
package com.praveen.pilani.workout.util;

import android.support.annotation.NonNull;

import java.lang.reflect.Array;
import java.util.NoSuchElementException;

/**
 * Helper methods for arrays
 */
public class Arrays {
    public static <S, T> T[] map(S[] source, Class<T[]> targetClass, Function<S, T> transformation) {
        T[] target = targetClass.cast(Array.newInstance(targetClass.getComponentType(), source.length));
        for (int i = 0; i < source.length; i++) {
            target[i] = transformation.apply(source[i]);
        }
        return target;
    }

    public static <T> int firstIndexOf(@NonNull T[] array, @NonNull T item) {
        for (int i = 0; i < array.length; i++) {
            if (item.equals(array[i])) {
                return i;
            }
        }
        throw new NoSuchElementException();
    }
}
