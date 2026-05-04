package demos.threed.truck_town;

import org.godot.annotation.GodotClass;
import org.godot.annotation.GodotMethod;
import org.godot.node.Control;

@GodotClass(name = "TTCarSelect", parent = "Control")
public class TTCarSelect extends Control {

	private org.godot.Godot town;
	private boolean initialized = false;

	@Override
	public void _ready() {
		if (initialized) return;
		initialized = true;
	}

	@Override
	public void _process(double delta) {
		org.godot.singleton.Input input = org.godot.singleton.Input.singleton();
		if ((boolean) input.call("is_action_just_pressed", "back")) {
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
		Object carSceneObj = call("load", carPath);
		if (carSceneObj == null) return;
		org.godot.Godot car = (org.godot.Godot) ((org.godot.Godot) carSceneObj).call("instantiate");

		Object townSceneObj = call("load", "res://town/town_scene.tscn");
		if (townSceneObj == null) return;
		town = (org.godot.Godot) ((org.godot.Godot) townSceneObj).call("instantiate");

		if (town != null) {
			town.call("setup", car, null, false);
			org.godot.Godot parent = (org.godot.Godot) call("get_parent");
			if (parent != null) parent.call("add_child", town);
			call("hide");
		}
	}

	@GodotMethod
	public void onBackPressed() {
		if (town != null) {
			town.call("queue_free");
			town = null;
			call("show");
		} else {
			org.godot.Godot tree = (org.godot.Godot) call("get_tree");
			if (tree != null) tree.call("quit");
		}
	}
}
