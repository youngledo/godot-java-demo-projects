package demos.gui.input_mapping;

import org.godot.annotation.Export;
import org.godot.annotation.GodotClass;
import org.godot.annotation.GodotMethod;
import org.godot.node.Button;
import org.godot.math.Color;

@GodotClass(name = "ActionRemapButton", parent = "Button")
public class ActionRemapButton extends Button {

    @Export
    public String action = "ui_up";

    private boolean initialized = false;

    @Override
    public void _ready() {
        if (initialized) return;
        initialized = true;

        org.godot.singleton.InputMap inputMap = org.godot.singleton.InputMap.singleton();
        assert (boolean) inputMap.call("has_action", action);

        call("set_process_unhandled_key_input", false);
        displayCurrentKey();
    }

    @GodotMethod
    public void _toggled(boolean isButtonPressed) {
        call("set_process_unhandled_key_input", isButtonPressed);
        if (isButtonPressed) {
            setProperty("text", "<press a key>");
            setProperty("modulate", new Color(1, 1, 0));
            call("release_focus");
        } else {
            displayCurrentKey();
            setProperty("modulate", new Color(1, 1, 1));
            call("grab_focus");
        }
    }

    @Override
    public boolean _unhandledKeyInput(Object inputEvent) {
        if (inputEvent instanceof org.godot.Godot) {
            org.godot.Godot ev = (org.godot.Godot) inputEvent;
            String className = (String) ev.call("get_class");
            long keycode = ev.getProperty("keycode") != null ? (long) ev.getProperty("keycode") : 0;
            // KEY_ENTER = 4194305, skip it
            if ("InputEventKey".equals(className) && keycode != 4194305) {
                boolean pressed = ev.getProperty("pressed") != null && (boolean) ev.getProperty("pressed");
                if (pressed) {
                    remapActionTo(ev);
                    setProperty("button_pressed", false);
                }
            }
        }
        return false;
    }

    private void remapActionTo(Object inputEvent) {
        org.godot.singleton.InputMap inputMap = org.godot.singleton.InputMap.singleton();
        inputMap.call("action_erase_events", action);
        inputMap.call("action_add_event", action, inputEvent);

        // Save to keymaps via KeyPersistence autoload
        org.godot.Godot keyPersistence = (org.godot.Godot) call("get_node", "/root/KeyPersistence");
        if (keyPersistence != null) {
            org.godot.collection.GodotDictionary keymaps = (org.godot.collection.GodotDictionary) keyPersistence.getProperty("keymaps");
            if (keymaps != null) {
                keymaps.put(action, inputEvent);
                keyPersistence.call("saveKeymap");
            }
        }

        String text = (String) ((org.godot.Godot) inputEvent).call("as_text");
        setProperty("text", text);
    }

    private void displayCurrentKey() {
        org.godot.singleton.InputMap inputMap = org.godot.singleton.InputMap.singleton();
        Object[] events = (Object[]) inputMap.call("action_get_events", action);
        if (events != null && events.length > 0) {
            String currentKey = (String) ((org.godot.Godot) events[0]).call("as_text");
            setProperty("text", currentKey);
        }
    }
}
