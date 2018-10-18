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

public class SoloBackingTrackPlayer extends AutomatonBackingTrackPlayer{
    public static void main(String[] args) {
        try {
            AutomatonGrid pianoGrid = new AutomatonGrid(32, false, 89);
            AutomatonGrid bassGrid = new AutomatonGrid(32, false, 78);

            for (int i = 0; i < 50; i++) {
                pianoGrid.iterate();
            }

            NoteName[] baseNotes = new NoteName[] { A, A, A, A, D, D, A, A, E, D, A, A };
            int iterations = 3;
            NoteName[] notes = new NoteName[baseNotes.length * iterations];
            for (int i = 0; i < iterations; i++) {
                System.arraycopy(baseNotes, 0, notes, i * baseNotes.length, baseNotes.length);
            }
            SoloBackingTrackPlayer player = new SoloBackingTrackPlayer();
            Synthesizer synthesizer = MidiSystem.getSynthesizer();
            Song pianoSong = new Song(Arrays.stream(notes).map(note -> new Bar(note, getBarPattern(pianoGrid.iterate()))).toArray(Bar[]::new));
            Song bassSong = new Song(Arrays.stream(notes).map(note -> new Bar(note, getBarPattern(bassGrid.iterate()))).toArray(Bar[]::new));
            synthesizer.open();
            player.generatePianoTrack(synthesizer, pianoSong);
            player.generateBassTrack(synthesizer, bassSong);
            player.generateOrganTrack(synthesizer, pianoSong);
            player.generateDrumTrack(synthesizer, pianoSong);
            player.generateSoloTrack(synthesizer, pianoSong, baseNotes[0]);
            player.playBackingTrack();
            synthesizer.close();

        } catch (MidiUnavailableException e) {
            throw new RuntimeException(e);
        }
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
        double[] baseNoteProbabilities = new double[] { 4, 0, 0, 0, 3, 3, 0.2, 3, 0, 0, 2, 0, 4};
        double[] squaredNoteProbabilities = Arrays.stream(baseNoteProbabilities).map(p -> p * p).toArray();

        Function<double[], RelativeNote> generateNote = noteProbabilities -> {
            double totalNoteProbability = Arrays.stream(noteProbabilities).sum();
            double noteVal = Math.random() * totalNoteProbability;
            double sum = 0;
            for(int i = 0; i < noteProbabilities.length; i++) {
                sum += noteProbabilities[i];
                if(noteVal < sum) {
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
                }
                else if (val < eighthProbability + tripletProbability) {
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
                }
                else {
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
