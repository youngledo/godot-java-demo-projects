package demos.misc.large_world_coordinates;

import org.godot.annotation.Export;
import org.godot.annotation.GodotClass;
import org.godot.annotation.GodotMethod;
import org.godot.math.Basis;
import org.godot.math.Color;
import org.godot.math.Transform3D;
import org.godot.math.Vector2;
import org.godot.math.Vector3;
import org.godot.node.BaseButton;
import org.godot.node.InputEventMouseButton;
import org.godot.node.InputEventMouseMotion;
import org.godot.node.Node3D;
import org.godot.node.RichTextLabel;
import org.godot.node.VBoxContainer;
import org.godot.singleton.OS;

@GodotClass(name = "LWCControls", parent = "VBoxContainer")
public class LWCControls extends VBoxContainer {

    private static final double ROT_SPEED = 0.003;
    private static final double ZOOM_SPEED = 0.5;
    private static final int MAIN_BUTTONS = 1 | 4 | 2;

    @Export
    public Node3D camera;
    @Export
    public Node3D cameraHolder;
    @Export
    public Node3D rotationX;
    @Export
    public Node3D nodeToMove;
    @Export
    public Node3D rigidBody;

    private double zoom = 7.0;
    private double rotX = 0.0;
    private double rotY = 0.0;
    private boolean initialized = false;

    @Override
    public void _ready() {
        if (initialized) return;
        initialized = true;

        if (camera != null) {
            zoom = camera.getPosition().z;
        }

        RichTextLabel helpLabel = getNodeAs("%HelpLabel", RichTextLabel.class);
        if (OS.singleton().hasFeature("double") && helpLabel != null) {
            helpLabel.setText("Double precision is enabled in this engine build.\n"
                + "No shaking should occur at high coordinate levels\n"
                + "(±65,536 or more on any axis).");
            helpLabel.addThemeColorOverride("font_color", new Color(0.667, 1.0, 0.667));
        }
    }

    @Override
    public void _process(double delta) {
        RichTextLabel coordinates = getNodeAs("%Coordinates", RichTextLabel.class);
        BaseButton incrementX = getNodeAs("%IncrementX", BaseButton.class);
        BaseButton incrementY = getNodeAs("%IncrementY", BaseButton.class);
        BaseButton incrementZ = getNodeAs("%IncrementZ", BaseButton.class);

        if (nodeToMove != null && coordinates != null) {
            Vector3 pos = nodeToMove.getPosition();
            coordinates.setText(String.format(
                "X: [color=#fb9]%f[/color]\nY: [color=#bfa]%f[/color]\nZ: [color=#9cf]%f[/color]",
                pos.x, pos.y, pos.z));

            if (incrementX != null && incrementX.isButtonPressed()) {
                Vector3 curPos = nodeToMove.getPosition();
                nodeToMove.setPosition(new Vector3(curPos.x + 10000.0 * delta, curPos.y, curPos.z));
            }
            if (incrementY != null && incrementY.isButtonPressed()) {
                Vector3 curPos = nodeToMove.getPosition();
                nodeToMove.setPosition(new Vector3(curPos.x, curPos.y + 100000.0 * delta, curPos.z));
            }
            if (incrementZ != null && incrementZ.isButtonPressed()) {
                Vector3 curPos = nodeToMove.getPosition();
                nodeToMove.setPosition(new Vector3(curPos.x, curPos.y, curPos.z + 1000000.0 * delta));
            }
        }
    }

    @Override
    public boolean _input(Object inputEvent) {
        if (inputEvent instanceof InputEventMouseButton event) {
            long buttonIndex = event.getButtonIndex();
            if (buttonIndex == 4) {
                zoom -= ZOOM_SPEED;
            }
            if (buttonIndex == 5) {
                zoom += ZOOM_SPEED;
            }
            zoom = Math.max(4, Math.min(15, zoom));
            if (camera != null) {
                Vector3 p = camera.getPosition();
                camera.setPosition(new Vector3(p.x, p.y, zoom));
            }
        }

        if (inputEvent instanceof InputEventMouseMotion event) {
            long buttonMask = event.getButtonMask();
            if ((buttonMask & MAIN_BUTTONS) != 0) {
                Vector2 relative = event.getScreenRelative();
                rotY -= relative.x * ROT_SPEED;
                rotX -= relative.y * ROT_SPEED;
                rotX = Math.max(-1.4, Math.min(0.16, rotX));
                if (cameraHolder != null) {
                    cameraHolder.setTransform(new Transform3D(Basis.fromEuler(new Vector3(0, rotY, 0)), new Vector3()));
                }
                if (rotationX != null) {
                    rotationX.setTransform(new Transform3D(Basis.fromEuler(new Vector3(rotX, 0, 0)), new Vector3()));
                }
            }
        }
        return false;
    }

    @GodotMethod
    public void OnGoToButtonPressed(long xPosition) {
        if (nodeToMove == null) return;
        if (xPosition == 0) {
            nodeToMove.setPosition(Vector3.ZERO);
        } else {
            Vector3 curPos = nodeToMove.getPosition();
            nodeToMove.setPosition(new Vector3((double) xPosition, curPos.y, curPos.z));
        }
    }

    @GodotMethod
    public void OnOpenDocumentationPressed() {
        OS.singleton().shellOpen("https://docs.godotengine.org/en/latest/tutorials/physics/large_world_coordinates.html");
    }
}
