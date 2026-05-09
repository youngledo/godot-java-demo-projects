package demos.gui.input_mapping;

import org.godot.annotation.GodotClass;
import org.godot.collection.GodotDictionary;
import org.godot.node.FileAccess;
import org.godot.node.InputEvent;
import org.godot.node.Node;
import org.godot.singleton.InputMap;

@GodotClass(name = "KeyPersistence", parent = "Node")
public class KeyPersistence extends Node {

    private static final String KEYMAPS_PATH = "user://keymaps.dat";
    public GodotDictionary keymaps = new GodotDictionary();

    private boolean initialized = false;

    @Override
    public void _ready() {
        if (initialized) return;
        initialized = true;

        InputMap inputMap = InputMap.singleton();
        for (String action : inputMap.getActions()) {
            InputEvent[] events = inputMap.actionGetEvents(action);
            if (events.length > 0) {
                keymaps.put(action, events[0]);
            }
        }

        loadKeymap();
    }

    public void loadKeymap() {
        if (!FileAccess.fileExists(KEYMAPS_PATH)) {
            saveKeymap();
            return;
        }

        FileAccess file = FileAccess.open(KEYMAPS_PATH, 1);
        if (file == null) return;

        Object tempKeymap = file.getVar(true);
        file.close();

        if (!(tempKeymap instanceof GodotDictionary tempDict)) return;

        InputMap inputMap = InputMap.singleton();
        for (String action : inputMap.getActions()) {
            if (tempDict.containsKey(action)) {
                Object event = tempDict.get(action);
                if (event instanceof InputEvent inputEvent) {
                    keymaps.put(action, inputEvent);
                    inputMap.actionEraseEvents(action);
                    inputMap.actionAddEvent(action, inputEvent);
                }
            }
        }
    }

    public void saveKeymap() {
        FileAccess file = FileAccess.open(KEYMAPS_PATH, 2);
        if (file != null) {
            file.storeVar(keymaps, true);
            file.close();
        }
    }
}
