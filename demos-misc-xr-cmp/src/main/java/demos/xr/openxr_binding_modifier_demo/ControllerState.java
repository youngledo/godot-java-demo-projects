package demos.xr.openxr_binding_modifier_demo;

import org.godot.annotation.GodotClass;
import org.godot.node.CheckBox;
import org.godot.node.Control;
import org.godot.node.Label;
import org.godot.node.Slider;
import org.godot.node.XRController3D;

@GodotClass(name = "ControllerState", parent = "Control")
public class ControllerState extends Control {

    private XRController3D controller;
    private Slider triggerInputNode;
    private CheckBox triggerClickNode;
    private Label onThresholdNode;
    private Label offThresholdNode;
    private CheckBox dpadUpNode;
    private CheckBox dpadDownNode;
    private CheckBox dpadLeftNode;
    private CheckBox dpadRightNode;

    private double offTriggerThreshold = 1.0;
    private double onTriggerThreshold = 0.0;

    private boolean initialized = false;

    @Override
    public void _ready() {
        if (initialized) return;
        initialized = true;

        Object controllerValue = getProperty("controller");
        if (controllerValue instanceof XRController3D xrController) {
            controller = xrController;
        }

        triggerInputNode = getNodeAs("VBoxContainer/TriggerInput/HSlider", Slider.class);
        triggerClickNode = getNodeAs("VBoxContainer/TriggerInput/CheckBox", CheckBox.class);
        onThresholdNode = getNodeAs("VBoxContainer/Thresholds/OnThreshold", Label.class);
        offThresholdNode = getNodeAs("VBoxContainer/Thresholds/OffThreshold", Label.class);

        dpadUpNode = getNodeAs("VBoxContainer/DPadState/Up", CheckBox.class);
        dpadDownNode = getNodeAs("VBoxContainer/DPadState/Down", CheckBox.class);
        dpadLeftNode = getNodeAs("VBoxContainer/DPadState/Left", CheckBox.class);
        dpadRightNode = getNodeAs("VBoxContainer/DPadState/Right", CheckBox.class);
    }

    @Override
    public void _process(double delta) {
        if (controller == null) return;

        double triggerInput = controller.getFloat("trigger");
        if (triggerInputNode != null) triggerInputNode.setValue(triggerInput);

        boolean triggerClick = controller.isButtonPressed("trigger_click");
        if (triggerClickNode != null) triggerClickNode.setButtonPressed(triggerClick);

        if (triggerClick) {
            offTriggerThreshold = Math.min(offTriggerThreshold, triggerInput);
        } else {
            onTriggerThreshold = Math.max(onTriggerThreshold, triggerInput);
        }

        if (onThresholdNode != null) {
            onThresholdNode.setText(String.format("On: %.2f", onTriggerThreshold));
        }
        if (offThresholdNode != null) {
            offThresholdNode.setText(String.format("Off: %.2f", offTriggerThreshold));
        }

        boolean dpadUp = controller.isButtonPressed("up");
        boolean dpadDown = controller.isButtonPressed("down");
        boolean dpadLeft = controller.isButtonPressed("left");
        boolean dpadRight = controller.isButtonPressed("right");

        if (dpadUpNode != null) dpadUpNode.setButtonPressed(dpadUp);
        if (dpadDownNode != null) dpadDownNode.setButtonPressed(dpadDown);
        if (dpadLeftNode != null) dpadLeftNode.setButtonPressed(dpadLeft);
        if (dpadRightNode != null) dpadRightNode.setButtonPressed(dpadRight);
    }
}
