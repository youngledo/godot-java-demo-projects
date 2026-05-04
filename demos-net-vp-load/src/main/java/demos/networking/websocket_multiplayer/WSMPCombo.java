package demos.networking.websocket_multiplayer;

import org.godot.Godot;
import org.godot.annotation.GodotClass;
import org.godot.node.Control;

import java.util.ArrayList;
import java.util.List;

@GodotClass(name = "WSMPCombo", parent = "Control")
public class WSMPCombo extends Control {

    private List<String> paths = new ArrayList<>();

    @Override
    public void _enterTree() {
        // The combo.tscn has 4 Main instances as children of GridContainer
        String[] childNames = {"Main", "Main2", "Main3", "Main4"};
        String selfPath = String.valueOf(call("get_path"));
        for (String chName : childNames) {
            paths.add(selfPath + "/GridContainer/" + chName);
        }

        org.godot.singleton.ClassDB classDB = org.godot.singleton.ClassDB.singleton();
        Godot tree = (Godot) call("get_tree");
        for (String path : paths) {
            Godot newMp = (Godot) classDB.class_call_static("MultiplayerAPI", "create_default_interface");
            tree.call("set_multiplayer", newMp, path);
        }
    }

    @Override
    public void _exitTree() {
        Godot tree = (Godot) call("get_tree");
        for (String path : paths) {
            tree.call("set_multiplayer", null, path);
        }
    }
}
