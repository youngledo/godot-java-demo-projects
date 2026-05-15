package demos.misc.joypads;

import org.godot.annotation.GodotClass;
import org.godot.annotation.GodotMethod;
import org.godot.core.Callable;
import org.godot.math.Color;
import org.godot.node.CanvasItem;
import org.godot.node.Control;
import org.godot.node.Node;
import org.godot.node.ProgressBar;
import org.godot.node.Range;
import org.godot.node.RichTextLabel;
import org.godot.singleton.Input;
import org.godot.singleton.OS;

@GodotClass(name = "Joypads", parent = "Control")
public class Joypads extends Control {

    private static final double DEADZONE = 0.2;
    private static final Color FONT_COLOR_DEFAULT = new Color(1.0, 1.0, 1.0, 0.5);
    private static final Color FONT_COLOR_ACTIVE = new Color(0.2, 1.0, 0.2, 1.0);

    private int joyNum = 0;
    private int curJoy = -1;
    private double axisValue = 0.0;

    private Node axes;
    private Node buttonGrid;
    private Node joypadAxes;
    private Node joypadButtons;
    private RichTextLabel joypadName;
    private Range joypadNumber;
    private RemapWizard remapWizard;
    private boolean initialized = false;

    @Override
    public void _ready() {
        if (initialized) return;
        initialized = true;

        axes = getNode("Axes");
        buttonGrid = getNode("Buttons/ButtonGrid");
        joypadAxes = getNode("JoypadDiagram/Axes");
        joypadButtons = getNode("JoypadDiagram/Buttons");
        joypadName = getNodeAs("DeviceInfo/JoyName", RichTextLabel.class);
        joypadNumber = getNodeAs("DeviceInfo/JoyNumber", Range.class);
        remapWizard = getNodeAs("RemapWizard", RemapWizard.class);

        Input input = Input.singleton();
        input.connect("joy_connection_changed", new Callable(this, "OnJoyConnectionChanged"), 0);

        org.godot.collection.GodotArray<Long> joypads = input.getConnectedJoypads();
        for (int i = 0; i < joypads.size(); i++) {
            int id = joypads.get(i).intValue();
            String name = input.getJoyName(id);
            String guid = input.getJoyGuid(id);
            System.out.println("Found joypad #" + id + ": " + name + " - " + guid);
        }
    }

    @Override
    public void _process(double delta) {
        if (joypadNumber == null) return;

        joyNum = (int) joypadNumber.getValue();

        if (joyNum != curJoy) {
            curJoy = joyNum;
            Input input = Input.singleton();
            String name = input.getJoyName(joyNum);
            if (!name.isEmpty()) {
                setJoypadName(name, input.getJoyGuid(joyNum));
            } else {
                clearJoypadName();
            }
        }

        for (int axis = 0; axis < 10; axis++) {
            axisValue = Input.singleton().getJoyAxis(joyNum, axis);

            ProgressBar progressBar = axes != null ? axes.getNodeAs("Axis" + axis + "/ProgressBar", ProgressBar.class) : null;
            if (progressBar != null) progressBar.setValue(100 * axisValue);

            RichTextLabel valueLabel = axes != null ? axes.getNodeAs("Axis" + axis + "/ProgressBar/Value", RichTextLabel.class) : null;
            if (valueLabel != null) {
                valueLabel.setText("[center][fade start=2 length=16]" + axisValue + "[/fade][/center]");
            }

            double scaledAlphaValue = (Math.abs(axisValue) - DEADZONE) / (1.0 - DEADZONE);

            if (axis <= 3) {
                CanvasItem axisPlus = joypadAxes != null ? joypadAxes.getNodeAs(axis + "+", CanvasItem.class) : null;
                CanvasItem axisMinus = joypadAxes != null ? joypadAxes.getNodeAs(axis + "-", CanvasItem.class) : null;
                if (Math.abs(axisValue) < DEADZONE) {
                    if (axisPlus != null) axisPlus.hide();
                    if (axisMinus != null) axisMinus.hide();
                } else if (axisValue > 0) {
                    if (axisPlus != null) { axisPlus.show(); axisPlus.setSelfModulate(new Color(1, 1, 1, scaledAlphaValue)); }
                    if (axisMinus != null) axisMinus.hide();
                } else {
                    if (axisPlus != null) axisPlus.hide();
                    if (axisMinus != null) { axisMinus.show(); axisMinus.setSelfModulate(new Color(1, 1, 1, scaledAlphaValue)); }
                }
            } else if (axis == 4 || axis == 5) {
                CanvasItem axisNode = joypadAxes != null ? joypadAxes.getNodeAs(String.valueOf(axis), CanvasItem.class) : null;
                if (axisValue <= DEADZONE) {
                    if (axisNode != null) axisNode.hide();
                } else {
                    if (axisNode != null) { axisNode.show(); axisNode.setSelfModulate(new Color(1, 1, 1, scaledAlphaValue)); }
                }
            }

            Control label = axes != null ? axes.getNodeAs("Axis" + axis + "/Label", Control.class) : null;
            if (label != null) {
                label.addThemeColorOverride("font_color",
                    Math.abs(axisValue) >= DEADZONE ? FONT_COLOR_ACTIVE : FONT_COLOR_DEFAULT);
            }
        }

        for (int button = 0; button < 21; button++) {
            boolean pressed = Input.singleton().isJoyButtonPressed(joyNum, button);
            Node child = buttonGrid != null ? buttonGrid.getChild(button) : null;
            if (child instanceof Control btnLabel) {
                btnLabel.addThemeColorOverride("font_color", pressed ? FONT_COLOR_ACTIVE : FONT_COLOR_DEFAULT);
            }
            if (button <= 15) {
                Node buttonChild = joypadButtons != null ? joypadButtons.getChild(button) : null;
                if (buttonChild instanceof CanvasItem btnNode) {
                    if (pressed) btnNode.show();
                    else btnNode.hide();
                }
            }
        }
    }

