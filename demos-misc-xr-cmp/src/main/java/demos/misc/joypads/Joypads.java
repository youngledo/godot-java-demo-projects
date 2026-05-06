package demos.misc.joypads;

import org.godot.annotation.GodotClass;
import org.godot.annotation.GodotMethod;
import org.godot.math.Color;
import org.godot.node.Control;
import org.godot.node.Node;

@GodotClass(name = "Joypads", parent = "Control")
public class Joypads extends Control {

    private static final double DEADZONE = 0.2;
    private static final Color FONT_COLOR_DEFAULT = new Color(1.0, 1.0, 1.0, 0.5);
    private static final Color FONT_COLOR_ACTIVE = new Color(0.2, 1.0, 0.2, 1.0);

    private int joyNum = 0;
    private int curJoy = -1;
    private double axisValue = 0.0;

    private org.godot.node.Node axes;
    private org.godot.node.Node buttonGrid;
    private org.godot.node.Node joypadAxes;
    private org.godot.node.Node joypadButtons;
    private org.godot.node.Control joypadName;
    private org.godot.node.Node joypadNumber;
    private org.godot.node.Node remapWizard;
    private boolean initialized = false;

    @Override
    public void _ready() {
        if (initialized) return;
        initialized = true;

        axes = getNode("Axes");
        buttonGrid = getNode("Buttons/ButtonGrid");
        joypadAxes = getNode("JoypadDiagram/Axes");
        joypadButtons = getNode("JoypadDiagram/Buttons");
        joypadName = (org.godot.node.Control) getNode("DeviceInfo/JoyName");
        joypadNumber = getNode("DeviceInfo/JoyNumber");
        remapWizard = getNode("RemapWizard");

        org.godot.singleton.Input input = org.godot.singleton.Input.singleton();
        input.connect("joy_connection_changed", new org.godot.core.Callable(this, "_on_joy_connection_changed"), 0);

        Object[] joypads = (Object[]) input.call("get_connected_joypads");
        if (joypads != null) {
            for (Object joypad : joypads) {
                if (joypad instanceof Long) {
                    int id = ((Long) joypad).intValue();
                    String name = (String) input.getJoyName(id);
                    String guid = (String) input.getJoyGuid(id);
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
            String name = (String) input.getJoyName(joyNum);
            if (!name.isEmpty() ) {
                setJoypadName(name, (String) input.getJoyGuid(joyNum));
            } else {
                clearJoypadName();
            }
        }

        // Loop through axes
        int maxAxis = Math.min((int) (long) org.godot.singleton.Input.singleton().call("get_joy_axis_max"), 10);
        for (int axis = 0; axis < maxAxis; axis++) {
            axisValue = (double) org.godot.singleton.Input.singleton().getJoyAxis(joyNum, axis);

            org.godot.Godot progressBar = (org.godot.Godot) axes.getNode("Axis" + axis + "/ProgressBar");
            if (progressBar != null) progressBar.call("set_value", 100 * axisValue);

            org.godot.Godot valueLabel = (org.godot.Godot) axes.getNode("Axis" + axis + "/ProgressBar/Value");
            if (valueLabel != null) {
                valueLabel.setProperty("text", "[center][fade start=2 length=16]" + axisValue + "[/fade][/center]");
            }

            double scaledAlphaValue = (Math.abs(axisValue) - DEADZONE) / (1.0 - DEADZONE);

            // Show joypad direction indicators
            if (axis <= 3) { // JOY_AXIS_RIGHT_Y = 3
                org.godot.node.CanvasItem axisPlus = (org.godot.node.CanvasItem) joypadAxes.getNode(axis + "+");
                org.godot.node.CanvasItem axisMinus = (org.godot.node.CanvasItem) joypadAxes.getNode(axis + "-");
                if (Math.abs(axisValue) < DEADZONE) {
                    if (axisPlus != null) axisPlus.hide();
                    if (axisMinus != null) axisMinus.hide();
                } else if (axisValue > 0) {
                    if (axisPlus != null) { axisPlus.show(); axisPlus.setProperty("self_modulate", new Color(1, 1, 1, (float) scaledAlphaValue)); }
                    if (axisMinus != null) axisMinus.hide();
                } else {
                    if (axisPlus != null) axisPlus.hide();
                    if (axisMinus != null) { axisMinus.show(); axisMinus.setProperty("self_modulate", new Color(1, 1, 1, (float) scaledAlphaValue)); }
                }
            } else if (axis == 4 || axis == 5) { // TRIGGER_LEFT = 4, TRIGGER_RIGHT = 5
                org.godot.node.CanvasItem axisNode = (org.godot.node.CanvasItem) joypadAxes.getNode(String.valueOf(axis));
                if (axisValue <= DEADZONE) {
                    if (axisNode != null) axisNode.hide();
                } else {
                    if (axisNode != null) { axisNode.show(); axisNode.setProperty("self_modulate", new Color(1, 1, 1, (float) scaledAlphaValue)); }
                }
            }

            // Highlight axis labels
            org.godot.Godot label = (org.godot.Godot) axes.getNode("Axis" + axis + "/Label");
            if (label != null) {
                label.call("add_theme_color_override", "font_color",
                    Math.abs(axisValue) >= DEADZONE ? FONT_COLOR_ACTIVE : FONT_COLOR_DEFAULT);
            }
        }

        // Loop through buttons
        for (int button = 0; button < Math.min(21, 21); button++) {
            boolean pressed = (boolean) org.godot.singleton.Input.singleton().isJoyButtonPressed(joyNum, button);
            org.godot.Godot btnLabel = (org.godot.Godot) buttonGrid.getChild(button);
            if (btnLabel != null) {
                btnLabel.call("add_theme_color_override", "font_color",
                    pressed ? FONT_COLOR_ACTIVE : FONT_COLOR_DEFAULT);
            }
            if (button <= 15) { // JOY_BUTTON_MISC1 = 15
                org.godot.node.CanvasItem btnNode = (org.godot.node.CanvasItem) joypadButtons.getChild(button);
                if (btnNode != null) {
                    if (pressed) btnNode.show();
                    else btnNode.hide();
                }
            }
        }
    }

    @GodotMethod
    public void OnJoyConnectionChanged(long deviceId, boolean connected) {
        org.godot.singleton.Input input = org.godot.singleton.Input.singleton();
        if (connected) {
            System.out.println("+ Found newly connected joypad #" + deviceId + ": " +
                input.getJoyName(deviceId) + " - " + input.getJoyGuid(deviceId));
        } else {
            System.out.println("- Disconnected joypad #" + deviceId + ".");
        }

        if (deviceId == curJoy) {
            if (connected) {
                setJoypadName((String) input.getJoyName(deviceId),
                    (String) input.getJoyGuid(deviceId));
            } else {
                clearJoypadName();
            }
        }
    }

    @GodotMethod
    public void OnStartVibrationPressed() {
        org.godot.node.Label weak = (org.godot.node.Label) getNode("Vibration/Weak/Value");
        org.godot.node.Label strong = (org.godot.node.Label) getNode("Vibration/Strong/Value");
        org.godot.node.Label duration = (org.godot.node.Label) getNode("Vibration/Duration/Value");
        if (weak != null && strong != null && duration != null) {
            org.godot.singleton.Input.singleton().startJoyVibration(curJoy,
                ((Number) weak.getProperty("value")).doubleValue(),
                ((Number) strong.getProperty("value")).doubleValue(),
                ((Number) duration.getProperty("value")).doubleValue());
        }
    }

    @GodotMethod
    public void OnStopVibrationPressed() {
        org.godot.singleton.Input.singleton().stopJoyVibration(curJoy);
    }

    @GodotMethod
    public void OnRemapPressed() {
        if (remapWizard != null) remapWizard.call("start", curJoy);
    }

    @GodotMethod
    public void OnClearPressed() {
        String guid = (String) org.godot.singleton.Input.singleton().getJoyGuid(curJoy);
        if (guid == null || guid.isEmpty() ) {
            call("push_error", "No gamepad selected.");
            return;
        }
        org.godot.singleton.Input.singleton().removeJoyMapping(guid);
    }

    @GodotMethod
    public void OnShowPressed() {
        if (remapWizard != null) remapWizard.call("show_map");
    }

    @GodotMethod
    public void OnJoyNameMetaClicked(Object meta) {
        org.godot.singleton.OS.singleton().shellOpen(String.valueOf(meta));
    }

    private void setJoypadName(String name, String guid) {
        if (joypadName != null) {
            joypadName.call("set_text", name + "\n[color=#fff9][url=https://github.com/godotengine/godot/blob/master/core/input/gamecontrollerdb.txt]" + guid + "[/url][/color]");
        }

        // Make the rest of the UI appear as enabled
        String[] nodes = {"JoypadDiagram", "Axes", "Buttons", "Vibration", "VBoxContainer"};
        for (String nodeName : nodes) {
            org.godot.node.Node node = getNode(nodeName);
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
            org.godot.node.Node node = getNode(nodeName);
            if (node != null) {
                org.godot.math.Color modulate = (org.godot.math.Color) node.getProperty("modulate");
                if (modulate != null) {
                    node.setProperty("modulate", new org.godot.math.Color(modulate.r, modulate.g, modulate.b, 0.5));
                }
            }
        }
    }
}
