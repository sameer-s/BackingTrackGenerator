package me.sameersuri.backingtrack.music;

import me.sameersuri.backingtrack.util.ArrayIterator;

import java.util.Iterator;

public class Song implements Iterable<Bar> {
    private Bar[] bars;

    public Song(Bar... bars) {
        this.bars = bars;
    }

    public int getLengthInBars() {
        return bars.length;
    }

    public Bar getBar(int i) {
        return bars[i];
    }

    @Override
    public Iterator<Bar> iterator() {
        return new ArrayIterator<>(bars);
    }
}
