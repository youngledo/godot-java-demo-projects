package demos.twod.screen_space_shaders;

import org.godot.annotation.GodotClass;
import org.godot.annotation.GodotMethod;
import org.godot.node.Control;
import org.godot.node.Node;

@GodotClass(name = "ScreenShaders", parent = "Control")
public class ScreenShaders extends Control {

	private org.godot.node.Node effectBtn;
	private org.godot.node.Node effects;
	private org.godot.node.Node pictureBtn;
	private org.godot.node.Node pictures;
	private boolean initialized = false;

	@Override
	public void _ready() {
		if (initialized) return;
		initialized = true;

		effectBtn = getNode("Effect");
		effects = getNode("Effects");
		pictureBtn = getNode("Picture");
		pictures = getNode("Pictures");

		// Populate option buttons
		if (pictures != null && pictureBtn != null) {
			int picCount = (int) (long) pictures.getChildCount();
			for (int i = 0; i < picCount; i++) {
				org.godot.node.CanvasItem child = (org.godot.node.CanvasItem) pictures.getChild(i);
				if (child != null) {
					String name = (String) child.getName();
					pictureBtn.call("add_item", "PIC: " + name);
				}
			}
		}
		if (effects != null && effectBtn != null) {
			int fxCount = (int) (long) effects.getChildCount();
			for (int i = 0; i < fxCount; i++) {
				org.godot.node.CanvasItem child = (org.godot.node.CanvasItem) effects.getChild(i);
				if (child != null) {
					String name = (String) child.getName();
					effectBtn.call("add_item", "FX: " + name);
				}
			}
		}
	}

	@GodotMethod
	public void OnPictureItemSelected(long id) {
		if (pictures == null) return;
		int count = (int) (long) pictures.getChildCount();
		for (int i = 0; i < count; i++) {
			org.godot.node.CanvasItem child = (org.godot.node.CanvasItem) pictures.getChild(i);
			if (child != null) {
				if (i == id) child.show();
				else child.hide();
			}
		}
	}

	@GodotMethod
	public void OnEffectItemSelected(long id) {
		if (effects == null) return;
		int count = (int) (long) effects.getChildCount();
		for (int i = 0; i < count; i++) {
			org.godot.node.CanvasItem child = (org.godot.node.CanvasItem) effects.getChild(i);
			if (child != null) {
				if (i == id) child.show();
				else child.hide();
			}
		}
	}
}
