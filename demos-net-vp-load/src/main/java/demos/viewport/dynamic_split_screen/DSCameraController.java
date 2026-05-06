package demos.viewport.dynamic_split_screen;

import org.godot.annotation.Export;
import org.godot.annotation.GodotClass;
import org.godot.core.Callable;
import org.godot.math.Color;
import org.godot.math.Vector2;
import org.godot.math.Vector3;
import org.godot.node.Camera3D;
import org.godot.node.CharacterBody3D;
import org.godot.node.Node3D;
import org.godot.node.SubViewport;
import org.godot.node.TextureRect;
import org.godot.node.Node;

@GodotClass(name = "DSCameraController", parent = "Node3D")
public class DSCameraController extends Node3D {

    @Export
    public double maxSeparation = 20.0;

    @Export
    public double splitLineThickness = 3.0;

    @Export
    public Color splitLineColor = new Color(0, 0, 0, 1);

    @Export
    public boolean adaptiveSplitLineThickness = true;

    private CharacterBody3D player1;
    private CharacterBody3D player2;
    private TextureRect view;
    private SubViewport viewport1;
    private SubViewport viewport2;
    private Camera3D camera1;
    private Camera3D camera2;
    private boolean initialized = false;

    @Override
    public void _ready() {
        if (initialized) return;
        initialized = true;

        player1 = (CharacterBody3D) getNode("../Player1");
        player2 = (CharacterBody3D) getNode("../Player2");
        view = (TextureRect) getNode("View");
        viewport1 = (SubViewport) getNode("Viewport1");
        viewport2 = (SubViewport) getNode("Viewport2");

        if (viewport1 != null) {
            camera1 = (Camera3D) viewport1.getNode("Camera1");
        }
        if (viewport2 != null) {
            camera2 = (Camera3D) viewport2.getNode("Camera2");
        }

        onSizeChanged();
        updateSplitscreen();

        // Connect viewport size_changed signal
        Object vp = getViewport();
        if (vp != null) {
            ((org.godot.Godot) vp).connect("size_changed", new Callable(this, "onSizeChanged"), 0);
        }

        // Set shader parameters for viewport textures
        if (view != null) {
            Object material = view.getProperty("material");
            if (material != null) {
                org.godot.node.ShaderMaterial mat = (org.godot.node.ShaderMaterial) material;
                if (viewport1 != null) {
                    mat.call("set_shader_parameter", "viewport1", viewport1.call("get_texture"));
                }
                if (viewport2 != null) {
                    mat.call("set_shader_parameter", "viewport2", viewport2.call("get_texture"));
                }
            }
        }
    }

    @Override
    public void _process(double delta) {
        moveCameras();
        updateSplitscreen();
    }

    private void moveCameras() {
        if (player1 == null || player2 == null || camera1 == null || camera2 == null) return;

        Vector3 positionDifference = getPositionDifferenceInWorld();
        double distance = clampDouble(getHorizontalLength(positionDifference), 0, maxSeparation);

        double len = getHorizontalLength(positionDifference);
        if (len > 0.001) {
            positionDifference = new Vector3(
                positionDifference.getX() / len * distance,
                positionDifference.getY() / len * distance,
                positionDifference.getZ() / len * distance
            );
        }

        Vector3 p1Pos = (Vector3) player1.getProperty("position");
        Vector3 p2Pos = (Vector3) player2.getProperty("position");

        camera1.setProperty("position", new Vector3(
            p1Pos.getX() + positionDifference.getX() / 2.0,
            ((Vector3) camera1.getProperty("position")).getY(),
            p1Pos.getZ() + positionDifference.getZ() / 2.0
        ));

        camera2.setProperty("position", new Vector3(
            p2Pos.getX() - positionDifference.getX() / 2.0,
            ((Vector3) camera2.getProperty("position")).getY(),
            p2Pos.getZ() - positionDifference.getZ() / 2.0
        ));
    }

