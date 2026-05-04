package demos.gui.accessibility;

// BLOCKED: This demo relies heavily on _draw() for rendering and
// NOTIFICATION_ACCESSIBILITY_UPDATE / NOTIFICATION_ACCESSIBILITY_INVALIDATE
// notifications which are not available in godot-java. Cannot be ported.

import org.godot.annotation.GodotClass;
import org.godot.node.Control;

@GodotClass(name = "CustomControl", parent = "Control")
public class CustomControl extends Control {

    private static final int ITEM_COUNT = 3;
    private int selected = 0;
    private int[] itemValues = new int[ITEM_COUNT];
    private String[] itemNames = {"Item 1", "Item 2", "Item 3"};

    // _draw() is not available in godot-java - this demo is BLOCKED
    // The original GDScript uses _draw() for all visual rendering and
    // accessibility notifications for screen reader support.
}
