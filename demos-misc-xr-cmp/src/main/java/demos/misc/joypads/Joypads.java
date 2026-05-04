package demos.misc.joypads;

import org.godot.annotation.GodotClass;
import org.godot.annotation.GodotMethod;
import org.godot.math.Color;
import org.godot.node.Control;

@GodotClass(name = "Joypads", parent = "Control")
public class Joypads extends Control {

    private static final double DEADZONE = 0.2;
    private static final Color FONT_COLOR_DEFAULT = new Color(1.0f, 1.0f, 1.0f, 0.5f);
    private static final Color FONT_COLOR_ACTIVE = new Color(0.2f, 1.0f, 0.2f, 1.0f);

    private int joyNum = 0;
    private int curJoy = -1;
    private double axisValue = 0.0;

    private org.godot.Godot axes;
    private org.godot.Godot buttonGrid;
    private org.godot.Godot joypadAxes;
    private org.godot.Godot joypadButtons;
    private org.godot.Godot joypadName;
    private org.godot.Godot joypadNumber;
    private org.godot.Godot remapWizard;
    private boolean initialized = false;

    @Override
    public void _ready() {
        if (initialized) return;
        initialized = true;

        axes = (org.godot.Godot) call("get_node", "Axes");
        buttonGrid = (org.godot.Godot) call("get_node", "Buttons/ButtonGrid");
        joypadAxes = (org.godot.Godot) call("get_node", "JoypadDiagram/Axes");
        joypadButtons = (org.godot.Godot) call("get_node", "JoypadDiagram/Buttons");
        joypadName = (org.godot.Godot) call("get_node", "DeviceInfo/JoyName");
        joypadNumber = (org.godot.Godot) call("get_node", "DeviceInfo/JoyNumber");
        remapWizard = (org.godot.Godot) call("get_node", "RemapWizard");

        org.godot.singleton.Input input = org.godot.singleton.Input.singleton();
        input.connect("joy_connection_changed",
            new org.godot.core.Callable(this, "_on_joy_connection_changed"), 0);

        Object[] joypads = (Object[]) input.call("get_connected_joypads");
        if (joypads != null) {
            for (Object joypad : joypads) {
                if (joypad instanceof Long) {
                    int id = ((Long) joypad).intValue();
                    String name = (String) input.call("get_joy_name", id);
                    String guid = (String) input.call("get_joy_guid", id);
                    System.out.println("Found joypad #" + id + ": " + name + " - " + guid);
                }
            }
        }
    }

    @Override
    public void _process(double delta) {
        if (joypadNumber == null) return;

        joyNum = (int) (long) joypadNumber.getProperty("value");

        if (joyNum != curJoy) {
            curJoy = joyNum;
            org.godot.singleton.Input input = org.godot.singleton.Input.singleton();
            String name = (String) input.call("get_joy_name", joyNum);
            if (!name.isEmpty()) {
                setJoypadName(name, (String) input.call("get_joy_guid", joyNum));
            } else {
                clearJoypadName();
            }
        }

        // Loop through axes
        int maxAxis = Math.min((int) (long) org.godot.singleton.Input.singleton().call("get_joy_axis_max"), 10);
        for (int axis = 0; axis < maxAxis; axis++) {
            axisValue = (double) org.godot.singleton.Input.singleton().call("get_joy_axis", joyNum, axis);

            org.godot.Godot progressBar = (org.godot.Godot) axes.call("get_node", "Axis" + axis + "/ProgressBar");
            if (progressBar != null) progressBar.call("set_value", 100 * axisValue);

            org.godot.Godot valueLabel = (org.godot.Godot) axes.call("get_node", "Axis" + axis + "/ProgressBar/Value");
            if (valueLabel != null) {
                valueLabel.setProperty("text", "[center][fade start=2 length=16]" + axisValue + "[/fade][/center]");
            }

            double scaledAlphaValue = (Math.abs(axisValue) - DEADZONE) / (1.0 - DEADZONE);

            // Show joypad direction indicators
            if (axis <= 3) { // JOY_AXIS_RIGHT_Y = 3
                org.godot.Godot axisPlus = (org.godot.Godot) joypadAxes.call("get_node", axis + "+");
                org.godot.Godot axisMinus = (org.godot.Godot) joypadAxes.call("get_node", axis + "-");
                if (Math.abs(axisValue) < DEADZONE) {
                    if (axisPlus != null) axisPlus.call("hide");
                    if (axisMinus != null) axisMinus.call("hide");
                } else if (axisValue > 0) {
                    if (axisPlus != null) { axisPlus.call("show"); axisPlus.setProperty("self_modulate", new Color(1, 1, 1, (float) scaledAlphaValue)); }
                    if (axisMinus != null) axisMinus.call("hide");
                } else {
                    if (axisPlus != null) axisPlus.call("hide");
                    if (axisMinus != null) { axisMinus.call("show"); axisMinus.setProperty("self_modulate", new Color(1, 1, 1, (float) scaledAlphaValue)); }
                }
            } else if (axis == 4 || axis == 5) { // TRIGGER_LEFT = 4, TRIGGER_RIGHT = 5
                org.godot.Godot axisNode = (org.godot.Godot) joypadAxes.call("get_node", String.valueOf(axis));
                if (axisValue <= DEADZONE) {
                    if (axisNode != null) axisNode.call("hide");
                } else {
                    if (axisNode != null) { axisNode.call("show"); axisNode.setProperty("self_modulate", new Color(1, 1, 1, (float) scaledAlphaValue)); }
                }
            }

            // Highlight axis labels
            org.godot.Godot label = (org.godot.Godot) axes.call("get_node", "Axis" + axis + "/Label");
            if (label != null) {
                label.call("add_theme_color_override", "font_color",
                    Math.abs(axisValue) >= DEADZONE ? FONT_COLOR_ACTIVE : FONT_COLOR_DEFAULT);
            }
        }

        // Loop through buttons
        for (int button = 0; button < Math.min(21, 21); button++) {
            boolean pressed = (boolean) org.godot.singleton.Input.singleton().call("is_joy_button_pressed", joyNum, button);
            org.godot.Godot btnLabel = (org.godot.Godot) buttonGrid.call("get_child", button);
            if (btnLabel != null) {
                btnLabel.call("add_theme_color_override", "font_color",
                    pressed ? FONT_COLOR_ACTIVE : FONT_COLOR_DEFAULT);
            }
            if (button <= 15) { // JOY_BUTTON_MISC1 = 15
                org.godot.Godot btnNode = (org.godot.Godot) joypadButtons.call("get_child", button);
                if (btnNode != null) {
                    if (pressed) btnNode.call("show");
                    else btnNode.call("hide");
                }
            }
        }
    }

