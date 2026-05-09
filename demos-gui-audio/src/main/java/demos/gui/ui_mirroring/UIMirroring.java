package demos.gui.ui_mirroring;

import org.godot.annotation.GodotClass;
import org.godot.annotation.GodotMethod;
import org.godot.node.Control;
import org.godot.node.Label;
import org.godot.singleton.TranslationServer;

@GodotClass(name = "UIMirroring", parent = "Control")
public class UIMirroring extends Control {

    private boolean initialized = false;

    @Override
    public void _ready() {
        if (initialized) return;
        initialized = true;

        updateLabel();
    }

    @GodotMethod
    public void OnButtonPressed() {
        TranslationServer ts = TranslationServer.singleton();
        ts.setLocale("ar".equals(ts.getLocale()) ? "en" : "ar");
        updateLabel();
    }

    private void updateLabel() {
        Label label = getNodeAs("Label", Label.class);
        if (label != null) {
            label.setText(TranslationServer.singleton().getLocale());
        }
    }
}
