package demos.gui.drag_and_drop;

import org.godot.annotation.GodotClass;
import org.godot.collection.GodotDictionary;
import org.godot.math.Color;
import org.godot.math.Vector2;
import org.godot.node.ColorPickerButton;
import org.godot.node.Control;

@GodotClass(name = "DragDropScript", parent = "ColorPickerButton")
public class DragDropScript extends ColorPickerButton {

    public Object GetDragData(Vector2 atPosition) {
        ColorPickerButton colorPickerButton = ColorPickerButton.create();
        colorPickerButton.setColor(getColor());
        colorPickerButton.setSize(new Vector2(80.0, 50.0));

        Control previewControl = Control.create();
        previewControl.addChild(colorPickerButton);
        Vector2 size = colorPickerButton.getSize();
        colorPickerButton.setPosition(new Vector2(-0.5 * size.getX(), -0.5 * size.getY()));

        setDragPreview(previewControl);
        return getColor();
    }

    public boolean CanDropData(Vector2 atPosition, Object data) {
        if (data instanceof GodotDictionary dictionary) {
            return "Color".equals(dictionary.get("type"));
        }
        return data instanceof Color;
    }

    public void DropData(Vector2 atPosition, Object data) {
        if (data instanceof Color color) {
            setColor(color);
        }
    }
}
