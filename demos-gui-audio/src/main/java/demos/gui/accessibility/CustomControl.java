package demos.gui.accessibility;

import org.godot.annotation.GodotClass;
import org.godot.math.Color;
import org.godot.math.Rect2;
import org.godot.math.Vector2;
import org.godot.node.Control;
import org.godot.node.Font;
import org.godot.node.InputEvent;
import org.godot.node.InputEventMouseButton;

@GodotClass(name = "CustomControl", parent = "Control")
public class CustomControl extends Control {
    private static final int ITEM_COUNT = 3;
    private final int[] itemValues = new int[ITEM_COUNT];
    private final String[] itemNames = {"Item 1", "Item 2", "Item 3"};
    private int selected;

    @Override
    public void _ready() {
        itemValues[0] = 25;
        itemValues[1] = 50;
        itemValues[2] = 75;
    }

    @Override
    public boolean _guiInput(Object event) {
        if (event instanceof InputEventMouseButton mouseButton && mouseButton.getButtonIndex() == 1L && mouseButton.isPressed()) {
            int row = (int) ((mouseButton.getPosition().y - 34.0) / 56.0);
            if (row >= 0 && row < ITEM_COUNT) {
                selected = row;
                itemValues[row] = (itemValues[row] + 10) % 110;
                queueRedraw();
            }
            return true;
        }

        if (event instanceof InputEvent inputEvent) {
            if (inputEvent.isActionPressed("ui_down")) {
                selected = (selected + 1) % ITEM_COUNT;
                queueRedraw();
                return true;
            }
            if (inputEvent.isActionPressed("ui_up")) {
                selected = (selected + ITEM_COUNT - 1) % ITEM_COUNT;
                queueRedraw();
                return true;
            }
            if (inputEvent.isActionPressed("ui_accept")) {
                itemValues[selected] = (itemValues[selected] + 10) % 110;
                queueRedraw();
                return true;
            }
        }

        return false;
    }

    @Override
    public void _draw() {
        Font font = getThemeDefaultFont();
        drawRect(new Rect2(0, 0, getSize().x, getSize().y), new Color(0.12, 0.13, 0.16));
        drawString(font, new Vector2(18, 24), "Custom Java Control", 0, -1.0, 18L, new Color(0.95, 0.95, 0.95));

        for (int i = 0; i < ITEM_COUNT; i++) {
            double y = 34.0 + i * 56.0;
            boolean isSelected = i == selected;
            Color rowColor = isSelected ? new Color(0.25, 0.45, 0.9, 0.8) : new Color(0.22, 0.23, 0.27);
            drawRect(new Rect2(12, y, getSize().x - 24.0, 44.0), rowColor);
            drawString(font, new Vector2(24, y + 28.0), itemNames[i], 0, -1.0, 16L, new Color(1, 1, 1));
            drawRect(new Rect2(105, y + 13.0, 150.0, 18.0), new Color(0.05, 0.05, 0.06));
            drawRect(new Rect2(105, y + 13.0, itemValues[i] * 1.5, 18.0), new Color(0.45, 1.0, 0.45));
            drawString(font, new Vector2(270, y + 28.0), itemValues[i] + "%", 0, -1.0, 16L, new Color(1, 1, 1));
        }
    }
}
