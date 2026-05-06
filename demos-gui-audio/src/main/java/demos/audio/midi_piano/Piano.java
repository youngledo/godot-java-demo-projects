package demos.audio.midi_piano;

import org.godot.annotation.GodotClass;
import org.godot.node.Control;
import org.godot.node.Node;

/**
 * Piano demo - generates a piano keyboard and responds to MIDI input.
 * A standard 88-key piano spans keys from 21 to 108.
 */
@GodotClass(name = "Piano", parent = "Control")
public class Piano extends Control {

    private static final int START_KEY = 21;
    private static final int END_KEY = 108;

    private org.godot.node.Node whiteKeys;
    private org.godot.node.Node blackKeys;

    // Maps pitch index to PianoKey node.
    private final java.util.Map<Integer, org.godot.Godot> pianoKeyDict = new java.util.HashMap<>();

    private boolean initialized = false;

    @Override
    public void _ready() {
        if (initialized) return;
        initialized = true;

        whiteKeys = getNode("WhiteKeys");
        blackKeys = getNode("BlackKeys");

        // Validate start key isn't a sharp note.
        assert !isNoteIndexSharp(pitchIndexToNoteIndex(START_KEY))
                : "The start key can't be a sharp note. Try 21.";

        for (int i = START_KEY; i <= END_KEY; i++) {
            pianoKeyDict.put(i, createPianoKey(i));
        }

        int whiteCount = whiteKeys != null ? ((Number) whiteKeys.getChildCount()).intValue() : 0;
        int blackCount = blackKeys != null ? ((Number) blackKeys.getChildCount()).intValue() : 0;
        if (whiteCount != blackCount) {
            addPlaceholderKey(blackKeys);
        }

        org.godot.singleton.OS.singleton().call("open_midi_inputs");

        org.godot.collection.GodotArray midiInputs = (org.godot.collection.GodotArray) org.godot.singleton.OS.singleton().call("get_connected_midi_inputs");
        if (midiInputs.size() > 0) {
            for (int i = 0; i < midiInputs.size(); i++) {
                System.out.println(midiInputs.get(i));
            }
        }
    }

    @Override
    public void _exitTree() {
        org.godot.singleton.OS.singleton().call("close_midi_inputs");
        // Free dynamically created piano key nodes
        if (whiteKeys != null) {
            int childCount = ((Number) whiteKeys.getChildCount()).intValue();
            for (int i = childCount - 1; i >= 0; i--) {
                org.godot.node.Node child = (org.godot.node.Node) whiteKeys.getChild(i);
                if (child != null) child.queueFree();
            }
        }
        if (blackKeys != null) {
            int childCount = ((Number) blackKeys.getChildCount()).intValue();
            for (int i = childCount - 1; i >= 0; i--) {
                org.godot.node.Node child = (org.godot.node.Node) blackKeys.getChild(i);
                if (child != null) child.queueFree();
            }
        }
        pianoKeyDict.clear();
        whiteKeys = null;
        blackKeys = null;
    }

    @Override
    public boolean _input(Object inputEvent) {
        if (!(inputEvent instanceof org.godot.Godot)) return false;
        org.godot.node.Node event = (org.godot.node.Node) inputEvent;

        String className = (String) event.call("get_class");
        if (!"InputEventMIDI".equals(className)) {
            return false;
        }

        int pitch = ((Number) event.getProperty("pitch")).intValue();
        if (pitch < START_KEY || pitch > END_KEY) {
            return false;
        }

        printMidiInfo(event);

        org.godot.Godot key = pianoKeyDict.get(pitch);
        if (key == null) return false;

        int message = ((Number) event.getProperty("message")).intValue();
        if (message == 7) { // MIDI_MESSAGE_NOTE_ON = 7
            key.call("activate");
        } else {
            key.call("deactivate");
        }
        return false;
    }

    private void addPlaceholderKey(org.godot.node.Node container) {
        if (container == null) return;
        org.godot.Godot placeholder = Control.create();
        placeholder.setProperty("size_flags_horizontal", 3L); // SIZE_EXPAND_FILL
        placeholder.setProperty("mouse_filter", 2L); // MOUSE_FILTER_IGNORE
        placeholder.setProperty("name", "Placeholder");
        container.addChild((org.godot.node.Node) placeholder);
    }

    private org.godot.Godot createPianoKey(int pitchIndex) {
        int noteIndex = pitchIndexToNoteIndex(pitchIndex);
        org.godot.Godot pianoKey;

        if (isNoteIndexSharp(noteIndex)) {
            // Black key
            org.godot.node.PackedScene blackKeyScene = (org.godot.node.PackedScene) org.godot.singleton.ResourceLoader.singleton().load("res://piano_keys/black_piano_key.tscn", "", 1);
            pianoKey = blackKeyScene.instantiate();
            if (blackKeys != null) blackKeys.addChild((org.godot.node.Node) pianoKey);
        } else {
            // White key
            org.godot.node.PackedScene whiteKeyScene = (org.godot.node.PackedScene) org.godot.singleton.ResourceLoader.singleton().load("res://piano_keys/white_piano_key.tscn", "", 1);
            pianoKey = whiteKeyScene.instantiate();
            if (whiteKeys != null) whiteKeys.addChild((org.godot.node.Node) pianoKey);
            if (isNoteIndexLackingSharp(noteIndex)) {
                addPlaceholderKey(blackKeys);
            }
        }
        pianoKey.call("setup", pitchIndex);
        return pianoKey;
    }

    private boolean isNoteIndexLackingSharp(int noteIndex) {
        // B and E, because no B# or E#
        return noteIndex == 2 || noteIndex == 7;
    }

    private boolean isNoteIndexSharp(int noteIndex) {
        // A#, C#, D#, F#, and G#
        return noteIndex == 1 || noteIndex == 4 || noteIndex == 6 || noteIndex == 9 || noteIndex == 11;
    }

    private int pitchIndexToNoteIndex(int pitch) {
        pitch += 3;
        return pitch % 12;
    }

    private void printMidiInfo(org.godot.Godot midiEvent) {
        System.out.println(midiEvent);
        System.out.println("Channel: " + midiEvent.getProperty("channel"));
        System.out.println("Message: " + midiEvent.getProperty("message"));
        System.out.println("Pitch: " + midiEvent.getProperty("pitch"));
        System.out.println("Velocity: " + midiEvent.getProperty("velocity"));
        System.out.println("Instrument: " + midiEvent.getProperty("instrument"));
        System.out.println("Pressure: " + midiEvent.getProperty("pressure"));
        System.out.println("Controller number: " + midiEvent.getProperty("controller_number"));
        System.out.println("Controller value: " + midiEvent.getProperty("controller_value"));
    }
}
