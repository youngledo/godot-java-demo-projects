package demos.threed.truck_town;

import org.godot.annotation.GodotClass;
import org.godot.annotation.GodotMethod;
import org.godot.math.Vector3;
import org.godot.node.Node3D;
import org.godot.node.Node;

@GodotClass(name = "TTTownScene", parent = "Node3D")
public class TTTownScene extends Node3D {

	private int mood = 1; // 0=SUNRISE, 1=DAY, 2=SUNSET, 3=NIGHT
	private boolean initialized = false;

	@Override
	public void _ready() {
		if (initialized) return;
		initialized = true;

		setMood(mood);
	}

	@Override
	public boolean _input(Object inputEvent) {
		org.godot.node.InputEvent ev = (org.godot.node.InputEvent) inputEvent;
		if ((boolean) ev.isActionPressed("cycle_mood")) {
			mood = (mood + 1) % 4;
			setMood(mood);
		}
		return false;
	}

	@GodotMethod
	public void setup(Object car, Object backCallback, boolean sdfgi) {
		org.godot.node.Node carNode = (org.godot.node.Node) car;
		org.godot.node.Node instancePos = getNode("InstancePos");
		if (instancePos != null && carNode != null) {
			instancePos.addChild(carNode);
		}

		org.godot.node.Node worldEnv = getNode("WorldEnvironment");
		if (worldEnv != null) {
			org.godot.Godot env = (org.godot.Godot) worldEnv.getProperty("environment");
			if (env != null) {
				env.setProperty("sdfgi_enabled", sdfgi);
			}
		}
	}

	private void setMood(int m) {
		org.godot.node.DirectionalLight3D light = (org.godot.node.DirectionalLight3D) getNode("DirectionalLight3D");
		org.godot.node.Node worldEnv = getNode("WorldEnvironment");
		if (light == null || worldEnv == null) return;

		org.godot.Godot env = (org.godot.Godot) worldEnv.getProperty("environment");

		switch (m) {
			case 0: // SUNRISE
				light.setProperty("rotation_degrees", new Vector3(-20, -150, -137));
				light.setProperty("light_color", new org.godot.math.Color(0.414, 0.377, 0.25, 1));
				light.setProperty("light_energy", 4.0);
				break;
			case 1: // DAY
				light.setProperty("rotation_degrees", new Vector3(-55, -120, -31));
				light.setProperty("light_color", new org.godot.math.Color(1, 1, 1, 1));
				light.setProperty("light_energy", 1.45);
				break;
			case 2: // SUNSET
				light.setProperty("rotation_degrees", new Vector3(-19, -31, 62));
				light.setProperty("light_color", new org.godot.math.Color(0.488, 0.3, 0.1, 1));
				light.setProperty("light_energy", 4.0);
				break;
			case 3: // NIGHT
				light.setProperty("rotation_degrees", new Vector3(-49, 116, -46));
				light.setProperty("light_color", new org.godot.math.Color(0.232, 0.415, 0.413, 1));
				light.setProperty("light_energy", 0.7);
				break;
		}
	}
}