    @GodotMethod
    public void OnJoyConnectionChanged(long deviceId, boolean connected) {
        Input input = Input.singleton();
        int devId = (int) deviceId;
        if (connected) {
            System.out.println("+ Found newly connected joypad #" + deviceId + ": " +
                input.getJoyName(devId) + " - " + input.getJoyGuid(devId));
        } else {
            System.out.println("- Disconnected joypad #" + deviceId + ".");
        }

        if (devId == curJoy) {
            if (connected) {
                setJoypadName(input.getJoyName(devId), input.getJoyGuid(devId));
            } else {
                clearJoypadName();
            }
        }
    }

    @GodotMethod
    public void OnStartVibrationPressed() {
        Range weak = getNodeAs("Vibration/Weak/Value", Range.class);
        Range strong = getNodeAs("Vibration/Strong/Value", Range.class);
        Range duration = getNodeAs("Vibration/Duration/Value", Range.class);
        if (weak != null && strong != null && duration != null) {
            Input.singleton().startJoyVibration(curJoy, weak.getValue(), strong.getValue(), duration.getValue());
        }
    }

    @GodotMethod
    public void OnStopVibrationPressed() {
        Input.singleton().stopJoyVibration(curJoy);
    }

    @GodotMethod
    public void OnRemapPressed() {
        if (remapWizard != null) remapWizard.start(curJoy);
    }

    @GodotMethod
    public void OnClearPressed() {
        String guid = Input.singleton().getJoyGuid(curJoy);
        if (guid == null || guid.isEmpty()) {
            System.err.println("No gamepad selected.");
            return;
        }
        Input.singleton().removeJoyMapping(guid);
    }

    @GodotMethod
    public void OnShowPressed() {
        if (remapWizard != null) remapWizard.showMap();
    }

    @GodotMethod
    public void OnJoyNameMetaClicked(Object meta) {
        OS.singleton().shellOpen(String.valueOf(meta));
    }

    private void setJoypadName(String name, String guid) {
        if (joypadName != null) {
            joypadName.setText(name + "\n[color=#fff9][url=https://github.com/godotengine/godot/blob/master/core/input/gamecontrollerdb.txt]" + guid + "[/url][/color]");
        }

        setUiEnabled(true);
    }

    private void clearJoypadName() {
        if (joypadName != null && joypadNumber != null) {
            joypadName.setText("[i]No controller detected at ID " + joypadNumber.getValue() + ".[/i]");
        }

        setUiEnabled(false);
    }

    private void setUiEnabled(boolean enabled) {
        String[] nodes = {"JoypadDiagram", "Axes", "Buttons", "Vibration", "VBoxContainer"};
        for (String nodeName : nodes) {
            CanvasItem node = getNodeAs(nodeName, CanvasItem.class);
            if (node != null) {
                Color modulate = node.getModulate();
                node.setModulate(new Color(modulate.r, modulate.g, modulate.b, enabled ? 1.0 : 0.5));
            }
        }
    }
}
