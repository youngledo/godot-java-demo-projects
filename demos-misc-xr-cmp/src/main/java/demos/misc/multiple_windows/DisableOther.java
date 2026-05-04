package demos.misc.multiple_windows;

import org.godot.annotation.Export;
import org.godot.annotation.GodotClass;
import org.godot.annotation.GodotMethod;
import org.godot.node.BaseButton;

@GodotClass(name = "MWDisableOther", parent = "BaseButton")
public class DisableOther extends BaseButton {

    public static final long ENABLE_OTHERS_WHEN_ENABLED = 0;
    public static final long ENABLE_OTHERS_WHEN_DISABLED = 1;

    @Export
    public long behavior = ENABLE_OTHERS_WHEN_ENABLED;

    private org.godot.Godot[] othersNodes;
    private boolean initialized = false;

    @Override
    public void _ready() {
        if (initialized) return;
        initialized = true;

        // Get @export others from property
        Object othersObj = getProperty("others");
        // Others are set via the scene export
        boolean buttonPressed = (boolean) getProperty("button_pressed");
        boolean othersDisabled;
        if (behavior == ENABLE_OTHERS_WHEN_ENABLED) {
            othersDisabled = !buttonPressed;
        } else {
            othersDisabled = buttonPressed;
        }
        setOthersDisabled(othersDisabled);
    }

    @GodotMethod
    public void _toggled(boolean toggledOn) {
        if (behavior == ENABLE_OTHERS_WHEN_ENABLED) {
            setOthersDisabled(!toggledOn);
        } else {
            setOthersDisabled(toggledOn);
        }
    }

    private void setOthersDisabled(boolean disabled) {
        Object othersObj = getProperty("others");
        if (othersObj instanceof Object[]) {
            for (Object o : (Object[]) othersObj) {
                if (o instanceof org.godot.Godot) {
                    ((org.godot.Godot) o).setProperty("disabled", disabled);
                }
            }
        }
    }
}
