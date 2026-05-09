package demos.audio.midi_piano;

import org.godot.annotation.GodotClass;
import org.godot.annotation.GodotMethod;
import org.godot.core.Callable;
import org.godot.node.ColorRect;
import org.godot.node.InputEventMouseButton;
import org.godot.node.Node;

@GodotClass(name = "PianoKeyColor", parent = "ColorRect")
public class PianoKeyColor extends ColorRect {

    private boolean initialized = false;

    @Override
    public void _ready() {
        if (initialized) return;
        initialized = true;

        connect("gui_input", new Callable(this, "_onGuiInput"), 0);
    }

    @GodotMethod
    public void _onGuiInput(Object inputEvent) {
        if (inputEvent instanceof InputEventMouseButton event && event.isPressed()) {
            Node parent = getParent();
            if (parent instanceof PianoKey pianoKey) {
                pianoKey.activate();
            }
        }
    }
}
