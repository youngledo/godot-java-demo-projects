package demos.threed.truck_town;

import org.godot.annotation.GodotClass;
import org.godot.annotation.GodotMethod;
import org.godot.node.Control;
import org.godot.node.SceneTree;
import org.godot.singleton.Input;

@GodotClass(name = "TTCarSelect", parent = "Control")
public class TTCarSelect extends Control {

	private TTTownScene town;
	private boolean initialized = false;

	@Override
	public void _ready() {
		if (initialized) return;
		initialized = true;
	}

	@Override
	public void _process(double delta) {
		org.godot.singleton.Input input = org.godot.singleton.Input.singleton();
		if ((boolean) (boolean) input.isActionJustPressed("back")) {
			onBackPressed();
		}
	}

	@GodotMethod
	public void onMiniVanPressed() {
		loadScene("res://vehicles/car_base.tscn");
	}

	@GodotMethod
	public void onTrailerTruckPressed() {
		loadScene("res://vehicles/trailer_truck.tscn");
	}

	@GodotMethod
	public void onTowTruckPressed() {
		loadScene("res://vehicles/tow_truck.tscn");
	}

	private void loadScene(String carPath) {
		org.godot.node.PackedScene carSceneObj = (org.godot.node.PackedScene) org.godot.singleton.ResourceLoader.singleton().load(carPath);
		if (carSceneObj == null) return;
		org.godot.Godot car = carSceneObj.instantiate();

		org.godot.node.PackedScene townSceneObj = (org.godot.node.PackedScene) org.godot.singleton.ResourceLoader.singleton().load("res://town/town_scene.tscn");
		if (townSceneObj == null) return;
		town = (TTTownScene) townSceneObj.instantiate();

		if (town != null) {
			town.setup(car, null, false);
			org.godot.node.Node parent = (org.godot.node.Node) getParent();
			if (parent != null) parent.addChild(town);
			hide();
		}
	}

	@GodotMethod
	public void onBackPressed() {
		if (town != null) {
			town.queueFree();
			town = null;
			show();
		} else {
			org.godot.node.SceneTree tree = getTree();
			if (tree != null) tree.quit();
		}
	}
}
