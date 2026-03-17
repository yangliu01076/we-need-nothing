package org.example.common.collect;

import java.util.ArrayList;
import java.util.Collections;

/**
 * @author duoyian
 * @date 2026/3/17
 */
public final class Lists {

    public static <E> ArrayList<E> newArrayList(E... elements) {;
        int capacity = computeArrayListCapacity(elements.length);
        ArrayList<E> list = new ArrayList<>(capacity);
        Collections.addAll(list, elements);
        return list;
    }

    static int computeArrayListCapacity(int arraySize) {
        return arraySize + (arraySize / 10) + 6;
    }
}
