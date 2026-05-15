package demos.gui.msdf_font;

import org.godot.annotation.GodotClass;
import org.godot.annotation.GodotMethod;
import org.godot.node.Control;
import org.godot.node.Font;
import org.godot.node.FontFile;
import org.godot.node.InputEvent;
import org.godot.node.Label;
import org.godot.node.SystemFont;
import org.godot.singleton.ResourceLoader;

@GodotClass(name = "SdfFontDemo", parent = "Control")
public class SdfFontDemo extends Control {

    @Override
    public boolean _input(Object inputEvent) {
        if (inputEvent instanceof InputEvent event && event.isActionPressed("toggle_msdf_font")) {
            Label fontLabel = getNodeAs("CenterContainer/Base/FontLabel", Label.class);
            if (fontLabel != null) {
                Font currentFont = fontLabel.getThemeFont("font");
                String fontPath = isMsdf(currentFont) ? "res://montserrat_semibold.ttf" : "res://montserrat_semibold_msdf.ttf";
                if (ResourceLoader.singleton().load(fontPath) instanceof Font font) {
                    fontLabel.addThemeFontOverride("font", font);
                }
            }
            updateLabel();
        }
        return false;
    }

    private void updateLabel() {
        Label fontLabel = getNodeAs("CenterContainer/Base/FontLabel", Label.class);
        Label fontMode = getNodeAs("FontMode", Label.class);
        if (fontLabel != null && fontMode != null) {
            fontMode.setText("Font rendering: " + (isMsdf(fontLabel.getThemeFont("font")) ? "MSDF" : "Traditional"));
        }
    }

    @GodotMethod
    public void OnOutlineSizeValueChanged(double value) {
        Label fontLabel = getNodeAs("CenterContainer/Base/FontLabel", Label.class);
        Label valueLabel = getNodeAs("OutlineSize/Value", Label.class);
        if (fontLabel != null) {
            fontLabel.addThemeConstantOverride("outline_size", (int) value);
        }
        if (valueLabel != null) {
            valueLabel.setText(String.valueOf((int) value));
        }
    }

    private boolean isMsdf(Font font) {
        if (font instanceof FontFile fontFile) return fontFile.isMultichannelSignedDistanceField();
        if (font instanceof SystemFont systemFont) return systemFont.isMultichannelSignedDistanceField();
        return false;
    }
}
