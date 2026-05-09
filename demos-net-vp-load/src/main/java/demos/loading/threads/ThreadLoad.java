package demos.loading.threads;

import org.godot.annotation.GodotClass;
import org.godot.annotation.GodotMethod;
import org.godot.core.Callable;
import org.godot.node.Control;
import org.godot.node.Resource;
import org.godot.node.Texture2D;
import org.godot.node.TextureRect;
import org.godot.node.Thread;
import org.godot.singleton.ResourceLoader;

@GodotClass(name = "ThreadLoad", parent = "Control")
public class ThreadLoad extends Control {

    private Thread thread;

    @GodotMethod
    public void _onLoadPressed() {
        if (thread != null && thread.isStarted()) {
            thread.waitToFinish();
        }

        thread = Thread.create();
        System.out.println("Starting thread.");
        thread.start(new Callable(this, "_bgLoad", "res://mona.png"));
    }

    public Object _bgLoad(String path) {
        System.out.println("Calling thread function.");
        Resource tex = ResourceLoader.singleton().load(path);
        callDeferred("_bgLoadDone");
        return tex;
    }

    public void _bgLoadDone() {
        if (thread != null) {
            Object tex = thread.waitToFinish();
            System.out.println("Thread finished.");

            TextureRect textureRect = getNodeAs("TextureRect", TextureRect.class);
            if (textureRect != null && tex instanceof Texture2D texture) {
                textureRect.setTexture(texture);
            }
            thread = null;
        }
    }

    @Override
    public void _exitTree() {
        if (thread != null) {
            if (thread.isStarted()) {
                thread.waitToFinish();
            }
            thread = null;
        }
    }
}
