package demos.gui.gd_paint;

import org.godot.annotation.GodotClass;
import org.godot.annotation.GodotMethod;
import org.godot.math.Color;
import org.godot.node.ColorPickerButton;
import org.godot.node.HScrollBar;
import org.godot.node.Label;
import org.godot.node.Panel;

@GodotClass(name = "ToolsPanel", parent = "Panel")
public class ToolsPanel extends Panel {
    private PaintControl paintControl;
    private Label labelBrushSize;
    private Label labelBrushShape;
    private Label labelStats;
    private Label labelTools;
    private HScrollBar brushSizeSlider;
    private boolean initialized;

    @Override
    public void _ready() {
        if (initialized) return;
        initialized = true;

        paintControl = getNodeAs("../PaintControl", PaintControl.class);
        labelBrushSize = getNodeAs("BrushSettings/LabelBrushSize", Label.class);
        labelBrushShape = getNodeAs("BrushSettings/LabelBrushShape", Label.class);
        labelStats = getNodeAs("LabelStats", Label.class);
        labelTools = getNodeAs("LabelTools", Label.class);
        brushSizeSlider = getNodeAs("BrushSettings/HScrollBarBrushSize", HScrollBar.class);

        ColorPickerButton brushColor = getNodeAs("ColorPickerBrush", ColorPickerButton.class);
        ColorPickerButton backgroundColor = getNodeAs("ColorPickerBackground", ColorPickerButton.class);
        if (paintControl != null) {
            if (brushSizeSlider != null) paintControl.setBrushSize((int) Math.round(brushSizeSlider.getValue()));
            if (brushColor != null) paintControl.setBrushColor(brushColor.getColor());
            if (backgroundColor != null) paintControl.setBackgroundColor(backgroundColor.getColor());
        }
        setProcess(true);
        refreshLabels();
    }

    @Override
    public void _process(double delta) {
        refreshStats();
    }

    @GodotMethod
    public void _on_tool_pencil_pressed() {
        setTool(PaintControl.BRUSH_MODE_PENCIL, "Pencil");
    }

    @GodotMethod
    public void _on_tool_eraser_pressed() {
        setTool(PaintControl.BRUSH_MODE_ERASER, "Eraser");
    }

    @GodotMethod
    public void _on_tool_rectangle_pressed() {
        setTool(PaintControl.BRUSH_MODE_RECTANGLE_SHAPE, "Rectangle");
    }

    @GodotMethod
    public void _on_tool_circle_pressed() {
        setTool(PaintControl.BRUSH_MODE_CIRCLE_SHAPE, "Circle");
    }

    @GodotMethod
    public void _on_shape_box_pressed() {
        if (paintControl != null) paintControl.setBrushShape(PaintControl.BRUSH_SHAPE_RECTANGLE);
        refreshLabels();
    }

    @GodotMethod
    public void _on_shape_circle_pressed() {
        if (paintControl != null) paintControl.setBrushShape(PaintControl.BRUSH_SHAPE_CIRCLE);
        refreshLabels();
    }

    @GodotMethod
    public void _on_brush_size_changed(double value) {
        if (paintControl != null) paintControl.setBrushSize((int) Math.round(value));
        refreshLabels();
    }

    @GodotMethod
    public void _on_brush_color_changed(Color color) {
        if (paintControl != null) paintControl.setBrushColor(color);
    }

    @GodotMethod
    public void _on_background_color_changed(Color color) {
        if (paintControl != null) paintControl.setBackgroundColor(color);
    }

    @GodotMethod
    public void _on_undo_pressed() {
        if (paintControl != null) paintControl.undoLastStroke();
        refreshStats();
    }

    @GodotMethod
    public void _on_clear_pressed() {
        if (paintControl != null) paintControl.clearPicture();
        refreshStats();
    }

    @GodotMethod
    public void _on_save_pressed() {
        if (labelStats != null) labelStats.setText("Save uses Godot frame capture in the original demo");
    }

    private void setTool(int mode, String name) {
        if (paintControl != null) paintControl.setBrushMode(mode);
        if (labelTools != null) labelTools.setText("Selected tool: " + name);
    }

    private void refreshLabels() {
        if (paintControl == null) return;

        if (labelBrushSize != null) labelBrushSize.setText("Brush size: " + paintControl.brushSize + "px");
        if (labelBrushShape != null) {
            String shape = paintControl.brushShape == PaintControl.BRUSH_SHAPE_RECTANGLE ? "Box" : "Circle";
            labelBrushShape.setText("Brush shape: " + shape);
        }
        refreshStats();
    }

    private void refreshStats() {
        if (paintControl != null && labelStats != null) {
            labelStats.setText(String.format("Brush objects: %05d", paintControl.getStrokeCount()));
        }
    }
}
