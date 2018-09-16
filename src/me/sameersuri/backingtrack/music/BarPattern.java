package me.sameersuri.backingtrack.music;

import java.util.Arrays;

public class BarPattern {
    private Rhythm[] rhythms;
    private ChordType[] chords;

    public BarPattern(ChordType[] chords, Rhythm[] rhythms) {
        if(chords.length != rhythms.length) {
            throw new IllegalStateException("must have equal number of chords and rhythms");
        }
        this.rhythms = rhythms;
        this.chords = chords;
    }

    public BarPattern(ChordType chord, Rhythm... rhythms) {
        ChordType[] chords = new ChordType[rhythms.length];
        Arrays.fill(chords, chord);

        this.rhythms = rhythms;
        this.chords = chords;
    }

    public int getChordCount() {
        return chords.length;
    }

    public Rhythm getChordRhythm(int i) {
        return rhythms[i];
    }

    public ChordType getChordType(int i) {
        return chords[i];
    }
}
