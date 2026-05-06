package demos.gui.input_mapping;

import org.godot.Godot;
import org.godot.annotation.GodotClass;
import org.godot.node.FileAccess;
import org.godot.node.Node;

@GodotClass(name = "KeyPersistence", parent = "Node")
public class KeyPersistence extends Node {

    private static final String KEYMAPS_PATH = "user://keymaps.dat";
    public org.godot.collection.GodotDictionary keymaps = new org.godot.collection.GodotDictionary();

    private boolean initialized = false;

    @Override
    public void _ready() {
        if (initialized) return;
        initialized = true;

        org.godot.singleton.InputMap inputMap = org.godot.singleton.InputMap.singleton();

        // Populate keymaps with all actions
        String[] actions = (String[]) inputMap.call("get_actions");
        if (actions != null) {
            for (String action : actions) {
                Object[] events = (Object[]) inputMap.call("action_get_events", action);
                if (events != null && events.length > 0) {
                    keymaps.put(action, events[0]);
                }
            }
        }

        loadKeymap();
    }

    public void loadKeymap() {
        org.godot.singleton.ResourceLoader resourceLoader = org.godot.singleton.ResourceLoader.singleton();

        if (!FileAccess.fileExists(KEYMAPS_PATH)) {
            saveKeymap();
            return;
        }

        FileAccess file = FileAccess.open(KEYMAPS_PATH, 1); // FileAccess.READ = 1
        if (file != null) {
            Object tempKeymap = file.getVar(true);
            file.close();

            org.godot.singleton.InputMap inputMap = org.godot.singleton.InputMap.singleton();

            if (tempKeymap instanceof org.godot.collection.GodotDictionary) {
                org.godot.collection.GodotDictionary tempDict = (org.godot.collection.GodotDictionary) tempKeymap;
                Object[] keys = (Object[]) keymaps.call("keys");
                if (keys != null) {
                    for (Object keyObj : keys) {
                        String action = (String) keyObj;
                        if (tempDict.containsKey(action)) {
                            keymaps.put(action, tempDict.get(action));
                            inputMap.call("action_erase_events", action);
                            inputMap.call("action_add_event", action, keymaps.get(action));
                        }
                    }
                }
            }
        }
    }

    public void saveKeymap() {
        FileAccess file = FileAccess.open(KEYMAPS_PATH, 2); // FileAccess.WRITE = 2
        if (file != null) {
            file.storeVar(keymaps, true);
            file.close();
        }
    }
}
