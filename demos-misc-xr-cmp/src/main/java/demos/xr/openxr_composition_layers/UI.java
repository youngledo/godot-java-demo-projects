package demos.xr.openxr_composition_layers;

import org.godot.annotation.GodotClass;
import org.godot.annotation.GodotMethod;
import org.godot.math.Vector2;
import org.godot.node.Control;
import org.godot.node.InputEventMouseMotion;
import org.godot.node.Label;

@GodotClass(name = "UI", parent = "Control")
public class UI extends Control {

    private int buttonCount = 0;

    @Override
    public boolean _input(Object eventObj) {
        if (eventObj instanceof InputEventMouseMotion event) {
            Control cursor = getNodeAs("Cursor", Control.class);
            if (cursor != null) {
                cursor.setPosition(event.getPosition().sub(new Vector2(16, 16)));
            }
        }
        return false;
    }

    @GodotMethod
    public void OnButtonPressed() {
        buttonCount++;
        Label countLabel = getNodeAs("CountLabel", Label.class);
        if (countLabel != null) {
            countLabel.setText("The button has been pressed " + buttonCount + " times!");
        }
    }
}
