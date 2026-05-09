package demos.viewport.dynamic_split_screen;

import java.util.Random;

import org.godot.annotation.GodotClass;
import org.godot.math.Color;
import org.godot.node.GeometryInstance3D;
import org.godot.node.Node;
import org.godot.node.Node3D;
import org.godot.node.SceneTree;
import org.godot.node.StandardMaterial3D;

@GodotClass(name = "DSWallColoring", parent = "Node3D")
public class DSWallColoring extends Node3D {

    private static final Random random = new Random();

    @Override
    public void _ready() {
        SceneTree tree = getTree();
        if (tree == null) return;

        Node[] walls = tree.getNodesInGroup("walls");
        for (Node wall : walls) {
            if (wall instanceof GeometryInstance3D wallNode) {
                Color color = new Color(random.nextDouble(), random.nextDouble(), random.nextDouble());
                StandardMaterial3D material = StandardMaterial3D.create();
                material.setAlbedoColor(color);
                wallNode.setMaterialOverride(material);
            }
        }
    }
}
