package demos.threed.lights_and_shadows;

import org.godot.annotation.GodotClass;
import org.godot.math.Vector3;
import org.godot.node.DirectionalLight3D;

@GodotClass(name = "DayNightCycle", parent = "DirectionalLight3D")
public class DayNightCycle extends DirectionalLight3D {

	@Override
	public void _process(double delta) {
		rotateObjectLocal(new Vector3(1, 0, 0), 0.025 * delta);
	}
}
