package demos.threed.truck_town;

import org.godot.annotation.Export;
import org.godot.annotation.GodotClass;
import org.godot.annotation.GodotMethod;
import org.godot.math.Vector3;
import org.godot.node.Button;

@GodotClass(name = "TTSpeedometer", parent = "Button")
public class TTSpeedometer extends Button {

	private int speedUnit = 0; // 0=m/s, 1=km/h, 2=mph
	public org.godot.Godot carBody;

	@Override
	public void _process(double delta) {
		if (carBody == null) return;

		Vector3 vel = (Vector3) carBody.getProperty("linear_velocity");
		double speed = vel != null ? Math.sqrt(vel.getX()*vel.getX() + vel.getY()*vel.getY() + vel.getZ()*vel.getZ()) : 0;

		String text;
		if (speedUnit == 0) {
			text = String.format("Speed: %.1f m/s", speed);
		} else if (speedUnit == 1) {
			text = String.format("Speed: %.0f km/h", speed * 3.6);
		} else {
			text = String.format("Speed: %.0f mph", speed * 2.23694);
		}
		setProperty("text", text);
	}

	@GodotMethod
	public void _on_speedometer_pressed() {
		speedUnit = (speedUnit + 1) % 3;
	}
}
