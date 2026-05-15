package demos.gui.input_mapping;

import org.godot.annotation.Export;
import org.godot.annotation.GodotClass;
import org.godot.annotation.GodotMethod;
import org.godot.collection.GodotArray;
import org.godot.math.Color;
import org.godot.node.Button;
import org.godot.node.InputEvent;
import org.godot.node.InputEventKey;
import org.godot.singleton.InputMap;

@GodotClass(name = "ActionRemapButton", parent = "Button")
public class ActionRemapButton extends Button {

    private static final long KEY_ENTER = 4194305;

    @Export
    public String action = "ui_up";

    private boolean initialized = false;

    @Override
    public void _ready() {
        if (initialized) return;
        initialized = true;

        assert InputMap.singleton().hasAction(action);

        setProcessUnhandledKeyInput(false);
        displayCurrentKey();
    }

    @GodotMethod
    public void _toggled(boolean isButtonPressed) {
        setProcessUnhandledKeyInput(isButtonPressed);
        if (isButtonPressed) {
            setText("<press a key>");
            setModulate(new Color(1, 1, 0));
            releaseFocus();
        } else {
            displayCurrentKey();
            setModulate(new Color(1, 1, 1));
            grabFocus();
        }
    }

    @Override
    public boolean _unhandledKeyInput(Object inputEvent) {
        if (inputEvent instanceof InputEventKey event && event.getKeycode() != KEY_ENTER && event.isPressed()) {
            remapActionTo(event);
            setButtonPressed(false);
        }
        return false;
    }

    private void remapActionTo(InputEvent inputEvent) {
        InputMap inputMap = InputMap.singleton();
        inputMap.actionEraseEvents(action);
        inputMap.actionAddEvent(action, inputEvent);

        KeyPersistence keyPersistence = getNodeAs("/root/KeyPersistence", KeyPersistence.class);
        if (keyPersistence != null) {
            keyPersistence.keymaps.put(action, inputEvent);
            keyPersistence.saveKeymap();
        }

        setText(inputEvent.asText());
    }

    private void displayCurrentKey() {
        GodotArray<InputEvent> events = InputMap.singleton().actionGetEvents(action);
        if (events.size() > 0) {
            setText(events.get(0).asText());
        }
    }
}
