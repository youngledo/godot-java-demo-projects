package demos.xr.openxr_composition_layers;

import org.godot.annotation.GodotClass;
import org.godot.annotation.GodotMethod;
import org.godot.node.Control;
import org.godot.node.Node;

@GodotClass(name = "UI", parent = "Control")
public class UI extends Control {

    private int buttonCount = 0;

    @Override
    public boolean _input(Object eventObj) {
        if (!(eventObj instanceof org.godot.Godot)) return false;
        org.godot.Godot event = (org.godot.Godot) eventObj;
        Object className = event.call("get_class");
        if ("InputEventMouseMotion".equals(className)) {
            org.godot.math.Vector2 pos = (org.godot.math.Vector2) event.getProperty("position");
            org.godot.node.Node cursor = getNode("Cursor");
            if (cursor != null && pos != null) {
                cursor.setProperty("position", pos.sub(new org.godot.math.Vector2(16, 16)));
            }
        }
        return false;
    }

    @GodotMethod
    public void OnButtonPressed() {
        buttonCount++;
        org.godot.node.Node countLabel = getNode("CountLabel");
        if (countLabel != null) {
            countLabel.setProperty("text", "The button has been pressed " + buttonCount + " times!");
        }
    }
}
