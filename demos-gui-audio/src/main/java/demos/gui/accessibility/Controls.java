package demos.gui.accessibility;

// BLOCKED: This demo uses _draw() which is not available in godot-java.
// The custom_control.gd also uses _draw(), accessibility notifications, and
// _notification() with NOTIFICATION_ACCESSIBILITY_* constants that have no
// Java API equivalents. This demo cannot be fully ported.

import org.godot.annotation.GodotClass;
import org.godot.node.Control;
import org.godot.node.Node;

@GodotClass(name = "Controls", parent = "Control")
public class Controls extends Control {

    private boolean initialized = false;

    @Override
    public void _ready() {
        if (initialized) return;
        initialized = true;

        org.godot.node.Control lineEditName = (org.godot.node.Control) getNode("LineEditName");
        if (lineEditName != null && (boolean) lineEditName.isInsideTree()) {
            lineEditName.grabFocus();
        }
    }

    @org.godot.annotation.GodotMethod
    public void OnButtonSetPressed() {
        if (!isInsideTree()) return;;

        org.godot.node.Node liveReg = getNode("LineEditLiveReg");
        org.godot.node.Node labelRegion = getNode("Panel/LabelRegion");
        if (liveReg != null && labelRegion != null) {
            String text = (String) liveReg.getProperty("text");
            labelRegion.setProperty("text", text);
        }
    }
}
