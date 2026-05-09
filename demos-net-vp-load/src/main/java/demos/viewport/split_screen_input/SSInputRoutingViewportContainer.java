package demos.viewport.split_screen_input;

import org.godot.annotation.GodotClass;
import org.godot.annotation.GodotMethod;
import org.godot.node.InputEventJoypadButton;
import org.godot.node.InputEventKey;
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
        if (inputEvent instanceof InputEventKey event) {
            long keycode = event.getKeycode();
            for (int key : currentKeyboardSet) {
                if (key == keycode) return true;
            }
        } else if (inputEvent instanceof InputEventJoypadButton event) {
            long device = event.getDevice();
            return currentJoypadDevice > -1 && device == currentJoypadDevice;
        }
        return false;
    }
}
