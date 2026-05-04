package demos.gui.accessibility;

// BLOCKED: This demo uses _draw() which is not available in godot-java.
// The custom_control.gd also uses _draw(), accessibility notifications, and
// _notification() with NOTIFICATION_ACCESSIBILITY_* constants that have no
// Java API equivalents. This demo cannot be fully ported.

import org.godot.annotation.GodotClass;
import org.godot.node.Control;

@GodotClass(name = "Controls", parent = "Control")
public class Controls extends Control {

    private boolean initialized = false;

    @Override
    public void _ready() {
        if (initialized) return;
        initialized = true;

        org.godot.Godot lineEditName = (org.godot.Godot) call("get_node", "LineEditName");
        if (lineEditName != null && (boolean) lineEditName.call("is_inside_tree")) {
            lineEditName.call("grab_focus");
        }
    }

    @org.godot.annotation.GodotMethod
    public void _on_button_set_pressed() {
        if (!is_inside_tree()) return;

        org.godot.Godot liveReg = (org.godot.Godot) call("get_node", "LineEditLiveReg");
        org.godot.Godot labelRegion = (org.godot.Godot) call("get_node", "Panel/LabelRegion");
        if (liveReg != null && labelRegion != null) {
            String text = (String) liveReg.getProperty("text");
            labelRegion.setProperty("text", text);
        }
    }
}
