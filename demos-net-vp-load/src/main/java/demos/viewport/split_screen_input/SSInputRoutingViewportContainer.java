package demos.viewport.split_screen_input;

import org.godot.annotation.GodotClass;
import org.godot.annotation.GodotMethod;
import org.godot.node.SubViewportContainer;

@GodotClass(name = "SSInputRoutingViewportContainer", parent = "SubViewportContainer")
public class SSInputRoutingViewportContainer extends SubViewportContainer {

    private int[] currentKeyboardSet = new int[0];
    private int currentJoypadDevice = -1;

    public void setInputConfig(int[] keyboardSet, int joypadDevice) {
        this.currentKeyboardSet = keyboardSet;
        this.currentJoypadDevice = joypadDevice;
    }

    @GodotMethod
    public boolean PropagateInputEvent(Object inputEvent) {
        if (inputEvent instanceof org.godot.Godot) {
            org.godot.Godot evt = (org.godot.Godot) inputEvent;
            String className = (String) evt.call("get_class");

            if ("InputEventKey".equals(className)) {
                int keycode = ((Number) evt.getProperty("keycode")).intValue();
                for (int key : currentKeyboardSet) {
                    if (key == keycode) return true;
                }
            } else if ("InputEventJoypadButton".equals(className)) {
                int device = ((Number) evt.getProperty("device")).intValue();
                if (currentJoypadDevice > -1 && device == currentJoypadDevice) {
                    return true;
                }
            }
        }
        return false;
    }
}
