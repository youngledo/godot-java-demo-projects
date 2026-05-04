package demos.gui.msdf_font;

import org.godot.annotation.GodotClass;
import org.godot.annotation.GodotMethod;
import org.godot.node.Control;

@GodotClass(name = "SdfFontDemo", parent = "Control")
public class SdfFontDemo extends Control {

    private boolean initialized = false;

    @Override
    public boolean _input(Object inputEvent) {
        if (inputEvent instanceof org.godot.Godot) {
            org.godot.Godot ev = (org.godot.Godot) inputEvent;
            boolean actionPressed = (boolean) ev.call("is_action_pressed", "toggle_msdf_font");
            if (actionPressed) {
                org.godot.Godot fontLabel = (org.godot.Godot) call("get_node", "CenterContainer/Base/FontLabel");
                if (fontLabel != null) {
                    org.godot.Godot currentFont = (org.godot.Godot) fontLabel.call("get_theme_font", "font");
                    if (currentFont != null) {
                        boolean isMsdf = (boolean) currentFont.getProperty("multichannel_signed_distance_field");
                        if (isMsdf) {
                            fontLabel.call("add_theme_font_override", "font", call("load", "res://montserrat_semibold.ttf"));
                        } else {
                            fontLabel.call("add_theme_font_override", "font", call("load", "res://montserrat_semibold_msdf.ttf"));
                        }
                    }
                }
                updateLabel();
            }
        }
        return false;
    }

    private void updateLabel() {
        org.godot.Godot fontLabel = (org.godot.Godot) call("get_node", "CenterContainer/Base/FontLabel");
        org.godot.Godot fontMode = (org.godot.Godot) call("get_node", "FontMode");
        if (fontLabel != null && fontMode != null) {
            org.godot.Godot font = (org.godot.Godot) fontLabel.call("get_theme_font", "font");
            if (font != null) {
                boolean isMsdf = (boolean) font.getProperty("multichannel_signed_distance_field");
                fontMode.setProperty("text", "Font rendering: " + (isMsdf ? "MSDF" : "Traditional"));
            }
        }
    }

    @GodotMethod
    public void _on_outline_size_value_changed(double value) {
        org.godot.Godot fontLabel = (org.godot.Godot) call("get_node", "CenterContainer/Base/FontLabel");
        org.godot.Godot valueLabel = (org.godot.Godot) call("get_node", "OutlineSize/Value");
        if (fontLabel != null) {
            fontLabel.call("add_theme_constant_override", "outline_size", (int) value);
        }
        if (valueLabel != null) {
            valueLabel.setProperty("text", String.valueOf((int) value));
        }
    }
}
