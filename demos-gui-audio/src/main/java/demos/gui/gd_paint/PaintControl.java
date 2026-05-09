package demos.gui.gd_paint;

import org.godot.annotation.GodotClass;
import org.godot.math.Color;
import org.godot.math.Rect2;
import org.godot.math.Vector2;
import org.godot.node.Control;
import org.godot.node.InputEventMouseButton;
import org.godot.node.InputEventMouseMotion;

import java.util.ArrayList;
import java.util.List;

@GodotClass(name = "PaintControl", parent = "Control")
public class PaintControl extends Control {
    public static final int BRUSH_MODE_PENCIL = 0;
    public static final int BRUSH_MODE_ERASER = 1;
    public static final int BRUSH_MODE_CIRCLE_SHAPE = 2;
    public static final int BRUSH_MODE_RECTANGLE_SHAPE = 3;

    public static final int BRUSH_SHAPE_RECTANGLE = 0;
    public static final int BRUSH_SHAPE_CIRCLE = 1;

    public int brushMode = BRUSH_MODE_PENCIL;
    public int brushSize = 32;
    public Color brushColor = new Color(0, 0, 0);
    public int brushShape = BRUSH_SHAPE_CIRCLE;
    public Color bgColor = new Color(1, 1, 1);

    private final List<Stroke> strokes = new ArrayList<>();
    private Stroke activeStroke;

    @Override
    public boolean _guiInput(Object event) {
        if (event instanceof InputEventMouseButton mouseButton && mouseButton.getButtonIndex() == 1L) {
            if (mouseButton.isPressed()) {
                beginStroke(mouseButton.getPosition());
            } else {
                endStroke(mouseButton.getPosition());
            }
            return true;
        }

        if (event instanceof InputEventMouseMotion mouseMotion && activeStroke != null) {
            updateStroke(mouseMotion.getPosition());
            return true;
        }

        return false;
    }

    @Override
    public void _draw() {
        Vector2 size = getSize();
        drawRect(new Rect2(0, 0, size.x, size.y), bgColor);

        for (Stroke stroke : strokes) {
            drawStroke(stroke);
        }
        if (activeStroke != null) {
            drawStroke(activeStroke);
        }
    }

    public void setBrushMode(int brushMode) {
        this.brushMode = brushMode;
    }

    public void setBrushSize(int brushSize) {
        this.brushSize = Math.max(1, brushSize);
    }

    public void setBrushShape(int brushShape) {
        this.brushShape = brushShape;
    }

    public void setBrushColor(Color brushColor) {
        this.brushColor = brushColor;
    }

    public void setBackgroundColor(Color bgColor) {
        this.bgColor = bgColor;
        queueRedraw();
    }

    public int getStrokeCount() {
        return strokes.size() + (activeStroke != null ? 1 : 0);
    }

    public void undoLastStroke() {
        if (!strokes.isEmpty()) {
            strokes.remove(strokes.size() - 1);
            queueRedraw();
        }
    }

    public void clearPicture() {
        strokes.clear();
        activeStroke = null;
        queueRedraw();
    }

    private void beginStroke(Vector2 position) {
        activeStroke = new Stroke(brushMode, brushShape, brushSize, strokeColor());
        activeStroke.start = copy(position);
        activeStroke.end = copy(position);
        activeStroke.points.add(copy(position));
        queueRedraw();
    }

    private void updateStroke(Vector2 position) {
        activeStroke.end = copy(position);
        if (activeStroke.mode == BRUSH_MODE_PENCIL || activeStroke.mode == BRUSH_MODE_ERASER) {
            activeStroke.points.add(copy(position));
        }
        queueRedraw();
    }

    private void endStroke(Vector2 position) {
        if (activeStroke == null) return;

        updateStroke(position);
        strokes.add(activeStroke);
        activeStroke = null;
        queueRedraw();
    }

    private Color strokeColor() {
        if (brushMode == BRUSH_MODE_ERASER) {
            return bgColor;
        }
        return new Color(brushColor.r, brushColor.g, brushColor.b, brushColor.a);
    }

    private void drawStroke(Stroke stroke) {
        if (stroke.mode == BRUSH_MODE_RECTANGLE_SHAPE) {
            drawRect(bounds(stroke.start, stroke.end), stroke.color, false, stroke.size, true);
            return;
        }

        if (stroke.mode == BRUSH_MODE_CIRCLE_SHAPE) {
            drawCircle(stroke.start, stroke.start.distanceTo(stroke.end), stroke.color, false, stroke.size, true);
            return;
        }

        for (int i = 0; i < stroke.points.size(); i++) {
            Vector2 point = stroke.points.get(i);
            drawBrushPoint(point, stroke);
            if (i > 0 && stroke.shape == BRUSH_SHAPE_CIRCLE) {
                drawLine(stroke.points.get(i - 1), point, stroke.color, stroke.size, true);
            }
        }
    }

    private void drawBrushPoint(Vector2 point, Stroke stroke) {
        double radius = stroke.size / 2.0;
        if (stroke.shape == BRUSH_SHAPE_RECTANGLE) {
            drawRect(new Rect2(point.x - radius, point.y - radius, stroke.size, stroke.size), stroke.color);
        } else {
            drawCircle(point, radius, stroke.color);
        }
    }

    private Rect2 bounds(Vector2 a, Vector2 b) {
        double x = Math.min(a.x, b.x);
        double y = Math.min(a.y, b.y);
        return new Rect2(x, y, Math.abs(a.x - b.x), Math.abs(a.y - b.y));
    }

    private Vector2 copy(Vector2 value) {
        return new Vector2(value.x, value.y);
    }

    private static final class Stroke {
        final int mode;
        final int shape;
        final int size;
        final Color color;
        final List<Vector2> points = new ArrayList<>();
        Vector2 start;
        Vector2 end;

        Stroke(int mode, int shape, int size, Color color) {
            this.mode = mode;
            this.shape = shape;
            this.size = size;
            this.color = color;
        }
    }
}
