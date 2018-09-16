package me.sameersuri.backingtrack;

import me.sameersuri.backingtrack.automata.AutomatonGrid;
import me.sameersuri.backingtrack.midi.MidiEvent;
import me.sameersuri.backingtrack.music.Bar;
import me.sameersuri.backingtrack.music.BarPattern;
import me.sameersuri.backingtrack.music.Chord;
import me.sameersuri.backingtrack.music.ChordType;
import me.sameersuri.backingtrack.music.Note;
import me.sameersuri.backingtrack.music.NoteName;
import me.sameersuri.backingtrack.music.Rhythm;
import me.sameersuri.backingtrack.music.Song;

import javax.sound.midi.MidiChannel;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.MidiUnavailableException;
import javax.sound.midi.Synthesizer;
import java.awt.image.AreaAveragingScaleFilter;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import static me.sameersuri.backingtrack.music.NoteName.*;

public class AutomatonBackingTrackPlayer {
    private static final double BPM = 150; // Tempo; beats per minute

    private static final long oneBeat = Math.round(1 / ((BPM / 60.0) / 1000.0)); // Tempo; milliseconds per beat
    private static final long oneBar = oneBeat * 4;

    private Map<MidiChannel, Map<Integer, Integer>> currentNotes = new HashMap<>();
    private SortedMap<Long, List<MidiEvent>> events = new TreeMap<>();

    private static final Comparator<MidiEvent> offEventsFirst = (o1, o2) -> {
        if (o1.isOnEvent()) {
            if (o2.isOnEvent()) {
                return 0;
            } else {
                return -1;
            }
        } else {
            if (o2.isOnEvent()) {
                return 1;
            } else {
                return 0;
            }
        }
    };

    public static void main(String[] args) {
        try {
            AutomatonGrid grid = new AutomatonGrid(32, false, 89);
            /*
                77 -> good but eventually devolves into beat
                    beginning good for piano
                    79 similar
                78 -> starts off slow, slowly grows into beats
                    more investigation needed
                    could be good for bass
                86 -> starts simple, grows to more complex
                    GOOD (esp. piano)
                    89 similar
             */

            for (int i = 0; i < 50; i++) {
                grid.iterate();
            }

            NoteName[] baseNotes = new NoteName[]{A, A, A, A, D, D, A, A, E, D, A, A};
            int iterations = 3;
            NoteName[] notes = new NoteName[baseNotes.length * iterations];
            for (int i = 0; i < iterations; i++) {
                System.arraycopy(baseNotes, 0, notes, i * baseNotes.length, baseNotes.length);
            }
            Bar[] bars = Arrays.stream(notes).map(note -> new Bar(note, getBarPattern(grid.iterate()))).toArray(Bar[]::new);
            AutomatonBackingTrackPlayer player = new AutomatonBackingTrackPlayer();
            player.playBackingTrack(new Song(bars));

        } catch (MidiUnavailableException e) {
            throw new RuntimeException(e);
        }
    }


    private static BarPattern getBarPattern(AutomatonGrid grid) {
        int[] rhythmArr = grid.getGrid();
        List<Rhythm> rhythms = new LinkedList<>();
        for (int i = 0; i < rhythmArr.length; i += 4) {
            int val = rhythmArr[i] + rhythmArr[i + 1] + rhythmArr[i + 2] + rhythmArr[i + 3];

            boolean valid;

            if (i % 16 == 0) {
                valid = val > 0;
            } else if (i % 8 == 0) {
                valid = val > 1;
            } else {
                valid = val > 2;
            }

            if (valid) {
                rhythms.add(new Rhythm((i / 8.0), 1 / 2., 35));
            }

            System.out.print(valid ? "x" : "-");
        }

        System.out.println();

        int chordNum = (int) (Math.random() * 3);
        ChordType chord = null;
        if (chordNum == 0) {
            chord = ChordType.DOMINANT7;
        }
        if (chordNum == 1) {
            chord = ChordType.MAJOR;
        }
        if (chordNum == 2) {
            chord = ChordType.MAJOR7;
        }

//        chord = ChordType.DOMINANT7;
        return new BarPattern(chord, rhythms.toArray(new Rhythm[0]));
    }

