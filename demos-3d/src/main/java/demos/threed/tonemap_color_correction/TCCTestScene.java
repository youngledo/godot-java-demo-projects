package demos.threed.tonemap_color_correction;

import org.godot.annotation.GodotClass;
import org.godot.annotation.Export;
import org.godot.node.Node3D;

@GodotClass(name = "TCCTestScene", parent = "Node3D")
public class TCCTestScene extends Node3D {

	@Export
	public org.godot.Godot worldEnvironment;
}
