package me.sameersuri.backingtrack;

import me.sameersuri.backingtrack.automata.AutomatonGrid;
import me.sameersuri.backingtrack.midi.MidiEvent;
import me.sameersuri.backingtrack.music.Bar;
import me.sameersuri.backingtrack.music.BarPattern;
import me.sameersuri.backingtrack.music.Chord;
import me.sameersuri.backingtrack.music.ChordType;
import me.sameersuri.backingtrack.music.Note;
import me.sameersuri.backingtrack.music.NoteName;
import me.sameersuri.backingtrack.music.RelativeNote;
import me.sameersuri.backingtrack.music.Rhythm;
import me.sameersuri.backingtrack.music.Song;

import javax.sound.midi.MidiChannel;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.MidiUnavailableException;
import javax.sound.midi.Synthesizer;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.function.Function;
import java.util.function.Supplier;

import static me.sameersuri.backingtrack.music.NoteName.A;
import static me.sameersuri.backingtrack.music.NoteName.D;
import static me.sameersuri.backingtrack.music.NoteName.E;

public class SoloBackingTrackPlayer extends AutomatonBackingTrackPlayer {
    public static void main(String[] args) {
        try {
            Synthesizer synthesizer = MidiSystem.getSynthesizer();
            synthesizer.open();
            new SoloBackingTrackPlayer().generateTrack(synthesizer).playBackingTrack();
            synthesizer.close();
        }
        catch (MidiUnavailableException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public SoloBackingTrackPlayer generateTrack(Synthesizer synthesizer) {
        super.generateTrack(synthesizer);
        generateSoloTrack(synthesizer, pianoSong, notes[0]);

        return this;
    }

    public void generateSoloTrack(Synthesizer synthesizer, Song song, NoteName key) {
        MidiChannel[] channels = synthesizer.getChannels();
        MidiChannel soloChannel = channels[3];
        soloChannel.programChange(1);

        long currentTime = oneBar;

        // Subdivisions
        double eighthProbability = 2;
        double tripletProbability = 1;
        double noSubdivisionProbability = 3;
        double totalSubdivisionProbability = eighthProbability + tripletProbability + noSubdivisionProbability;

        // Probability of individual notes
        double[] baseNoteProbabilities = new double[]{4, 0, 0, 0, 3, 3, 0.2, 3, 0, 0, 2, 0, 4};
        double[] squaredNoteProbabilities = Arrays.stream(baseNoteProbabilities).map(p -> p * p).toArray();

        Function<double[], RelativeNote> generateNote = noteProbabilities -> {
            double totalNoteProbability = Arrays.stream(noteProbabilities).sum();
            double noteVal = Math.random() * totalNoteProbability;
            double sum = 0;
            for (int i = 0; i < noteProbabilities.length; i++) {
                sum += noteProbabilities[i];
                if (noteVal < sum) {
                    return RelativeNote.values()[i];
                }
            }

            return RelativeNote.OCTAVE;
        };
        for (Bar bar : song) {
            for (int i = 0; i < 4; i++) {
                double val = Math.random() * totalSubdivisionProbability;
                if (val < eighthProbability) {
                    // Subdivide into eights
                    Note firstNote = new Note(key.getMidiValue() + generateNote.apply(baseNoteProbabilities).getMidiValue());
                    Note secondNote = new Note(key.getMidiValue() + generateNote.apply(squaredNoteProbabilities).getMidiValue());

                    addMidiEvent(currentTime, true, soloChannel, firstNote.getMidiValue(), 64);
                    addMidiEvent(currentTime + oneBeat / 2, false, soloChannel, firstNote.getMidiValue(), 16);

                    addMidiEvent(currentTime + (oneBeat / 2), true, soloChannel, secondNote.getMidiValue(), 64);
                    addMidiEvent(currentTime + oneBeat, false, soloChannel, secondNote.getMidiValue(), 16);
                } else if (val < eighthProbability + tripletProbability) {
                    // Subdivide into triplets
                    Note firstNote = new Note(key.getMidiValue() + generateNote.apply(baseNoteProbabilities).getMidiValue());
                    Note secondNote = new Note(key.getMidiValue() + generateNote.apply(squaredNoteProbabilities).getMidiValue());
                    Note thirdNote = new Note(key.getMidiValue() + generateNote.apply(squaredNoteProbabilities).getMidiValue());

                    addMidiEvent(currentTime, true, soloChannel, firstNote.getMidiValue(), 64);
                    addMidiEvent(currentTime + oneBeat / 3, false, soloChannel, firstNote.getMidiValue(), 16);

                    addMidiEvent(currentTime + oneBeat / 3, true, soloChannel, secondNote.getMidiValue(), 64);
                    addMidiEvent(currentTime + 2 * oneBeat / 3, false, soloChannel, secondNote.getMidiValue(), 16);

                    addMidiEvent(currentTime + 2 * oneBeat / 3, true, soloChannel, thirdNote.getMidiValue(), 64);
                    addMidiEvent(currentTime + oneBeat, false, soloChannel, thirdNote.getMidiValue(), 16);
                } else {
                    // Do not divide beat
                    Note note = new Note(key.getMidiValue() + generateNote.apply(baseNoteProbabilities).getMidiValue());

                    addMidiEvent(currentTime, true, soloChannel, note.getMidiValue(), 64);
                    addMidiEvent(currentTime + oneBeat, false, soloChannel, note.getMidiValue(), 16);
                }

                currentTime += oneBeat;
            }
        }
    }
}
