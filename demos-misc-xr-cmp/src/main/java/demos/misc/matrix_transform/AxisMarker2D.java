package demos.misc.matrix_transform;

import org.godot.annotation.GodotClass;
import org.godot.math.Transform2D;
import org.godot.math.Vector2;
import org.godot.node.Line2D;
import org.godot.node.Node;
import org.godot.node.Node2D;

@GodotClass(name = "AxisMarker2D", parent = "Node2D")
public class AxisMarker2D extends Node2D {

    @Override
    public void _process(double delta) {
        Node child = getChild(0);
        if (!(child instanceof Line2D line)) return;

        Vector2 origin = getPosition();
        line.setPointPosition(1, origin);

        Node parent = getParent();
        if (parent instanceof Node2D markerParent) {
            Transform2D globalTransform = markerParent.getGlobalTransform();
            line.setTransform(globalTransform);
        }
    }
}
