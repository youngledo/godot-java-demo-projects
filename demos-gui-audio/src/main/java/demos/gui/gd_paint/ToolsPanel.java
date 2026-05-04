package demos.gui.gd_paint;

// BLOCKED: This demo depends on PaintControl which uses _draw().
// The tools_panel.gd references paint_control properties and methods.
// This demo cannot be ported.

import org.godot.annotation.GodotClass;
import org.godot.node.Panel;

@GodotClass(name = "ToolsPanel", parent = "Panel")
public class ToolsPanel extends Panel {

    private org.godot.Godot paintControl;
    private org.godot.Godot labelBrushSize;
    private org.godot.Godot labelBrushShape;
    private org.godot.Godot labelStats;
    private org.godot.Godot labelTools;
    private org.godot.Godot saveDialog;
    private boolean initialized = false;

    @Override
    public void _ready() {
        if (initialized) return;
        initialized = true;
        // This demo is BLOCKED because PaintControl requires _draw()
    }
}
