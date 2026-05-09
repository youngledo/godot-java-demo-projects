package demos.audio.midi_piano;

import java.util.HashMap;
import java.util.Map;
import org.godot.annotation.GodotClass;
import org.godot.math.Vector2;
import org.godot.node.Control;
import org.godot.node.InputEventMIDI;
import org.godot.node.Node;
import org.godot.node.PackedScene;
import org.godot.singleton.OS;
import org.godot.singleton.ResourceLoader;

@GodotClass(name = "Piano", parent = "Control")
public class Piano extends Control {

    private static final int START_KEY = 21;
    private static final int END_KEY = 108;

    private Node whiteKeys;
    private Node blackKeys;

    private final Map<Integer, PianoKey> pianoKeyDict = new HashMap<>();

    private boolean initialized = false;

    @Override
    public void _ready() {
        if (initialized) return;
        initialized = true;

        whiteKeys = getNode("WhiteKeys");
        blackKeys = getNode("BlackKeys");

        assert !isNoteIndexSharp(pitchIndexToNoteIndex(START_KEY)) : "The start key can't be a sharp note. Try 21.";

        for (int i = START_KEY; i <= END_KEY; i++) {
            pianoKeyDict.put(i, createPianoKey(i));
        }

        int whiteCount = whiteKeys != null ? whiteKeys.getChildCount() : 0;
        int blackCount = blackKeys != null ? blackKeys.getChildCount() : 0;
        if (whiteCount != blackCount) {
            addPlaceholderKey(blackKeys);
        }

        OS.singleton().openMidiInputs();
        for (String midiInput : OS.singleton().getConnectedMidiInputs()) {
            System.out.println(midiInput);
        }
    }

    @Override
    public void _exitTree() {
        OS.singleton().closeMidiInputs();
        freeChildren(whiteKeys);
        freeChildren(blackKeys);
        pianoKeyDict.clear();
        whiteKeys = null;
        blackKeys = null;
    }

    @Override
    public boolean _input(Object inputEvent) {
        if (!(inputEvent instanceof InputEventMIDI event)) return false;

        int pitch = (int) event.getPitch();
        if (pitch < START_KEY || pitch > END_KEY) {
            return false;
        }

        printMidiInfo(event);

        PianoKey key = pianoKeyDict.get(pitch);
        if (key == null) return false;

        if (event.getMessage() == 7) {
            key.activate();
        } else {
            key.deactivate();
        }
        return false;
    }

    private void addPlaceholderKey(Node container) {
        if (container == null) return;
        Control placeholder = Control.create();
        placeholder.setSizeFlagsHorizontal(3);
        placeholder.setMouseFilter(2);
        placeholder.setName("Placeholder");
        container.addChild(placeholder);
    }

    private PianoKey createPianoKey(int pitchIndex) {
        int noteIndex = pitchIndexToNoteIndex(pitchIndex);
        String scenePath = isNoteIndexSharp(noteIndex)
                ? "res://piano_keys/black_piano_key.tscn"
                : "res://piano_keys/white_piano_key.tscn";
        Node container = isNoteIndexSharp(noteIndex) ? blackKeys : whiteKeys;

        PianoKey pianoKey = null;
        if (ResourceLoader.singleton().load(scenePath, "", 1) instanceof PackedScene packedScene
                && packedScene.instantiate() instanceof PianoKey key) {
            pianoKey = key;
            if (container != null) container.addChild(pianoKey);
        }

        if (!isNoteIndexSharp(noteIndex) && isNoteIndexLackingSharp(noteIndex)) {
            addPlaceholderKey(blackKeys);
        }

        if (pianoKey != null) {
            pianoKey.setup(pitchIndex);
        }
        return pianoKey;
    }

    private void freeChildren(Node node) {
        if (node == null) return;
        int childCount = node.getChildCount();
        for (int i = childCount - 1; i >= 0; i--) {
            Node child = node.getChild(i);
            if (child != null) child.queueFree();
        }
    }

    private boolean isNoteIndexLackingSharp(int noteIndex) {
        return noteIndex == 2 || noteIndex == 7;
    }

    private boolean isNoteIndexSharp(int noteIndex) {
        return noteIndex == 1 || noteIndex == 4 || noteIndex == 6 || noteIndex == 9 || noteIndex == 11;
    }

    private int pitchIndexToNoteIndex(int pitch) {
        pitch += 3;
        return pitch % 12;
    }

    private void printMidiInfo(InputEventMIDI midiEvent) {
        System.out.println(midiEvent);
        System.out.println("Channel: " + midiEvent.getChannel());
        System.out.println("Message: " + midiEvent.getMessage());
        System.out.println("Pitch: " + midiEvent.getPitch());
        System.out.println("Velocity: " + midiEvent.getVelocity());
        System.out.println("Instrument: " + midiEvent.getInstrument());
        System.out.println("Pressure: " + midiEvent.getPressure());
        System.out.println("Controller number: " + midiEvent.getControllerNumber());
        System.out.println("Controller value: " + midiEvent.getControllerValue());
    }
}
