package demos.threed.sprites;

import org.godot.annotation.GodotClass;
import org.godot.annotation.Export;
import org.godot.math.Vector3;
import org.godot.node.Sprite3D;

@GodotClass(name = "SpriteRotate", parent = "Sprite3D")
public class SpriteRotate extends Sprite3D {

	@Export
	public double speedDeg = 90.0;

	@Override
	public void _process(double delta) {
		call("rotate_y", Math.toRadians(speedDeg * delta));
	}
}
