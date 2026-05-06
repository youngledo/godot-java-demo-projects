package demos.gui.ui_mirroring;

import org.godot.annotation.GodotClass;
import org.godot.annotation.GodotMethod;
import org.godot.node.Control;
import org.godot.node.Node;

@GodotClass(name = "UIMirroring", parent = "Control")
public class UIMirroring extends Control {

    private boolean initialized = false;

    @Override
    public void _ready() {
        if (initialized) return;
        initialized = true;

        org.godot.singleton.TranslationServer ts = org.godot.singleton.TranslationServer.singleton();
        org.godot.node.Label label = (org.godot.node.Label) getNode("Label");
        if (label != null) {
            label.setProperty("text", ts.call("get_locale"));
        }
    }

    @GodotMethod
    public void OnButtonPressed() {
        org.godot.singleton.TranslationServer ts = org.godot.singleton.TranslationServer.singleton();
        org.godot.node.Label label = (org.godot.node.Label) getNode("Label");

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
