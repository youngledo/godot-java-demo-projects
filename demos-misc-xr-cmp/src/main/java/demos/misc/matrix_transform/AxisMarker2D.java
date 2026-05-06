package demos.misc.matrix_transform;

import org.godot.annotation.GodotClass;
import org.godot.node.Node2D;
import org.godot.node.Node;

@GodotClass(name = "AxisMarker2D", parent = "Node2D")
public class AxisMarker2D extends Node2D {

    @Override
    public void _process(double delta) {
        org.godot.node.Line2D line = (org.godot.node.Line2D) call("get_child", 0);
        if (line == null) return;

        org.godot.math.Vector2 origin = (org.godot.math.Vector2) getProperty("position");

        // Set second point of line to the marker's position
        line.setPointPosition(1, origin);

        org.godot.Godot markerParent = (org.godot.Godot) getParent();
        if (markerParent != null && markerParent instanceof Node2D) {
            org.godot.math.Transform2D globalTransform = (org.godot.math.Transform2D) markerParent.getProperty("global_transform");
            line.setProperty("transform", globalTransform);
        }
    }
}
