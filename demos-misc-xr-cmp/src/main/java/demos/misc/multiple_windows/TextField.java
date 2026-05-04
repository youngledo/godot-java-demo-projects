package demos.misc.multiple_windows;

import org.godot.annotation.Export;
import org.godot.annotation.GodotClass;
import org.godot.annotation.GodotMethod;
import org.godot.node.LineEdit;

@GodotClass(name = "MWTextField", parent = "LineEdit")
public class TextField extends LineEdit {

    @Export
    public org.godot.Godot submitButton;

    private boolean initialized = false;

    @Override
    public void _ready() {
        if (initialized) return;
        initialized = true;

        connect("text_submitted",
            new org.godot.core.Callable(this, "_on_text_submitted"), 4096); // CONNECT_DEFERRED
        if (submitButton != null) {
            submitButton.connect("pressed",
                new org.godot.core.Callable(this, "_on_submit_pressed"), 0);
        }
    }

    @GodotMethod
    public void _on_text_submitted(String text) {
        call("clear");
    }

    @GodotMethod
    public void _on_submit_pressed() {
        String text = (String) getProperty("text");
        call("emit_signal", "text_submitted", text);
    }
}
