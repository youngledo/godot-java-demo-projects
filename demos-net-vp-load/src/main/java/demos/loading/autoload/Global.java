package demos.loading.autoload;

import org.godot.annotation.GodotClass;
import org.godot.node.Node;
import org.godot.node.PackedScene;
import org.godot.node.Resource;
import org.godot.node.SceneTree;
import org.godot.singleton.ResourceLoader;

@GodotClass(name = "Global", parent = "Node")
public class Global extends Node {

    public void gotoScene(String path) {
        callDeferred("_deferredGotoScene", path);
    }

    public void _deferredGotoScene(String path) {
        SceneTree tree = getTree();
        Node currentScene = tree.getCurrentScene();
        if (currentScene != null) {
            currentScene.free();
        }

        Resource resource = ResourceLoader.singleton().load(path);
        if (!(resource instanceof PackedScene packedScene)) return;

        Node instancedScene = packedScene.instantiate();
        tree.getRoot().addChild(instancedScene);
        tree.setCurrentScene(instancedScene);
    }
}
