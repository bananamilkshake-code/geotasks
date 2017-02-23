package me.jtalk.android.geotasks.util;


import com.google.common.base.Function;
import com.google.common.base.Objects;

import java.util.Collection;

public class ArrayHelper {

    public static <T> T[] arrayOf(Collection<T> source, Function<Integer, T[]> arraySupplier) {
        T[] result = arraySupplier.apply(source.size());
        source.toArray(result);
        return result;
    }

    public static <T> int indexOf(T[] array, T toFind) {
        for (int i = 0; i < array.length; i++) {
            if (Objects.equal(array[i], toFind)) {
                return i;
            }
        }
        return -1;
    }
}
