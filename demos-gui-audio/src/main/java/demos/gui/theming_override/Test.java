package demos.gui.theming_override;

import org.godot.annotation.GodotClass;
import org.godot.annotation.GodotMethod;
import org.godot.math.Color;
import org.godot.node.Control;
import org.godot.node.Label;

@GodotClass(name = "Test", parent = "Control")
public class Test extends Control {

    private org.godot.Godot label;
    private org.godot.Godot button;
    private org.godot.Godot button2;
    private org.godot.Godot resetAllButton;
    private boolean initialized = false;

    @Override
    public void _ready() {
        if (initialized) return;
        initialized = true;

        label = (org.godot.Godot) call("get_node", "Panel/MarginContainer/VBoxContainer/Label");
        button = (org.godot.Godot) call("get_node", "Panel/MarginContainer/VBoxContainer/Button");
        button2 = (org.godot.Godot) call("get_node", "Panel/MarginContainer/VBoxContainer/Button2");
        resetAllButton = (org.godot.Godot) call("get_node", "Panel/MarginContainer/VBoxContainer/ResetAllButton");

        if (button != null && (boolean) button.call("is_inside_tree")) button.call("grab_focus");
    }

    @GodotMethod
    public void _on_button_pressed() {
        if (!is_inside_tree()) return;

        if (button != null) {
            org.godot.Godot normalStyle = (org.godot.Godot) button.call("get_theme_stylebox", "normal");
            org.godot.Godot hoverStyle = (org.godot.Godot) button.call("get_theme_stylebox", "hover");
            org.godot.Godot pressedStyle = (org.godot.Godot) button.call("get_theme_stylebox", "pressed");

            if (normalStyle != null) {
                org.godot.Godot newNormal = (org.godot.Godot) normalStyle.call("duplicate");
                newNormal.setProperty("border_color", new Color(1, 1, 0));
                button.call("add_theme_stylebox_override", "normal", newNormal);
            }
            if (hoverStyle != null) {
                org.godot.Godot newHover = (org.godot.Godot) hoverStyle.call("duplicate");
                newHover.setProperty("border_color", new Color(1, 1, 0));
                button.call("add_theme_stylebox_override", "hover", newHover);
            }
            if (pressedStyle != null) {
                org.godot.Godot newPressed = (org.godot.Godot) pressedStyle.call("duplicate");
                newPressed.setProperty("border_color", new Color(1, 1, 0));
                button.call("add_theme_stylebox_override", "pressed", newPressed);
            }
        }

        if (label != null) {
            label.call("add_theme_color_override", "font_color", new Color(1, 1, 0.375));
        }
    }

    @GodotMethod
    public void _on_button2_pressed() {
        if (!is_inside_tree()) return;

        if (button2 != null) {
            org.godot.Godot normalStyle = (org.godot.Godot) button2.call("get_theme_stylebox", "normal");
            org.godot.Godot hoverStyle = (org.godot.Godot) button2.call("get_theme_stylebox", "hover");
            org.godot.Godot pressedStyle = (org.godot.Godot) button2.call("get_theme_stylebox", "pressed");

            if (normalStyle != null) {
                org.godot.Godot newNormal = (org.godot.Godot) normalStyle.call("duplicate");
                newNormal.setProperty("border_color", new Color(0, 1, 0.5));
                button2.call("add_theme_stylebox_override", "normal", newNormal);
            }
            if (hoverStyle != null) {
                org.godot.Godot newHover = (org.godot.Godot) hoverStyle.call("duplicate");
                newHover.setProperty("border_color", new Color(0, 1, 0.5));
                button2.call("add_theme_stylebox_override", "hover", newHover);
            }
            if (pressedStyle != null) {
                org.godot.Godot newPressed = (org.godot.Godot) pressedStyle.call("duplicate");
                newPressed.setProperty("border_color", new Color(0, 1, 0.5));
                button2.call("add_theme_stylebox_override", "pressed", newPressed);
            }
        }

        if (label != null) {
            label.call("add_theme_color_override", "font_color", new Color(0.375, 1, 0.75));
        }
    }

    @GodotMethod
    public void _on_reset_all_button_pressed() {
        if (!is_inside_tree()) return;

        if (button != null) {
            button.call("remove_theme_stylebox_override", "normal");
            button.call("remove_theme_stylebox_override", "hover");
            button.call("remove_theme_stylebox_override", "pressed");
        }
        if (button2 != null) {
            button2.call("remove_theme_stylebox_override", "normal");
            button2.call("remove_theme_stylebox_override", "hover");
            button2.call("remove_theme_stylebox_override", "pressed");
        }
        if (label != null) {
            label.call("remove_theme_color_override", "font_color");
        }
    }
}
