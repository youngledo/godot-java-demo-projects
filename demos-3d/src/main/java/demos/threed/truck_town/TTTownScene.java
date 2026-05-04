package demos.threed.truck_town;

import org.godot.annotation.GodotClass;
import org.godot.annotation.GodotMethod;
import org.godot.math.Vector3;
import org.godot.node.Node3D;

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
		org.godot.Godot ev = (org.godot.Godot) inputEvent;
		if ((boolean) ev.call("is_action_pressed", "cycle_mood")) {
			mood = (mood + 1) % 4;
			setMood(mood);
		}
		return false;
	}

	@GodotMethod
	public void setup(Object car, Object backCallback, boolean sdfgi) {
		org.godot.Godot carNode = (org.godot.Godot) car;
		org.godot.Godot instancePos = (org.godot.Godot) call("get_node", "InstancePos");
		if (instancePos != null && carNode != null) {
			instancePos.call("add_child", carNode);
		}

		org.godot.Godot worldEnv = (org.godot.Godot) call("get_node", "WorldEnvironment");
		if (worldEnv != null) {
			org.godot.Godot env = (org.godot.Godot) worldEnv.getProperty("environment");
			if (env != null) {
				env.setProperty("sdfgi_enabled", sdfgi);
			}
		}
	}

	private void setMood(int m) {
		org.godot.Godot light = (org.godot.Godot) call("get_node", "DirectionalLight3D");
		org.godot.Godot worldEnv = (org.godot.Godot) call("get_node", "WorldEnvironment");
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
