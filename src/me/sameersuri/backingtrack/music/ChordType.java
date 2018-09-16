package me.sameersuri.backingtrack.music;

import me.sameersuri.backingtrack.util.ArrayIterator;

import java.util.Iterator;

import static me.sameersuri.backingtrack.music.RelativeNote.*;

public enum ChordType implements Iterable<RelativeNote> {
    MAJOR(ROOT, MAJOR_THIRD, PERFECT_FIFTH),
    MINOR(ROOT, MINOR_THIRD, PERFECT_FIFTH),
    DOMINANT7(ROOT, MAJOR_THIRD, PERFECT_FIFTH, MINOR_SEVENTH),
    MAJOR7(ROOT, MAJOR_THIRD, PERFECT_FIFTH, MAJOR_SEVENTH),
    MINOR7(ROOT, MINOR_THIRD, PERFECT_FIFTH, MINOR_SEVENTH);

    private RelativeNote[] notes;

    public int getNumberOfNotes() {
        return notes.length;
    }

    public RelativeNote getNote(int i) {
        return notes[i];
    }

    ChordType(RelativeNote... notes)
    {
        this.notes = notes;
    }

    @Override
    public Iterator<RelativeNote> iterator() {
        return new ArrayIterator<>(notes);
    }
}
