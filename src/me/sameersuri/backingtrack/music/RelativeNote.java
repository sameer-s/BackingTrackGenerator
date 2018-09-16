package me.sameersuri.backingtrack.music;

public enum RelativeNote {
    ROOT,
    MINOR_SECOND,
    MAJOR_SECOND,
    MINOR_THIRD,
    MAJOR_THIRD,
    PERFECT_FOURTH,
    TRITONE,
    PERFECT_FIFTH,
    MINOR_SIXTH,
    MAJOR_SIXTH,
    MINOR_SEVENTH,
    MAJOR_SEVENTH,
    OCTAVE;

    public int getMidiValue() {
        return ordinal();
    }
}
