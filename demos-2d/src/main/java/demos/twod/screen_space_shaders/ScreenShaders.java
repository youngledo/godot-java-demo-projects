package demos.twod.screen_space_shaders;

import org.godot.annotation.GodotClass;
import org.godot.annotation.GodotMethod;
import org.godot.node.Control;

@GodotClass(name = "ScreenShaders", parent = "Control")
public class ScreenShaders extends Control {

	private org.godot.Godot effectBtn;
	private org.godot.Godot effects;
	private org.godot.Godot pictureBtn;
	private org.godot.Godot pictures;
	private boolean initialized = false;

	@Override
	public void _ready() {
		if (initialized) return;
		initialized = true;

		effectBtn = (org.godot.Godot) call("get_node", "Effect");
		effects = (org.godot.Godot) call("get_node", "Effects");
		pictureBtn = (org.godot.Godot) call("get_node", "Picture");
		pictures = (org.godot.Godot) call("get_node", "Pictures");

		// Populate option buttons
		if (pictures != null && pictureBtn != null) {
			int picCount = (int) (long) pictures.call("get_child_count");
			for (int i = 0; i < picCount; i++) {
				org.godot.Godot child = (org.godot.Godot) pictures.call("get_child", i);
				if (child != null) {
					String name = (String) child.call("get_name");
					pictureBtn.call("add_item", "PIC: " + name);
				}
			}
		}
		if (effects != null && effectBtn != null) {
			int fxCount = (int) (long) effects.call("get_child_count");
			for (int i = 0; i < fxCount; i++) {
				org.godot.Godot child = (org.godot.Godot) effects.call("get_child", i);
				if (child != null) {
					String name = (String) child.call("get_name");
					effectBtn.call("add_item", "FX: " + name);
				}
			}
		}
	}

	@GodotMethod
	public void _on_picture_item_selected(long id) {
		if (pictures == null) return;
		int count = (int) (long) pictures.call("get_child_count");
		for (int i = 0; i < count; i++) {
			org.godot.Godot child = (org.godot.Godot) pictures.call("get_child", i);
			if (child != null) {
				if (i == id) child.call("show");
				else child.call("hide");
			}
		}
	}

	@GodotMethod
	public void _on_effect_item_selected(long id) {
		if (effects == null) return;
		int count = (int) (long) effects.call("get_child_count");
		for (int i = 0; i < count; i++) {
			org.godot.Godot child = (org.godot.Godot) effects.call("get_child", i);
			if (child != null) {
				if (i == id) child.call("show");
				else child.call("hide");
			}
		}
	}
}
