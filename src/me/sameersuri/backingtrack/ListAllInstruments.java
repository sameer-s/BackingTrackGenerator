package me.sameersuri.backingtrack;

import javax.sound.midi.Instrument;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.MidiUnavailableException;
import javax.sound.midi.Synthesizer;

public class ListAllInstruments {
    public static void main(String[] args) throws MidiUnavailableException {
        Synthesizer synthesizer = MidiSystem.getSynthesizer();
        synthesizer.open();

        Instrument[] instruments = synthesizer.getAvailableInstruments();

        for(int i = 0; i < instruments.length; i++) {
            System.out.println(i + " " + instruments[i]);
        }

        synthesizer.close();
    }
}
