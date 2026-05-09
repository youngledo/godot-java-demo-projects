package demos.viewport.screen_capture;

import java.util.Random;

import org.godot.annotation.GodotClass;
import org.godot.annotation.GodotMethod;
import org.godot.math.Color;
import org.godot.node.Button;
import org.godot.node.Control;
import org.godot.node.Image;
import org.godot.node.ImageTexture;
import org.godot.node.TextureRect;
import org.godot.node.Viewport;
import org.godot.node.ViewportTexture;

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

        capturedImage = getNodeAs("CapturedImage", TextureRect.class);
        captureButton = getNodeAs("CaptureButton", Button.class);

        if (captureButton != null && captureButton.isInsideTree()) {
            captureButton.grabFocus();
        }
    }

    @GodotMethod
    public void OnCaptureButtonPressed() {
        if (!isInsideTree()) return;

        Viewport vp = getViewport();
        if (vp == null) return;

        ViewportTexture texture = vp.getTexture();
        if (texture == null) return;

        Image image = texture.getImage();
        if (image == null) return;

        ImageTexture tex = ImageTexture.createFromImage(image);
        if (capturedImage != null && tex != null) {
            capturedImage.setTexture(tex);
        }

        if (captureButton != null) {
            double h = random.nextDouble();
            double s = 0.2 + random.nextDouble() * 0.6;
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
            captureButton.setModulate(new Color(r, g, b));
        }
    }
}
