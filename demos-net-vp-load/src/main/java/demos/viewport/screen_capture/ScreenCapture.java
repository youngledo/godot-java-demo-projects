package demos.viewport.screen_capture;

import java.util.Random;

import org.godot.annotation.GodotClass;
import org.godot.annotation.GodotMethod;
import org.godot.math.Color;
import org.godot.node.Button;
import org.godot.node.Control;
import org.godot.node.TextureRect;

@GodotClass(name = "ScreenCapture", parent = "Control")
public class ScreenCapture extends Control {

    private TextureRect capturedImage;
    private Button captureButton;
    private boolean initialized = false;
    private final Random random = new Random();

    @Override
    public void _ready() {
        if (initialized) return;
        initialized = true;

        capturedImage = (TextureRect) getNode("CapturedImage");
        captureButton = (Button) getNode("CaptureButton");

        // Focus button for keyboard/gamepad-friendly navigation
        if (captureButton != null && (boolean) captureButton.isInsideTree()) {
            captureButton.grabFocus();
        }
    }

    @GodotMethod
    public void OnCaptureButtonPressed() {
        if (!isInsideTree()) return;

        // Retrieve the captured image
        Object vp = getViewport();
        if (vp == null) return;

        Object texture = ((org.godot.Godot) vp).call("get_texture");
        if (texture == null) return;

        Object img = ((org.godot.Godot) texture).call("get_image");
        if (img == null) return;

        // Create a texture from the image
        Object tex = call("ImageTexture.create_from_image", img);

        // Set the texture to the captured image node
        if (capturedImage != null && tex != null) {
            capturedImage.call("set_texture", tex);
        }

        // Colorize the button with a random color
        if (captureButton != null) {
            // Random color using HSV-like approach
            double h = random.nextDouble();
            double s = 0.2 + random.nextDouble() * 0.6;
            // Convert HSV to RGB (simplified)
            int i = (int) (h * 6);
            double f = h * 6 - i;
            double p = 1.0 * (1 - s);
            double q = 1.0 * (1 - f * s);
            double t = 1.0 * (1 - (1 - f) * s);
            double r, g, b;
            switch (i % 6) {
                case 0: r = 1; g = t; b = p; break;
                case 1: r = q; g = 1; b = p; break;
                case 2: r = p; g = 1; b = t; break;
                case 3: r = p; g = q; b = 1; break;
                case 4: r = t; g = p; b = 1; break;
                default: r = 1; g = p; b = q; break;
            }
            captureButton.setProperty("modulate", new Color(r, g, b));
        }
    }
}
