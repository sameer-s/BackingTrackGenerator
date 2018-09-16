package me.sameersuri.backingtrack.midi;

import javax.sound.midi.MidiChannel;

public class MidiEvent {
    private boolean isOnEvent;
    private MidiChannel channel;
    private int midiNote;
    private int velocity;

    public MidiEvent(boolean isOnEvent, MidiChannel channel, int midiNote, int velocity) {
        this.isOnEvent = isOnEvent;
        this.channel = channel;
        this.midiNote = midiNote;
        this.velocity = velocity;
    }

    public boolean isOnEvent() {
        return isOnEvent;
    }

    public MidiChannel getChannel() {
        return channel;
    }

    public int getMidiNote() {
        return midiNote;
    }

    public int getVelocity() {
        return velocity;
    }
}
