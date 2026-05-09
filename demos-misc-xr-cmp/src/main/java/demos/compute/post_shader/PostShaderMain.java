package demos.compute.post_shader;

import org.godot.annotation.GodotClass;
import org.godot.node.Compositor;
import org.godot.node.CompositorEffect;
import org.godot.node.Label3D;
import org.godot.node.Node3D;
import org.godot.node.WorldEnvironment;
import org.godot.singleton.Input;

@GodotClass(name = "PostShaderMain", parent = "Node3D")
public class PostShaderMain extends Node3D {

    private Compositor compositor;
    private boolean initialized = false;

    @Override
    public void _ready() {
        if (initialized) return;
        initialized = true;

        WorldEnvironment worldEnv = getNodeAs("WorldEnvironment", WorldEnvironment.class);
        if (worldEnv != null) {
            compositor = worldEnv.getCompositor();
        }
    }

    @Override
    public boolean _input(Object inputEvent) {
        Input input = Input.singleton();

        if (input.isActionPressed("toggle_grayscale_effect")) {
            toggleEffect(0);
            updateInfoText();
        }

        if (input.isActionPressed("toggle_shader_effect")) {
            toggleEffect(1);
            updateInfoText();
        }

        return false;
    }

    private void toggleEffect(int index) {
        CompositorEffect effect = effectAt(index);
        if (effect != null) {
            effect.setEnabled(!effect.getEnabled());
        }
    }

    private void updateInfoText() {
        Label3D info = getNodeAs("Info", Label3D.class);
        if (info == null || compositor == null) return;

        CompositorEffect grayscaleEffect = effectAt(0);
        CompositorEffect shaderEffect = effectAt(1);
        String grayscaleStatus = grayscaleEffect != null && grayscaleEffect.getEnabled() ? "Enabled" : "Disabled";
        String shaderStatus = shaderEffect != null && shaderEffect.getEnabled() ? "Enabled" : "Disabled";

        info.setText("Grayscale effect: " + grayscaleStatus + "\n"
            + "Shader effect: " + shaderStatus + "\n");
    }

    private CompositorEffect effectAt(int index) {
        if (compositor == null) return null;
        CompositorEffect[] effects = compositor.getCompositorEffects();
        if (effects == null || index < 0 || index >= effects.length) return null;
        return effects[index];
    }
}
