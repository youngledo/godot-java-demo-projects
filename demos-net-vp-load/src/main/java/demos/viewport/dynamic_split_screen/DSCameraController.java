package demos.viewport.dynamic_split_screen;

import org.godot.annotation.Export;
import org.godot.annotation.GodotClass;
import org.godot.core.Callable;
import org.godot.math.Color;
import org.godot.math.Rect2;
import org.godot.math.Vector2;
import org.godot.math.Vector2i;
import org.godot.math.Vector3;
import org.godot.node.Camera3D;
import org.godot.node.CharacterBody3D;
import org.godot.node.Node3D;
import org.godot.node.ShaderMaterial;
import org.godot.node.SubViewport;
import org.godot.node.TextureRect;
import org.godot.node.Viewport;

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

        player1 = getNodeAs("../Player1", CharacterBody3D.class);
        player2 = getNodeAs("../Player2", CharacterBody3D.class);
        view = getNodeAs("View", TextureRect.class);
        viewport1 = getNodeAs("Viewport1", SubViewport.class);
        viewport2 = getNodeAs("Viewport2", SubViewport.class);

        if (viewport1 != null) {
            camera1 = viewport1.getNodeAs("Camera1", Camera3D.class);
        }
        if (viewport2 != null) {
            camera2 = viewport2.getNodeAs("Camera2", Camera3D.class);
        }

        onSizeChanged();
        updateSplitscreen();

        Viewport vp = getViewport();
        if (vp != null) {
            vp.connect("size_changed", new Callable(this, "onSizeChanged"), 0);
        }

        ShaderMaterial material = getViewShaderMaterial();
        if (material != null) {
            if (viewport1 != null) {
                material.setShaderParameter("viewport1", viewport1.getTexture());
            }
            if (viewport2 != null) {
                material.setShaderParameter("viewport2", viewport2.getTexture());
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

        Vector3 p1Pos = player1.getPosition();
        Vector3 p2Pos = player2.getPosition();
        Vector3 camera1Pos = camera1.getPosition();
        Vector3 camera2Pos = camera2.getPosition();

        camera1.setPosition(new Vector3(
            p1Pos.getX() + positionDifference.getX() / 2.0,
            camera1Pos.getY(),
            p1Pos.getZ() + positionDifference.getZ() / 2.0
        ));

        camera2.setPosition(new Vector3(
            p2Pos.getX() - positionDifference.getX() / 2.0,
            camera2Pos.getY(),
            p2Pos.getZ() - positionDifference.getZ() / 2.0
        ));
    }

    private void updateSplitscreen() {
        if (view == null || camera1 == null || camera2 == null || player1 == null || player2 == null) return;

        Vector2 screenSize = getScreenSize();
        Vector3 p1Pos = player1.getPosition();
        Vector3 p2Pos = player2.getPosition();

        Vector2 p1Screen = camera1.unprojectPosition(p1Pos);
        Vector2 p2Screen = camera2.unprojectPosition(p2Pos);
        Vector2 player1Position = new Vector2(p1Screen.getX() / screenSize.getX(), p1Screen.getY() / screenSize.getY());
        Vector2 player2Position = new Vector2(p2Screen.getX() / screenSize.getX(), p2Screen.getY() / screenSize.getY());

        double thickness = splitLineThickness;
        if (adaptiveSplitLineThickness) {
            Vector3 positionDifference = getPositionDifferenceInWorld();
            double distance = getHorizontalLength(positionDifference);
            thickness = lerpDouble(0, splitLineThickness, (distance - maxSeparation) / maxSeparation);
            thickness = clampDouble(thickness, 0, splitLineThickness);
        }

        ShaderMaterial material = getViewShaderMaterial();
        if (material != null) {
            material.setShaderParameter("split_active", isSplitState());
            material.setShaderParameter("player1_position", player1Position);
            material.setShaderParameter("player2_position", player2Position);
            material.setShaderParameter("split_line_thickness", thickness);
            material.setShaderParameter("split_line_color", splitLineColor);
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
        Vector2 screenSize = getScreenSize();
        Vector2i viewportSize = new Vector2i((int) screenSize.getX(), (int) screenSize.getY());

        if (viewport1 != null) {
            viewport1.setSize(viewportSize);
        }
        if (viewport2 != null) {
            viewport2.setSize(viewportSize);
        }

        ShaderMaterial material = getViewShaderMaterial();
        if (material != null) {
            material.setShaderParameter("viewport_size", screenSize);
        }
    }

    private Vector2 getScreenSize() {
        Viewport vp = getViewport();
        if (vp == null) return new Vector2(1152, 648);

        Rect2 rect = vp.getVisibleRect();
        return rect != null ? rect.size : new Vector2(1152, 648);
    }

    private ShaderMaterial getViewShaderMaterial() {
        if (view == null) return null;
        Object material = view.getMaterial();
        return material instanceof ShaderMaterial shaderMaterial ? shaderMaterial : null;
    }

    private Vector3 getPositionDifferenceInWorld() {
        if (player1 == null || player2 == null) return new Vector3(0, 0, 0);
        Vector3 p2 = player2.getPosition();
        Vector3 p1 = player1.getPosition();
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
