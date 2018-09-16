package me.sameersuri.backingtrack.music;

import me.sameersuri.backingtrack.util.ArrayIterator;

import java.util.Iterator;

public class Chord implements Iterable<Note>{
    private NoteName root;
    private ChordType type;
    private Note[] notes;
    private Rhythm rhythm;

    public Chord(ChordType type, NoteName root, Rhythm rhythm) {
        this.type = type;
        this.root = root;

        notes = new Note[type.getNumberOfNotes()];

        for(int i = 0; i < type.getNumberOfNotes(); i++) {
            RelativeNote relativeNote = type.getNote(i);
            notes[i] = new Note(root.getMidiValue() + relativeNote.getMidiValue());
        }

        this.rhythm = rhythm;
    }

    public NoteName getRoot() {
        return root;
    }

    public ChordType getType() {
        return type;
    }

    public int getNumberOfNotes() {
        return notes.length;
    }

    public Note getNote(int i) {
        return notes[i];
    }

    public Rhythm getRhythm() {
        return rhythm;
    }

    @Override
    public Iterator<Note> iterator() {
        return new ArrayIterator<>(notes);
    }
}
