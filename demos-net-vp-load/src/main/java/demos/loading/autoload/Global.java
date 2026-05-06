package demos.loading.autoload;

import org.godot.Godot;
import org.godot.annotation.GodotClass;
import org.godot.node.Node;

/**
 * Autoload singleton that handles scene switching without using
 * SceneTree.call("change_scene_to_file") or changeSceneToPacked() helpers.
 * Instead, it manually frees the current scene, loads a new PackedScene,
 * instances it, adds it to root, and sets it as current_scene.
 */
@GodotClass(name = "Global", parent = "Node")
public class Global extends Node {

    /**
     * Called to switch to a new scene at the given path.
     * Defers the actual work to avoid freeing the current scene
     * while it might still be in a callback.
     */
    public void gotoScene(String path) {
        // Defer so we don't free the scene while it's still executing code.
        callDeferred("_deferredGotoScene", path);
    }

    /**
     * Deferred method that performs the actual scene switch.
     */
    public void _deferredGotoScene(String path) {
        // Free the current scene immediately (safe because this is deferred).
        Godot tree = (Godot) getTree();
        Godot currentScene = (Godot) tree.getProperty("current_scene");
        if (currentScene != null) {
            currentScene.call("free");
        }

        // Load the new scene as a PackedScene.
        Object packedSceneObj = call("ResourceLoader.load", path);
        if (packedSceneObj == null) return;

        Godot packedScene = (Godot) packedSceneObj;
        Godot instancedScene = (Godot) packedScene.call("instantiate");

        // Add it to the scene tree, as direct child of root.
        Object root = tree.getProperty("root");
        if (root instanceof Godot) {
            ((Godot) root).call("add_child", instancedScene);
        }

        // Set it as the current scene, only after it has been added to the tree.
        tree.setProperty("current_scene", instancedScene);
    }
}
