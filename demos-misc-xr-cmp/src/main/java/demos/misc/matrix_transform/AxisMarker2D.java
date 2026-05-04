package demos.misc.matrix_transform;

import org.godot.annotation.GodotClass;
import org.godot.node.Node2D;

@GodotClass(name = "AxisMarker2D", parent = "Node2D")
public class AxisMarker2D extends Node2D {

    @Override
    public void _process(double delta) {
        org.godot.Godot line = (org.godot.Godot) call("get_child", 0);
        if (line == null) return;
        line = (org.godot.Godot) line.call("get_child", 0);
        if (line == null) return;

        org.godot.math.Vector2 origin = (org.godot.math.Vector2) getProperty("position");

        // Set second point of line to the marker's position
        line.call("set_point_position", 1, origin);

        org.godot.Godot markerParent = (org.godot.Godot) call("get_parent");
        if (markerParent != null && markerParent instanceof Node2D) {
            org.godot.math.Transform2D globalTransform = (org.godot.math.Transform2D) markerParent.getProperty("global_transform");
            line.setProperty("transform", globalTransform);
        }
    }
}
