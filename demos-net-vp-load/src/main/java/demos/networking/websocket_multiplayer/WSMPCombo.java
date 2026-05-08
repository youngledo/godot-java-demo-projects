package demos.networking.websocket_multiplayer;

import org.godot.Godot;
import org.godot.annotation.GodotClass;
import org.godot.node.Control;
import org.godot.node.SceneTree;

import java.util.ArrayList;
import java.util.List;

@GodotClass(name = "WSMPCombo", parent = "Control")
public class WSMPCombo extends Control {

    private List<String> paths = new ArrayList<>();

    @Override
    public void _enterTree() {
        String[] childNames = {"Main", "Main2", "Main3", "Main4"};
        String selfPath = getPath().toString();
        for (String chName : childNames) {
            paths.add(selfPath + "/GridContainer/" + chName);
        }

        org.godot.singleton.ClassDB classDB = org.godot.singleton.ClassDB.singleton();
        SceneTree tree = getTree();
        for (String path : paths) {
            Godot newMp = (Godot) classDB.classCallStatic("MultiplayerAPI", "create_default_interface");
            tree.setMultiplayer(tree.getMultiplayer(path), path);
        }
    }

    @Override
    public void _exitTree() {
        SceneTree tree = getTree();
        for (String path : paths) {
            tree.setMultiplayer(null, path);
        }
    }
}