    public void playBackingTrack(Song song) throws MidiUnavailableException {
        Synthesizer synthesizer = MidiSystem.getSynthesizer();
        synthesizer.open();

        MidiChannel[] channels = synthesizer.getChannels();
        MidiChannel pianoChannel = channels[0];
        MidiChannel bassChannel = channels[1];
        bassChannel.programChange(34);
        MidiChannel organChannel = channels[2];
        organChannel.programChange(20);
        MidiChannel drumChannel = channels[9];

        long currentTime = 0;

        pianoChannel.setMute(true);
        bassChannel.setMute(true);

        for (int i = 0; i < 4; i++) {
            addMidiEvent(currentTime + (i * oneBeat), true, drumChannel, 77, 64);
            addMidiEvent(currentTime + (i * oneBeat) + 60, false, drumChannel, 77, 16);
        }

        currentTime += oneBar;

        for (Bar bar : song) {
            // Metronome/drum
            for (int i = 0; i < 4; i++) {
                addMidiEvent(currentTime + (i * oneBeat), true, drumChannel, 77, 64);
                addMidiEvent(currentTime + (i * oneBeat) + 60, false, drumChannel, 77, 16);

                if (currentTime <= 25 * oneBar) {
                    int bassNote = bar.getRoot().getMidiValue() - 24;
                    addMidiEvent(currentTime + (i * oneBeat), true, bassChannel, bassNote, 60);
                    addMidiEvent(currentTime + (i * oneBeat) + oneBeat, false, bassChannel, bassNote, 16);

                    addMidiEvent((long) (currentTime + ((i + 0.5) * oneBeat)), true, bassChannel, bassNote, 60);
                    addMidiEvent((long) (currentTime + ((i + 0.5) * oneBeat)) + oneBeat, false, bassChannel, bassNote, 16);
                } else if (i % 2 == 0) {
                    int bassNote = bar.getRoot().getMidiValue() - 24;
                    addMidiEvent(currentTime + (i * oneBeat), true, bassChannel, bassNote, 60);
                    addMidiEvent(currentTime + (i * oneBeat) + oneBeat, false, bassChannel, bassNote, 16);
                }
            }

//            int bassNote = bar.getRoot().getMidiValue() - 24;
//            addMidiEvent(currentTime, true, bassChannel, bassNote, 60);
//            addMidiEvent(currentTime+ oneBeat, false, bassChannel, bassNote, 16);

            // Organ
            int rootNote = bar.getRoot().getMidiValue();
            int[] organNotes = new int[]{rootNote, rootNote - 5, rootNote - 12};
            for (int organNote : organNotes) {
                addMidiEvent(currentTime, true, organChannel, organNote, 35);
                addMidiEvent(currentTime + oneBar, false, organChannel, organNote, 16);
            }

            for (Chord chord : bar) {
                long startDelay = (long) ((chord.getRhythm().getBeat() - 1) * oneBeat);
                long length = ((long) ((chord.getRhythm().getDuration()) * oneBeat));

                // Piano
                for (Note note : chord) {
                    addMidiEvent(currentTime + startDelay, true, pianoChannel, note.getMidiValue() - 12, chord.getRhythm().getVelocity());
                    addMidiEvent(currentTime + startDelay + length, false, pianoChannel, note.getMidiValue() - 12, chord.getRhythm().getVelocity());
                }

                // Bass
//                int bassNote = chord.getRoot().getMidiValue() - 24;
//                addMidiEvent(currentTime + startDelay, true, bassChannel, bassNote, 100);
//                addMidiEvent(currentTime + startDelay + oneBeat, false, bassChannel, bassNote, 16);
            }

            currentTime += oneBar;
        }

        for(List<MidiEvent> eventList : events.values()) {
            eventList.sort(offEventsFirst.reversed());
        }

        long startTime = System.currentTimeMillis();
        while (!events.isEmpty()) {
            long key = events.firstKey();

            // noinspection StatementWithEmptyBody
            while (System.currentTimeMillis() < startTime + key) {
            }

            if (key > 13 * oneBar) {
                bassChannel.setMute(false);
            }
            if (key > 25 * oneBar) {
                pianoChannel.setMute(false);
            }

            for (MidiEvent event : events.get(key)) {
                if (event.isOnEvent()) {
                    event.getChannel().noteOn(event.getMidiNote(), event.getVelocity());

                    Map<Integer, Integer> noteMap = currentNotes.getOrDefault(event.getChannel(), new HashMap<>());
                    int currentNoteCount = noteMap.getOrDefault(event.getMidiNote(), 0);
                    noteMap.put(event.getMidiNote(), currentNoteCount + 1);
                    currentNotes.put(event.getChannel(), noteMap);
                } else {
                    Map<Integer, Integer> noteMap = currentNotes.get(event.getChannel());
                    int currentNoteCount = noteMap.getOrDefault(event.getMidiNote(), 0);
                    noteMap.put(event.getMidiNote(), currentNoteCount - 1);
                    currentNotes.put(event.getChannel(), noteMap);

                    if (currentNoteCount == 1) {
                        event.getChannel().noteOff(event.getMidiNote(), event.getVelocity());
                    }
                }
            }

            events.remove(key);
        }

        synthesizer.close();
    }

    private void addMidiEvent(long delay, boolean isOnEvent, MidiChannel channel, int midiNote, int velocity) {
        List<MidiEvent> list = events.getOrDefault(delay, new LinkedList<>());
        list.add(new MidiEvent(isOnEvent, channel, midiNote, velocity));
        events.put(delay, list);
    }
}
