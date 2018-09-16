package me.sameersuri.backingtrack.music;

public enum NoteName {
    Ab, A, Bb, B, C, Db, D, Eb, E, F, Gb, G;

    public int getMidiValue() {
        return 56 + ordinal();
    }
}
