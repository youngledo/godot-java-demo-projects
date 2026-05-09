package demos.gui.theming_override;

import org.godot.annotation.GodotClass;
import org.godot.annotation.GodotMethod;
import org.godot.math.Color;
import org.godot.node.Button;
import org.godot.node.Control;
import org.godot.node.Label;
import org.godot.node.Resource;
import org.godot.node.StyleBox;
import org.godot.node.StyleBoxFlat;

@GodotClass(name = "Test", parent = "Control")
public class Test extends Control {

    private Label label;
    private Button button;
    private Button button2;
    private boolean initialized = false;

    @Override
    public void _ready() {
        if (initialized) return;
        initialized = true;

        label = getNodeAs("Panel/MarginContainer/VBoxContainer/Label", Label.class);
        button = getNodeAs("Panel/MarginContainer/VBoxContainer/Button", Button.class);
        button2 = getNodeAs("Panel/MarginContainer/VBoxContainer/Button2", Button.class);

        if (button != null && button.isInsideTree()) button.grabFocus();
    }

    @GodotMethod
    public void OnButtonPressed() {
        if (!isInsideTree()) return;

        overrideButtonBorder(button, new Color(1, 1, 0));
        if (label != null) {
            label.addThemeColorOverride("font_color", new Color(1, 1, 0.375));
        }
    }

    @GodotMethod
    public void OnButton2Pressed() {
        if (!isInsideTree()) return;

        overrideButtonBorder(button2, new Color(0, 1, 0.5));
        if (label != null) {
            label.addThemeColorOverride("font_color", new Color(0.375, 1, 0.75));
        }
    }

    @GodotMethod
    public void OnResetAllButtonPressed() {
        if (!isInsideTree()) return;

        removeButtonOverrides(button);
        removeButtonOverrides(button2);
        if (label != null) {
            label.removeThemeColorOverride("font_color");
        }
    }

    private void overrideButtonBorder(Button target, Color color) {
        if (target == null) return;
        overrideButtonBorder(target, "normal", color);
        overrideButtonBorder(target, "hover", color);
        overrideButtonBorder(target, "pressed", color);
    }

    private void overrideButtonBorder(Button target, String name, Color color) {
        StyleBox style = target.getThemeStylebox(name);
        if (style == null) return;

        Resource duplicated = style.duplicate();
        if (duplicated instanceof StyleBoxFlat styleBoxFlat) {
            styleBoxFlat.setBorderColor(color);
            target.addThemeStyleboxOverride(name, styleBoxFlat);
        }
    }

    private void removeButtonOverrides(Button target) {
        if (target == null) return;
        target.removeThemeStyleboxOverride("normal");
        target.removeThemeStyleboxOverride("hover");
        target.removeThemeStyleboxOverride("pressed");
    }
}
