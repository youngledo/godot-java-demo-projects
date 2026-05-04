package demos.gui.drag_and_drop;

import org.godot.annotation.GodotClass;
import org.godot.math.Vector2;
import org.godot.node.ColorPickerButton;
import org.godot.node.Control;

@GodotClass(name = "DragDropScript", parent = "ColorPickerButton")
public class DragDropScript extends ColorPickerButton {

    // Note: _get_drag_data, _can_drop_data, and _drop_data are virtual methods
    // in Godot's Control class. In godot-java these may need to be registered
    // differently. This port provides the logic matching the GDScript behavior.

    public Object _get_drag_data(Vector2 atPosition) {
        org.godot.Godot cpb = (org.godot.Godot) call("new"); // ColorPickerButton.new()
        if (cpb != null) {
            Object color = getProperty("color");
            cpb.setProperty("color", color);
            cpb.setProperty("size", new Vector2(80.0, 50.0));

            org.godot.Godot preview = (org.godot.Godot) ((org.godot.Godot) call("get_class", "")).call("new");
            // Create a Control node as preview container
            org.godot.node.Control previewControl = new org.godot.node.Control();
            previewControl.call("add_child", cpb);
            Vector2 size = (Vector2) cpb.getProperty("size");
            if (size != null) {
                cpb.setProperty("position", new Vector2(-0.5 * size.getX(), -0.5 * size.getY()));
            }

            call("set_drag_preview", previewControl);
        }

        return getProperty("color");
    }

    public boolean _can_drop_data(Vector2 atPosition, Object data) {
        if (data instanceof org.godot.collection.GodotDictionary) {
            return "Color".equals(((org.godot.collection.GodotDictionary) data).get("type"));
        }
        // Check if data is a Color type
        return data != null && data.getClass().getSimpleName().equals("Color");
    }

    public void _drop_data(Vector2 atPosition, Object data) {
        setProperty("color", data);
    }
}
