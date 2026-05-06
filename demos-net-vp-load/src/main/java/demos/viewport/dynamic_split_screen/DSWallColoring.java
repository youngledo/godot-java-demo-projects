package demos.viewport.dynamic_split_screen;

import java.util.Random;

import org.godot.annotation.GodotClass;
import org.godot.node.Node3D;
import org.godot.node.Node;

@GodotClass(name = "DSWallColoring", parent = "Node3D")
public class DSWallColoring extends Node3D {

    private static final Random random = new Random();

    @Override
    public void _ready() {
        // Get all nodes in the "walls" group and set random colors
        Object tree = getTree();
        if (tree == null) return;

        Object walls = ((org.godot.Godot) tree).call("get_nodes_in_group", "walls");
        if (walls instanceof org.godot.collection.GodotArray) {
            org.godot.collection.GodotArray wallsArray = (org.godot.collection.GodotArray) walls;
            for (int i = 0; i < wallsArray.size(); i++) {
                Object wall = wallsArray.get(i);
                if (wall instanceof org.godot.Godot) {
                    org.godot.Godot wallNode = (org.godot.Godot) wall;
                    // Create a new StandardMaterial3D with random color
                    org.godot.math.Color color = new org.godot.math.Color(
                        random.nextDouble(), random.nextDouble(), random.nextDouble()
                    );
                    Object material = call("_new_standard_material_3d");
                    if (material != null) {
                        ((org.godot.Godot) material).setProperty("albedo_color", color);
                        wallNode.setProperty("material_override", material);
                    }
                }
            }
        }
    }
}