    @GodotMethod
    public void _on_joy_connection_changed(long deviceId, boolean connected) {
        org.godot.singleton.Input input = org.godot.singleton.Input.singleton();
        if (connected) {
            System.out.println("+ Found newly connected joypad #" + deviceId + ": " +
                input.call("get_joy_name", deviceId) + " - " + input.call("get_joy_guid", deviceId));
        } else {
            System.out.println("- Disconnected joypad #" + deviceId + ".");
        }

        if (deviceId == curJoy) {
            if (connected) {
                setJoypadName((String) input.call("get_joy_name", deviceId),
                    (String) input.call("get_joy_guid", deviceId));
            } else {
                clearJoypadName();
            }
        }
    }

    @GodotMethod
    public void _on_start_vibration_pressed() {
        org.godot.Godot weak = (org.godot.Godot) call("get_node", "Vibration/Weak/Value");
        org.godot.Godot strong = (org.godot.Godot) call("get_node", "Vibration/Strong/Value");
        org.godot.Godot duration = (org.godot.Godot) call("get_node", "Vibration/Duration/Value");
        if (weak != null && strong != null && duration != null) {
            org.godot.singleton.Input.singleton().call("start_joy_vibration", curJoy,
                ((Number) weak.getProperty("value")).doubleValue(),
                ((Number) strong.getProperty("value")).doubleValue(),
                ((Number) duration.getProperty("value")).doubleValue());
        }
    }

    @GodotMethod
    public void _on_stop_vibration_pressed() {
        org.godot.singleton.Input.singleton().call("stop_joy_vibration", curJoy);
    }

    @GodotMethod
    public void _on_Remap_pressed() {
        if (remapWizard != null) remapWizard.call("start", curJoy);
    }

    @GodotMethod
    public void _on_Clear_pressed() {
        String guid = (String) org.godot.singleton.Input.singleton().call("get_joy_guid", curJoy);
        if (guid == null || guid.isEmpty()) {
            call("push_error", "No gamepad selected.");
            return;
        }
        org.godot.singleton.Input.singleton().call("remove_joy_mapping", guid);
    }

    @GodotMethod
    public void _on_Show_pressed() {
        if (remapWizard != null) remapWizard.call("show_map");
    }

    @GodotMethod
    public void _on_joy_name_meta_clicked(Object meta) {
        org.godot.singleton.OS.singleton().call("shell_open", String.valueOf(meta));
    }

    private void setJoypadName(String name, String guid) {
        if (joypadName != null) {
            joypadName.call("set_text", name + "\n[color=#fff9][url=https://github.com/godotengine/godot/blob/master/core/input/gamecontrollerdb.txt]" + guid + "[/url][/color]");
        }

        // Make the rest of the UI appear as enabled
        String[] nodes = {"JoypadDiagram", "Axes", "Buttons", "Vibration", "VBoxContainer"};
        for (String nodeName : nodes) {
            org.godot.Godot node = (org.godot.Godot) call("get_node", nodeName);
            if (node != null) {
                org.godot.math.Color modulate = (org.godot.math.Color) node.getProperty("modulate");
                if (modulate != null) {
                    node.setProperty("modulate", new org.godot.math.Color(modulate.r, modulate.g, modulate.b, 1.0));
                }
            }
        }
    }

    private void clearJoypadName() {
        if (joypadName != null && joypadNumber != null) {
            joypadName.call("set_text", "[i]No controller detected at ID " + joypadNumber.getProperty("value") + ".[/i]");
        }

        // Make the rest of the UI appear as disabled
        String[] nodes = {"JoypadDiagram", "Axes", "Buttons", "Vibration", "VBoxContainer"};
        for (String nodeName : nodes) {
            org.godot.Godot node = (org.godot.Godot) call("get_node", nodeName);
            if (node != null) {
                org.godot.math.Color modulate = (org.godot.math.Color) node.getProperty("modulate");
                if (modulate != null) {
                    node.setProperty("modulate", new org.godot.math.Color(modulate.r, modulate.g, modulate.b, 0.5));
                }
            }
        }
    }
}
