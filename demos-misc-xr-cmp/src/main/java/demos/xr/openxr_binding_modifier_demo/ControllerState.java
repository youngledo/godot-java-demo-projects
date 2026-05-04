package demos.xr.openxr_binding_modifier_demo;

import org.godot.annotation.GodotClass;
import org.godot.node.Control;

@GodotClass(name = "ControllerState", parent = "Control")
public class ControllerState extends Control {

    private org.godot.Godot controller;
    private org.godot.Godot triggerInputNode;
    private org.godot.Godot triggerClickNode;
    private org.godot.Godot onThresholdNode;
    private org.godot.Godot offThresholdNode;
    private org.godot.Godot dpadUpNode;
    private org.godot.Godot dpadDownNode;
    private org.godot.Godot dpadLeftNode;
    private org.godot.Godot dpadRightNode;

    private double offTriggerThreshold = 1.0;
    private double onTriggerThreshold = 0.0;

    private boolean initialized = false;

    @Override
    public void _ready() {
        if (initialized) return;
        initialized = true;

        // controller is set as an exported property from the scene
        controller = (org.godot.Godot) getProperty("controller");

        triggerInputNode = (org.godot.Godot) call("get_node", "VBoxContainer/TriggerInput/HSlider");
        triggerClickNode = (org.godot.Godot) call("get_node", "VBoxContainer/TriggerInput/CheckBox");
        onThresholdNode = (org.godot.Godot) call("get_node", "VBoxContainer/Thresholds/OnThreshold");
        offThresholdNode = (org.godot.Godot) call("get_node", "VBoxContainer/Thresholds/OffThreshold");

        dpadUpNode = (org.godot.Godot) call("get_node", "VBoxContainer/DPadState/Up");
        dpadDownNode = (org.godot.Godot) call("get_node", "VBoxContainer/DPadState/Down");
        dpadLeftNode = (org.godot.Godot) call("get_node", "VBoxContainer/DPadState/Left");
        dpadRightNode = (org.godot.Godot) call("get_node", "VBoxContainer/DPadState/Right");
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
