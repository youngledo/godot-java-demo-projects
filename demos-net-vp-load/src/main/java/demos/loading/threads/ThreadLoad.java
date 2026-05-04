package demos.loading.threads;

import org.godot.annotation.GodotClass;
import org.godot.annotation.GodotMethod;
import org.godot.node.Control;
import org.godot.Godot;

/**
 * Demonstrates loading a resource in a background thread.
 * Uses Godot's Thread class to load an image without blocking the main thread.
 */
@GodotClass(name = "ThreadLoad", parent = "Control")
public class ThreadLoad extends Control {

    private Godot thread;

    @GodotMethod
    public void _onLoadPressed() {
        // If a thread is already running, let it finish before we start another.
        if (thread != null) {
            boolean started = (boolean) thread.call("is_started");
            if (started) {
                thread.call("wait_to_finish");
            }
        }

        thread = (Godot) call("Thread.new");
        System.out.println("Starting thread.");

        // Start the thread - our _bgLoad method needs an argument.
        thread.call("start", this, "_bgLoad", "res://mona.png");
    }

    /**
     * Background load function - runs on a separate thread.
     * Loads the texture and then defers completion callback to main thread.
     */
    public Object _bgLoad(String path) {
        System.out.println("Calling thread function.");
        Object tex = call("load", path);
        // call_deferred tells the main thread to call a method during idle time.
        // Our method operates on nodes in the tree, so it isn't safe to call directly.
        call("call_deferred", "_bgLoadDone");
        return tex;
    }

    /**
     * Called on the main thread after the background load completes.
     */
    public void _bgLoadDone() {
        if (thread != null) {
            // Wait for the thread to complete, and get the returned value.
            Object tex = thread.call("wait_to_finish");
            System.out.println("Thread finished.");

            Godot textureRect = (Godot) call("get_node", "TextureRect");
            if (textureRect != null) {
                textureRect.setProperty("texture", tex);
            }
            // We're done with the thread now, so we can free it.
            thread = null;
        }
    }

    @Override
    public void _exitTree() {
        // Always wait for a thread to finish before letting it get freed.
        if (thread != null) {
            boolean started = (boolean) thread.call("is_started");
            if (started) {
                thread.call("wait_to_finish");
            }
            thread = null;
        }
    }
}
