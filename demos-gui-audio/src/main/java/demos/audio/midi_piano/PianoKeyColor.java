package demos.audio.midi_piano;

import org.godot.Godot;
import org.godot.annotation.GodotClass;
import org.godot.annotation.GodotMethod;
import org.godot.node.ColorRect;

/**
 * PianoKeyColor - script attached to the ColorRect child of PianoKey.
 * Handles mouse click input on individual piano keys.
 */
@GodotClass(name = "PianoKeyColor", parent = "ColorRect")
public class PianoKeyColor extends ColorRect {

    private boolean initialized = false;

    @Override
    public void _ready() {
        if (initialized) return;
        initialized = true;

        connect("gui_input", new org.godot.core.Callable(this, "_onGuiInput"), 0);
    }

    @GodotMethod
    public void _onGuiInput(Object inputEvent) {
        Godot event = (Godot) inputEvent;
        String className = (String) event.call("get_class");
        if ("InputEventMouseButton".equals(className)) {
            boolean pressed = (boolean) event.getProperty("pressed");
            if (pressed) {
                Godot parent = (Godot) getParent();
                if (parent != null) {
                    parent.call("activate");
                }
            }
        }
    }
}
