package me.sameersuri.backingtrack;

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
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import static me.sameersuri.backingtrack.music.NoteName.*;

public class BackingTrackPlayer {
    private static final double BPM = 150; // Tempo; beats per minute

    private static final long oneBeat = Math.round(1 / ((BPM / 60.0) / 1000.0)); // Tempo; milliseconds per oneBeat
    private static final long oneBar = oneBeat * 4;

    private Map<MidiChannel, Map<Integer, Integer>> currentNotes = new HashMap<>();
    private SortedMap<Long, List<MidiEvent>> events = new TreeMap<>();

    public static void main(String[] args) {
        try {
            BarPattern simple = new BarPattern(
                    ChordType.DOMINANT7,
                    new Rhythm(1, 1, 64),
                    new Rhythm(2, 1.5, 64),
                    new Rhythm(3.5, 1.5, 64)
            );
            NoteName[] notes = new NoteName[]{A, A, A, A, D, D, A, A, E, D, A, A};
            Bar[] bars = Arrays.stream(notes).map(note -> new Bar(note, simple)).toArray(Bar[]::new);
            BackingTrackPlayer player = new BackingTrackPlayer();
            player.playBackingTrack(new Song(bars));

        } catch (MidiUnavailableException e) {
            throw new RuntimeException(e);
        }
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

//        drumChannel.setSolo(true);

        long currentTime = 0;

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
            }

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
                    addMidiEvent(currentTime + startDelay, true, pianoChannel, note.getMidiValue(), chord.getRhythm().getVelocity());
                    addMidiEvent(currentTime + startDelay + length, false, pianoChannel, note.getMidiValue(), chord.getRhythm().getVelocity());
                }

                // Bass
                int bassNote = chord.getRoot().getMidiValue() - 24;
                addMidiEvent(currentTime + startDelay, true, bassChannel, bassNote, 100);
                addMidiEvent(currentTime + startDelay + oneBeat, false, bassChannel, bassNote, 16);
            }

            currentTime += oneBar;
        }

        long startTime = System.currentTimeMillis();
        while (!events.isEmpty()) {
            long first = events.firstKey();

            // noinspection StatementWithEmptyBody
            while(System.currentTimeMillis() < startTime + first){}

            System.out.println("Event! " + first);

            for(MidiEvent event : events.get(first)) {
                if(event.isOnEvent()) {
                    event.getChannel().noteOn(event.getMidiNote(), event.getVelocity());

                    Map<Integer, Integer> noteMap = currentNotes.getOrDefault(event.getChannel(), new HashMap<>());
                    int currentNoteCount = noteMap.getOrDefault(event.getMidiNote(), 0);
                    noteMap.put(event.getMidiNote(), currentNoteCount + 1);
                    currentNotes.put(event.getChannel(), noteMap);
                }
                else {
                    Map<Integer, Integer> noteMap = currentNotes.get(event.getChannel());
                    int currentNoteCount = noteMap.getOrDefault(event.getMidiNote(), 0);
                    noteMap.put(event.getMidiNote(), currentNoteCount - 1);
                    currentNotes.put(event.getChannel(), noteMap);

                    if(currentNoteCount == 1) {
                        event.getChannel().noteOff(event.getMidiNote(), event.getVelocity());
                    }
                }
            }

            events.remove(first);
        }

        synthesizer.close();
    }

    private void addMidiEvent(long delay, boolean isOnEvent, MidiChannel channel, int midiNote, int velocity) {
        List<MidiEvent> list = events.getOrDefault(delay, new LinkedList<>());
        list.add(new MidiEvent(isOnEvent, channel, midiNote, velocity));
        events.put(delay, list);
    }
}
