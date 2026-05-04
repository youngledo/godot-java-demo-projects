package demos.gui.ui_mirroring;

import org.godot.annotation.GodotClass;
import org.godot.annotation.GodotMethod;
import org.godot.node.Control;

@GodotClass(name = "UIMirroring", parent = "Control")
public class UIMirroring extends Control {

    private boolean initialized = false;

    @Override
    public void _ready() {
        if (initialized) return;
        initialized = true;

        org.godot.singleton.TranslationServer ts = org.godot.singleton.TranslationServer.singleton();
        org.godot.Godot label = (org.godot.Godot) call("get_node", "Label");
        if (label != null) {
            label.setProperty("text", ts.call("get_locale"));
        }
    }

    @GodotMethod
    public void _on_Button_pressed() {
        org.godot.singleton.TranslationServer ts = org.godot.singleton.TranslationServer.singleton();
        org.godot.Godot label = (org.godot.Godot) call("get_node", "Label");

        String locale = (String) ts.call("get_locale");
        if (!"ar".equals(locale)) {
            ts.call("set_locale", "ar");
        } else {
            ts.call("set_locale", "en");
        }

        if (label != null) {
            label.setProperty("text", ts.call("get_locale"));
        }
    }
}
