package me.sameersuri.backingtrack.util;

import java.util.Iterator;

public class ArrayIterator<E> implements Iterator<E> {
    private E[] arr;
    private int i;
    public ArrayIterator(E[] arr) {
        this.arr = arr;
    }

    @Override
    public boolean hasNext() {
        return i < arr.length;
    }

    @Override
    public E next() {
        return arr[i++];
    }
}
