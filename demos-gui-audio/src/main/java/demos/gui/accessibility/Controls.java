package demos.gui.accessibility;

import org.godot.annotation.GodotClass;
import org.godot.annotation.GodotMethod;
import org.godot.node.Control;
import org.godot.node.Label;
import org.godot.node.LineEdit;
import org.godot.singleton.DisplayServer;

@GodotClass(name = "Controls", parent = "Control")
public class Controls extends Control {
    private boolean initialized = false;

    @Override
    public void _ready() {
        if (initialized) return;
        initialized = true;

        Label labelRegion = getNodeAs("Panel/LabelRegion", Label.class);
        if (labelRegion != null) {
            labelRegion.setAccessibilityLive(DisplayServer.AccessibilityLiveMode.LIVE_POLITE.value);
        }

        LineEdit lineEditName = getNodeAs("LineEditName", LineEdit.class);
        if (lineEditName != null && lineEditName.isInsideTree()) {
            lineEditName.grabFocus();
        }
    }

    @GodotMethod
    public void _on_button_set_pressed() {
        updateLiveRegion();
    }

    @GodotMethod
    public void OnButtonSetPressed() {
        updateLiveRegion();
    }

    private void updateLiveRegion() {
        if (!isInsideTree()) return;

        LineEdit liveReg = getNodeAs("LineEditLiveReg", LineEdit.class);
        Label labelRegion = getNodeAs("Panel/LabelRegion", Label.class);
        if (liveReg != null && labelRegion != null) {
            labelRegion.setText(liveReg.getText());
            labelRegion.queueAccessibilityUpdate();
        }
    }
}
