package demos.threed.global_illumination;

import org.godot.annotation.GodotClass;
import org.godot.annotation.GodotMethod;
import org.godot.node.Node3D;

@GodotClass(name = "GITest", parent = "Node3D")
public class GITest extends Node3D {

	private int giMode = 0;
	private int reflectionProbeMode = 0;
	private int ssilMode = 0;

	private org.godot.Godot environment;
	private org.godot.Godot giModeLabel;
	private org.godot.Godot fpsLabel;
	private org.godot.Godot reflectionProbeModeLabel;
	private org.godot.Godot ssilModeLabel;
	private org.godot.Godot reflectionProbe;
	private org.godot.Godot voxelGI;
	private org.godot.Godot lightmapGIAll;
	private org.godot.Godot lightmapGIIndirect;
	private boolean initialized = false;

	private static final String[] GI_MODE_TEXTS = {
		"Environment Lighting (Fastest)",
		"Baked Lightmap All (Fast)",
		"Baked Lightmap Indirect (Average)",
		"VoxelGI (Slow)",
		"SDFGI (Slow)"
	};

	@Override
	public void _ready() {
		if (initialized) return;
		initialized = true;

		org.godot.Godot worldEnv = (org.godot.Godot) call("get_node", "WorldEnvironment");
		if (worldEnv != null) environment = (org.godot.Godot) worldEnv.getProperty("environment");

		giModeLabel = (org.godot.Godot) call("get_node", "GIMode");
		fpsLabel = (org.godot.Godot) call("get_node", "FPS");
		reflectionProbeModeLabel = (org.godot.Godot) call("get_node", "ReflectionProbeMode");
		ssilModeLabel = (org.godot.Godot) call("get_node", "SSILMode");
		reflectionProbe = (org.godot.Godot) call("get_node", "Camera/ReflectiveSphere/ReflectionProbe");
		voxelGI = (org.godot.Godot) call("get_node", "VoxelGI");
		lightmapGIAll = (org.godot.Godot) call("get_node", "LightmapGIAll");
		lightmapGIIndirect = (org.godot.Godot) call("get_node", "LightmapGIIndirect");

		setGiMode(giMode);
		setReflectionProbeMode(reflectionProbeMode);
		setSsilMode(ssilMode);
	}

	@Override
	public boolean _input(Object inputEvent) {
		org.godot.Godot ev = (org.godot.Godot) inputEvent;
		if ((boolean) ev.call("is_action_pressed", "cycle_gi_mode")) {
			giMode = (giMode + 1) % 5;
			setGiMode(giMode);
		}
		if ((boolean) ev.call("is_action_pressed", "cycle_reflection_probe_mode")) {
			reflectionProbeMode = (reflectionProbeMode + 1) % 3;
			setReflectionProbeMode(reflectionProbeMode);
		}
		if ((boolean) ev.call("is_action_pressed", "cycle_ssil_mode")) {
			ssilMode = (ssilMode + 1) % 4;
			setSsilMode(ssilMode);
		}
		return false;
	}

	@Override
	public void _physicsProcess(double delta) {
		if (fpsLabel != null) {
			org.godot.singleton.Engine engine = org.godot.singleton.Engine.singleton();
			if (engine != null) {
				long fps = (long) engine.call("get_frames_per_second");
				double mspf = fps > 0 ? 1000.0 / fps : 0;
				fpsLabel.setProperty("text", fps + " FPS (" + String.format("%.2f", mspf) + " mspf)");
			}
		}
	}

	private void setGiMode(int mode) {
		giMode = mode;
		if (giModeLabel != null) {
			giModeLabel.setProperty("text", "Global illumination: " + GI_MODE_TEXTS[mode]);
		}

		// Toggle visibility of GI nodes
		if (voxelGI != null) voxelGI.setProperty("visible", mode == 3);
		if (lightmapGIAll != null) lightmapGIAll.setProperty("visible", mode == 1);
		if (lightmapGIIndirect != null) lightmapGIIndirect.setProperty("visible", mode == 2);

		// SDFGI
		if (environment != null) {
			environment.setProperty("sdfgi_enabled", mode == 4);
		}
	}

	private void setReflectionProbeMode(int mode) {
		reflectionProbeMode = mode;
		if (reflectionProbe != null) {
			reflectionProbe.setProperty("visible", mode > 0);
			reflectionProbe.setProperty("update_mode", mode == 2 ? 1 : 0); // 0=ONCE, 1=ALWAYS
		}
	}

	private void setSsilMode(int mode) {
		ssilMode = mode;
		if (environment != null) {
			environment.setProperty("ssao_enabled", mode == 1 || mode == 3);
			environment.setProperty("ssil_enabled", mode == 2 || mode == 3);
		}
	}
}
