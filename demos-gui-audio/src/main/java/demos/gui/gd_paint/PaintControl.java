package demos.gui.gd_paint;

// BLOCKED: This demo uses _draw() which is not available in godot-java.
// The paint_control.gd relies entirely on _draw() for rendering brush strokes.
// Additionally, it uses await (RenderingServer.frame_post_draw) which is not
// directly supported. This demo cannot be ported.

import org.godot.annotation.GodotClass;
import org.godot.node.Control;

@GodotClass(name = "PaintControl", parent = "Control")
public class PaintControl extends Control {

    public static final int BRUSH_MODE_PENCIL = 0;
    public static final int BRUSH_MODE_ERASER = 1;
    public static final int BRUSH_MODE_CIRCLE_SHAPE = 2;
    public static final int BRUSH_MODE_RECTANGLE_SHAPE = 3;

    public static final int BRUSH_SHAPE_RECTANGLE = 0;
    public static final int BRUSH_SHAPE_CIRCLE = 1;

    public static final int UNDO_MODE_SHAPE = -2;
    public static final int UNDO_NONE = -1;

    public java.util.List<org.godot.collection.GodotDictionary> brushDataList = new java.util.ArrayList<>();
    public int brushMode = BRUSH_MODE_PENCIL;
    public int brushSize = 32;
    public org.godot.math.Color brushColor = new org.godot.math.Color(0, 0, 0);
    public int brushShape = BRUSH_SHAPE_CIRCLE;
    public org.godot.math.Color bgColor = new org.godot.math.Color(1, 1, 1);
    public int undoElementListNum = UNDO_NONE;

    // _draw() is NOT available in godot-java - this demo is BLOCKED
}