    private void updateSplitscreen() {
        if (view == null || camera1 == null || camera2 == null || player1 == null || player2 == null) return;

        Object vp = getViewport();
        Vector2 screenSize = new Vector2(1152, 648);
        if (vp != null) {
            Object rect = ((org.godot.Godot) vp).call("get_visible_rect");
            if (rect != null) {
                Object s = ((org.godot.Godot) rect).getProperty("size");
                if (s instanceof Vector2) screenSize = (Vector2) s;
            }
        }

        // Calculate player screen positions
        Vector3 p1Pos = (Vector3) player1.getProperty("position");
        Vector3 p2Pos = (Vector3) player2.getProperty("position");

        Object p1ScreenObj = camera1.unprojectPosition(p1Pos);
        Object p2ScreenObj = camera2.unprojectPosition(p2Pos);

        Vector2 player1Position = new Vector2(0, 0);
        Vector2 player2Position = new Vector2(0, 0);
        if (p1ScreenObj instanceof Vector2) {
            player1Position = new Vector2(((Vector2) p1ScreenObj).getX() / screenSize.getX(), ((Vector2) p1ScreenObj).getY() / screenSize.getY());
        }
        if (p2ScreenObj instanceof Vector2) {
            player2Position = new Vector2(((Vector2) p2ScreenObj).getX() / screenSize.getX(), ((Vector2) p2ScreenObj).getY() / screenSize.getY());
        }

        double thickness = 0.0;
        if (adaptiveSplitLineThickness) {
            Vector3 positionDifference = getPositionDifferenceInWorld();
            double distance = getHorizontalLength(positionDifference);
            thickness = lerpDouble(0, splitLineThickness, (distance - maxSeparation) / maxSeparation);
            thickness = clampDouble(thickness, 0, splitLineThickness);
        } else {
            thickness = splitLineThickness;
        }

        boolean splitActive = isSplitState();

        Object material = view.getProperty("material");
        if (material != null) {
            org.godot.node.ShaderMaterial mat = (org.godot.node.ShaderMaterial) material;
            mat.call("set_shader_parameter", "split_active", splitActive);
            mat.call("set_shader_parameter", "player1_position", player1Position);
            mat.call("set_shader_parameter", "player2_position", player2Position);
            mat.call("set_shader_parameter", "split_line_thickness", thickness);
            mat.call("set_shader_parameter", "split_line_color", splitLineColor);
        }
    }

    private boolean isSplitState() {
        if (player1 == null || player2 == null) return false;
        Vector3 positionDifference = getPositionDifferenceInWorld();
        double separationDistance = getHorizontalLength(positionDifference);
        return separationDistance > maxSeparation;
    }

    @org.godot.annotation.GodotMethod
    public void onSizeChanged() {
        Object vp = getViewport();
        Vector2 screenSize = new Vector2(1152, 648);
        if (vp != null) {
            Object rect = ((org.godot.Godot) vp).call("get_visible_rect");
            if (rect != null) {
                Object s = ((org.godot.Godot) rect).getProperty("size");
                if (s instanceof Vector2) screenSize = (Vector2) s;
            }
        }

        if (viewport1 != null) {
            viewport1.setProperty("size", screenSize);
        }
        if (viewport2 != null) {
            viewport2.setProperty("size", screenSize);
        }

        if (view != null) {
            Object material = view.getProperty("material");
            if (material != null) {
                ((org.godot.node.ShaderMaterial) material).call("set_shader_parameter", "viewport_size", screenSize);
            }
        }
    }

    private Vector3 getPositionDifferenceInWorld() {
        if (player1 == null || player2 == null) return new Vector3(0, 0, 0);
        Vector3 p2 = (Vector3) player2.getProperty("position");
        Vector3 p1 = (Vector3) player1.getProperty("position");
        return new Vector3(p2.getX() - p1.getX(), p2.getY() - p1.getY(), p2.getZ() - p1.getZ());
    }

    private double getHorizontalLength(Vector3 vec) {
        return Math.sqrt(vec.getX() * vec.getX() + vec.getZ() * vec.getZ());
    }

    private static double clampDouble(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }

    private static double lerpDouble(double from, double to, double weight) {
        return from + (to - from) * weight;
    }
}
