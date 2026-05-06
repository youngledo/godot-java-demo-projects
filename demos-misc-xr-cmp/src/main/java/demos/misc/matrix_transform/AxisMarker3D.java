package demos.misc.matrix_transform;

import org.godot.annotation.GodotClass;
import org.godot.math.Transform3D;
import org.godot.math.Vector3;
import org.godot.node.Node3D;
import org.godot.node.Node;

@GodotClass(name = "AxisMarker3D", parent = "Node3D")
public class AxisMarker3D extends Node3D {

    @Override
    public void _process(double delta) {
        org.godot.node.Node origin = getChild(0);
        if (origin == null) return;
        org.godot.node.Node3D holder = (org.godot.node.Node3D) origin.getChild(0);
        if (holder == null) return;
        org.godot.Godot cube = (org.godot.Godot) holder.getChild(0);
        if (cube == null) return;

        Vector3 pos = (Vector3) getProperty("position");
        // "Hide" the origin vector if the AxisMarker is at (0, 0, 0)
        if (pos != null && pos.getX() == 0.0 && pos.getY() == 0.0 && pos.getZ() == 0.0) {
            holder.setProperty("transform", new Transform3D());
            Transform3D scaled = Transform3D.scaled(new Vector3(0.0001, 0.0001, 0.0001));
            cube.setProperty("transform", scaled);
            return;
        }

        if (pos != null) {
            Vector3 halfPos = new Vector3(pos.getX() / 2.0, pos.getY() / 2.0, pos.getZ() / 2.0);
            holder.setProperty("transform", new Transform3D(new org.godot.math.Basis(), halfPos));
            holder.lookAt(pos, Vector3.UP);

            org.godot.Godot parent = (org.godot.Godot) getParent();
            if (parent != null) {
                Transform3D parentGlobal = (Transform3D) parent.getProperty("global_transform");
                Transform3D holderTransform = (Transform3D) holder.getProperty("transform");
                holder.setProperty("transform", parentGlobal.multiply(holderTransform));
            }

            double length = pos.length();
            Transform3D cubeTransform = new Transform3D(
                org.godot.math.Basis.diagonal(new Vector3(0.1, 0.1, length)), new Vector3());
            cube.setProperty("transform", cubeTransform);
        }
    }
}
