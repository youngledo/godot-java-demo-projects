package demos.gui.input_mapping;

import org.godot.annotation.Export;
import org.godot.annotation.GodotClass;
import org.godot.annotation.GodotMethod;
import org.godot.node.Button;
import org.godot.math.Color;
import org.godot.node.Node;

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

        setProcessUnhandledKeyInput(false);
        displayCurrentKey();
    }

    @GodotMethod
    public void _toggled(boolean isButtonPressed) {
        setProcessUnhandledKeyInput(isButtonPressed);
        if (isButtonPressed) {
            setProperty("text", "<press a key>");
            setProperty("modulate", new Color(1, 1, 0));
            releaseFocus();
        } else {
            displayCurrentKey();
            setProperty("modulate", new Color(1, 1, 1));
            grabFocus();
        }
    }

    @Override
    public boolean _unhandledKeyInput(Object inputEvent) {
        if (inputEvent instanceof org.godot.Godot) {
            org.godot.node.InputEvent ev = (org.godot.node.InputEvent) inputEvent;
            String className = ev.get_class_();
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
        org.godot.node.Node keyPersistence = getNode("/root/KeyPersistence");
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
