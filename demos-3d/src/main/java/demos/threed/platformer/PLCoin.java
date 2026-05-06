package demos.threed.platformer;

import org.godot.annotation.GodotClass;
import org.godot.annotation.GodotMethod;
import org.godot.node.Area3D;
import org.godot.node.Node;

@GodotClass(name = "PLCoin", parent = "Area3D")
public class PLCoin extends Area3D {

	private boolean taken = false;
	private boolean initialized = false;

	@Override
	public void _ready() {
		if (initialized) return;
		initialized = true;
	}

	@GodotMethod
	public void OnCoinBodyEnter(Object body) {
		if (taken) return;
		String cls = (String) ((org.godot.Godot) body).call("get_class");
		if (!"PLPlayer".equals(cls)) return;

		taken = true;
		org.godot.node.Node anim = getNode("Animation");
		if (anim != null) anim.call("play", "take");

		Object coinsObj = ((org.godot.Godot) body).getProperty("coins");
		if (coinsObj != null) {
			((org.godot.Godot) body).setProperty("coins", ((Number) coinsObj).intValue() + 1);
		}
	}
}
