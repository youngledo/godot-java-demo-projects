package demos.xr.openxr_binding_modifier_demo;

import org.godot.annotation.GodotClass;
import org.godot.node.Control;
import org.godot.node.Node;

@GodotClass(name = "ControllerState", parent = "Control")
public class ControllerState extends Control {

    private org.godot.Godot controller;
    private org.godot.node.Slider triggerInputNode;
    private org.godot.node.CheckBox triggerClickNode;
    private org.godot.node.Node onThresholdNode;
    private org.godot.node.Node offThresholdNode;
    private org.godot.node.Node dpadUpNode;
    private org.godot.node.Node dpadDownNode;
    private org.godot.node.Node dpadLeftNode;
    private org.godot.node.Node dpadRightNode;

    private double offTriggerThreshold = 1.0;
    private double onTriggerThreshold = 0.0;

    private boolean initialized = false;

    @Override
    public void _ready() {
        if (initialized) return;
        initialized = true;

        // controller is set as an exported property from the scene
        controller = (org.godot.Godot) getProperty("controller");

        triggerInputNode = (org.godot.node.Slider) getNode("VBoxContainer/TriggerInput/HSlider");
        triggerClickNode = (org.godot.node.CheckBox) getNode("VBoxContainer/TriggerInput/CheckBox");
        onThresholdNode = getNode("VBoxContainer/Thresholds/OnThreshold");
        offThresholdNode = getNode("VBoxContainer/Thresholds/OffThreshold");

        dpadUpNode = getNode("VBoxContainer/DPadState/Up");
        dpadDownNode = getNode("VBoxContainer/DPadState/Down");
        dpadLeftNode = getNode("VBoxContainer/DPadState/Left");
        dpadRightNode = getNode("VBoxContainer/DPadState/Right");
    }

    @Override
    public void _process(double delta) {
        if (controller == null) return;

        double triggerInput = (double) controller.call("get_float", "trigger");
        if (triggerInputNode != null) triggerInputNode.setProperty("value", triggerInput);

        boolean triggerClick = (boolean) controller.call("is_button_pressed", "trigger_click");
        if (triggerClickNode != null) triggerClickNode.setProperty("button_pressed", triggerClick);

        if (triggerClick) {
            offTriggerThreshold = Math.min(offTriggerThreshold, triggerInput);
        } else {
            onTriggerThreshold = Math.max(onTriggerThreshold, triggerInput);
        }

        if (onThresholdNode != null) {
            onThresholdNode.setProperty("text", String.format("On: %.2f", onTriggerThreshold));
        }
        if (offThresholdNode != null) {
            offThresholdNode.setProperty("text", String.format("Off: %.2f", offTriggerThreshold));
        }

        boolean dpadUp = (boolean) controller.call("is_button_pressed", "up");
        boolean dpadDown = (boolean) controller.call("is_button_pressed", "down");
        boolean dpadLeft = (boolean) controller.call("is_button_pressed", "left");
        boolean dpadRight = (boolean) controller.call("is_button_pressed", "right");

        if (dpadUpNode != null) dpadUpNode.setProperty("button_pressed", dpadUp);
        if (dpadDownNode != null) dpadDownNode.setProperty("button_pressed", dpadDown);
        if (dpadLeftNode != null) dpadLeftNode.setProperty("button_pressed", dpadLeft);
        if (dpadRightNode != null) dpadRightNode.setProperty("button_pressed", dpadRight);
    }
}
