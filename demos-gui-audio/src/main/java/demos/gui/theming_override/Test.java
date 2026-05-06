package demos.gui.theming_override;

import org.godot.annotation.GodotClass;
import org.godot.annotation.GodotMethod;
import org.godot.math.Color;
import org.godot.node.Control;
import org.godot.node.Label;
import org.godot.node.Node;

@GodotClass(name = "Test", parent = "Control")
public class Test extends Control {

    private org.godot.node.Label label;
    private org.godot.node.Button button;
    private org.godot.node.Button button2;
    private org.godot.node.Node resetAllButton;
    private boolean initialized = false;

    @Override
    public void _ready() {
        if (initialized) return;
        initialized = true;

        label = (org.godot.node.Label) getNode("Panel/MarginContainer/VBoxContainer/Label");
        button = (org.godot.node.Button) getNode("Panel/MarginContainer/VBoxContainer/Button");
        button2 = (org.godot.node.Button) getNode("Panel/MarginContainer/VBoxContainer/Button2");
        resetAllButton = getNode("Panel/MarginContainer/VBoxContainer/ResetAllButton");

        if (button != null && (boolean) button.isInsideTree()) button.grabFocus();
    }

    @GodotMethod
    public void OnButtonPressed() {
        if (!isInsideTree()) return;;

        if (button != null) {
            org.godot.node.Resource normalStyle = (org.godot.node.Resource) button.getThemeStylebox("normal");
            org.godot.node.Resource hoverStyle = (org.godot.node.Resource) button.getThemeStylebox("hover");
            org.godot.node.Resource pressedStyle = (org.godot.node.Resource) button.getThemeStylebox("pressed");

            if (normalStyle != null) {
                org.godot.Godot newNormal = (org.godot.node.Resource) normalStyle.duplicate();
                newNormal.setProperty("border_color", new Color(1, 1, 0));
                button.call("add_theme_stylebox_override", "normal", newNormal);
            }
            if (hoverStyle != null) {
                org.godot.Godot newHover = (org.godot.node.Resource) hoverStyle.duplicate();
                newHover.setProperty("border_color", new Color(1, 1, 0));
                button.call("add_theme_stylebox_override", "hover", newHover);
            }
            if (pressedStyle != null) {
                org.godot.Godot newPressed = (org.godot.node.Resource) pressedStyle.duplicate();
                newPressed.setProperty("border_color", new Color(1, 1, 0));
                button.call("add_theme_stylebox_override", "pressed", newPressed);
            }
        }

        if (label != null) {
            label.call("add_theme_color_override", "font_color", new Color(1, 1, 0.375));
        }
    }

    @GodotMethod
    public void OnButton2Pressed() {
        if (!isInsideTree()) return;;

        if (button2 != null) {
            org.godot.node.Resource normalStyle = (org.godot.node.Resource) button2.getThemeStylebox("normal");
            org.godot.node.Resource hoverStyle = (org.godot.node.Resource) button2.getThemeStylebox("hover");
            org.godot.node.Resource pressedStyle = (org.godot.node.Resource) button2.getThemeStylebox("pressed");

            if (normalStyle != null) {
                org.godot.Godot newNormal = (org.godot.node.Resource) normalStyle.duplicate();
                newNormal.setProperty("border_color", new Color(0, 1, 0.5));
                button2.call("add_theme_stylebox_override", "normal", newNormal);
            }
            if (hoverStyle != null) {
                org.godot.Godot newHover = (org.godot.node.Resource) hoverStyle.duplicate();
                newHover.setProperty("border_color", new Color(0, 1, 0.5));
                button2.call("add_theme_stylebox_override", "hover", newHover);
            }
            if (pressedStyle != null) {
                org.godot.Godot newPressed = (org.godot.node.Resource) pressedStyle.duplicate();
                newPressed.setProperty("border_color", new Color(0, 1, 0.5));
                button2.call("add_theme_stylebox_override", "pressed", newPressed);
            }
        }

        if (label != null) {
            label.addThemeColorOverride("font_color", new Color(0.375, 1, 0.75));
        }
    }

    @GodotMethod
    public void OnResetAllButtonPressed() {
        if (!isInsideTree()) return;;

        if (button != null) {
            button.removeThemeStyleboxOverride("normal");
            button.removeThemeStyleboxOverride("hover");
            button.removeThemeStyleboxOverride("pressed");
        }
        if (button2 != null) {
            button2.removeThemeStyleboxOverride("normal");
            button2.removeThemeStyleboxOverride("hover");
            button2.removeThemeStyleboxOverride("pressed");
        }
        if (label != null) {
            label.removeThemeColorOverride("font_color");
        }
    }
}
