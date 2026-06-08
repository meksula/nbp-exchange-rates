package com.meksula.nbp.utils;

public class CollectionUtils {

    public static <T> T firstItemOrThrow(Iterable<T> iterable) {
        if (iterable == null) {
            throw new IllegalArgumentException("Collection is null");
        }
        var iterator = iterable.iterator();
        if (!iterator.hasNext()) {
            throw new IllegalArgumentException("Collection is empty");
        }
        return iterator.next();
    }
}
