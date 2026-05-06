package demos.viewport.threed_in_twod;

import org.godot.annotation.GodotClass;
import org.godot.core.Callable;
import org.godot.math.Vector2;
import org.godot.math.Vector2i;
import org.godot.node.Node2D;
import org.godot.node.Sprite2D;
import org.godot.node.SubViewport;
import org.godot.node.Node;

@GodotClass(name = "ThreeDInTwoD", parent = "Node2D")
public class ThreeDInTwoD extends Node2D {

    private SubViewport viewport;
    private Vector2i viewportInitialSize;
    private Sprite2D viewportSprite;
    private boolean initialized = false;

    @Override
    public void _ready() {
        if (initialized) return;
        initialized = true;

        viewport = (SubViewport) getNode("SubViewport");
        viewportSprite = (Sprite2D) getNode("ViewportSprite");

        if (viewport != null) {
            viewportInitialSize = (Vector2i) viewport.getProperty("size");
        }

        // Start animated sprite
        org.godot.node.AnimatedSprite2D animatedSprite = (org.godot.node.AnimatedSprite2D) getNode("AnimatedSprite2D");
        if (animatedSprite != null) {
            animatedSprite.play();
        }

        // Connect viewport size_changed signal
        Object rootViewport = getViewport();
        if (rootViewport != null) {
            ((org.godot.Godot) rootViewport).connect("size_changed", new Callable(this, "_rootViewportSizeChanged"), 0);
        }
    }

    @org.godot.annotation.GodotMethod
    public void _rootViewportSizeChanged() {
        Object rootViewport = getViewport();
        if (rootViewport == null || viewport == null || viewportSprite == null || viewportInitialSize == null) return;

        Vector2i rootSize = (Vector2i) ((org.godot.Godot) rootViewport).getProperty("size");
        if (rootSize == null) return;

        // The viewport is resized depending on the window height
        viewport.setProperty("size", new Vector2i((int) rootSize.getY(), (int) rootSize.getY()));

        // To compensate for the larger resolution, the viewport sprite is scaled down
        double scale = viewportInitialSize.getY() / rootSize.getY();
        viewportSprite.setProperty("scale", new Vector2(scale, scale));
    }
}
