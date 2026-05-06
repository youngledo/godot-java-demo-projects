package demos.threed.lights_and_shadows;

import org.godot.annotation.GodotClass;
import org.godot.math.Vector3;
import org.godot.node.Node3D;
import org.godot.node.Node;

@GodotClass(name = "Spin", parent = "Node3D")
public class Spin extends Node3D {

	private double increment = 0.0;

	@Override
	public void _process(double delta) {
		double x = Math.sin(increment);
		double z = Math.cos(increment);
		setProperty("position", new Vector3(x, 0, z));
		setProperty("rotation", new Vector3(0, increment % (2 * Math.PI), 0));
		increment += delta;
	}
}
