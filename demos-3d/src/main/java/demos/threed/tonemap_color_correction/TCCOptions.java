package demos.threed.tonemap_color_correction;

import org.godot.annotation.Export;
import org.godot.annotation.GodotClass;
import org.godot.annotation.GodotMethod;
import org.godot.node.VBoxContainer;
import org.godot.node.Node;

@GodotClass(name = "TCCOptions", parent = "VBoxContainer")
public class TCCOptions extends VBoxContainer {

	@Export
	public String[] scenePaths;

	private org.godot.node.Node currentScene;
	private org.godot.Godot worldEnvironment;
	private boolean initialized = false;

	@Override
	public void _ready() {
		if (initialized) return;
		initialized = true;

		onSceneOptionButtonItemSelected(0);
	}

	@GodotMethod
	public void onSceneOptionButtonItemSelected(int index) {
		if (currentScene != null) {
			currentScene.queueFree();
			currentScene = null;
		}

		org.godot.Godot oldEnv = worldEnvironment;

		if (scenePaths != null && index < scenePaths.length) {
			Object sceneObj = call("load", scenePaths[index]);
			if (sceneObj instanceof org.godot.node.PackedScene sceneRes) {
				org.godot.node.Node instance = (org.godot.node.Node) sceneRes.instantiate();
				if (instance != null) {
					currentScene = instance;
					addChild(instance);

					worldEnvironment = (org.godot.Godot) instance.getProperty("world_environment");
				}
			}
		}
	}

	@GodotMethod
	public void onTonemapModeItemSelected(int index) {
		if (worldEnvironment != null) {
			org.godot.Godot env = (org.godot.Godot) worldEnvironment.getProperty("environment");
			if (env != null) env.setProperty("tonemap_mode", index);
		}
	}

	@GodotMethod
	public void onExposureValueChanged(double value) {
		if (worldEnvironment != null) {
			org.godot.Godot env = (org.godot.Godot) worldEnvironment.getProperty("environment");
			if (env != null) env.setProperty("tonemap_exposure", value);
		}
	}

	@GodotMethod
	public void onWhitepointValueChanged(double value) {
		if (worldEnvironment != null) {
			org.godot.Godot env = (org.godot.Godot) worldEnvironment.getProperty("environment");
			if (env != null) env.setProperty("tonemap_white", value);
		}
	}

	@GodotMethod
	public void onSaturationValueChanged(double value) {
		if (worldEnvironment != null) {
			org.godot.Godot env = (org.godot.Godot) worldEnvironment.getProperty("environment");
			if (env != null) env.setProperty("adjustment_saturation", value);
		}
	}
}
