package demos.gui.msdf_font;

import org.godot.annotation.GodotClass;
import org.godot.annotation.GodotMethod;
import org.godot.node.Control;
import org.godot.node.Node;

@GodotClass(name = "SdfFontDemo", parent = "Control")
public class SdfFontDemo extends Control {

    private boolean initialized = false;

    @Override
    public boolean _input(Object inputEvent) {
        if (inputEvent instanceof org.godot.Godot) {
            org.godot.node.InputEvent ev = (org.godot.node.InputEvent) inputEvent;
            boolean actionPressed = (boolean) ev.isActionPressed("toggle_msdf_font");
            if (actionPressed) {
                org.godot.node.Node fontLabel = getNode("CenterContainer/Base/FontLabel");
                if (fontLabel != null) {
                    org.godot.Godot currentFont = (org.godot.Godot) fontLabel.call("get_theme_font", "font");
                    if (currentFont != null) {
                        boolean isMsdf = (boolean) currentFont.getProperty("multichannel_signed_distance_field");
                        if (isMsdf) {
                            fontLabel.call("add_theme_font_override", "font", org.godot.singleton.ResourceLoader.singleton().load("res://montserrat_semibold.ttf"));
                        } else {
                            fontLabel.call("add_theme_font_override", "font", org.godot.singleton.ResourceLoader.singleton().load("res://montserrat_semibold_msdf.ttf"));
                        }
                    }
                }
                updateLabel();
            }
        }
        return false;
    }

    private void updateLabel() {
        org.godot.node.Node fontLabel = getNode("CenterContainer/Base/FontLabel");
        org.godot.node.Node fontMode = getNode("FontMode");
        if (fontLabel != null && fontMode != null) {
            org.godot.Godot font = (org.godot.Godot) fontLabel.call("get_theme_font", "font");
            if (font != null) {
                boolean isMsdf = (boolean) font.getProperty("multichannel_signed_distance_field");
                fontMode.setProperty("text", "Font rendering: " + (isMsdf ? "MSDF" : "Traditional"));
            }
        }
    }

    @GodotMethod
    public void OnOutlineSizeValueChanged(double value) {
        org.godot.node.Node fontLabel = getNode("CenterContainer/Base/FontLabel");
        org.godot.node.Label valueLabel = (org.godot.node.Label) getNode("OutlineSize/Value");
        if (fontLabel != null) {
            fontLabel.call("add_theme_constant_override", "outline_size", (int) value);
        }
        if (valueLabel != null) {
            valueLabel.setProperty("text", String.valueOf((int) value));
        }
    }
}
