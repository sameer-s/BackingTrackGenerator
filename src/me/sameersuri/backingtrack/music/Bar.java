package me.sameersuri.backingtrack.music;

import java.util.Iterator;

/**
 * A single-chord bar
 */
public class Bar implements Iterable<Chord> {
    private NoteName root;
    private BarPattern pattern;

    public Bar(NoteName root, BarPattern pattern) {
        this.root = root;
        this.pattern = pattern;
    }

    public NoteName getRoot() {
        return root;
    }

    public int getChordCount() {
        return pattern.getChordCount();
    }

    public Chord getChord(int i) {
        ChordType type = pattern.getChordType(i);
        Rhythm rhythm = pattern.getChordRhythm(i);
        return new Chord(type, root, rhythm);
    }

    @Override
    public Iterator<Chord> iterator() {
        return new Iterator<Chord>() {
            int i = 0;
            @Override
            public boolean hasNext() {
                return i < getChordCount();
            }

            @Override
            public Chord next() {
                return getChord(i++);
            }
        };
    }
}
