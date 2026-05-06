package demos.compute.post_shader;

import org.godot.annotation.GodotClass;
import org.godot.annotation.GodotMethod;
import org.godot.node.Node3D;
import org.godot.node.Node;

/**
 * Port of compute/post_shader/main.gd
 *
 * Toggles post-processing compositor effects (grayscale and custom shader)
 * based on input actions.
 */
@GodotClass(name = "PostShaderMain", parent = "Node3D")
public class PostShaderMain extends Node3D {

    private Object compositor;
    private boolean initialized = false;

    @Override
    public void _ready() {
        if (initialized) return;
        initialized = true;

        Object worldEnv = getNode("WorldEnvironment");
        if (worldEnv != null) {
            compositor = callOn(worldEnv, "get", "compositor");
        }
    }

    @Override
    public boolean _input(Object inputEvent) {
        Object inputSingleton = call("Input.singleton");

        boolean toggleGrayscale = (boolean) callOn(inputSingleton, "is_action_pressed", "toggle_grayscale_effect");
        if (toggleGrayscale) {
            toggleEffect(0);
            updateInfoText();
        }

        boolean toggleShader = (boolean) callOn(inputSingleton, "is_action_pressed", "toggle_shader_effect");
        if (toggleShader) {
            toggleEffect(1);
            updateInfoText();
        }

        return false;
    }

    private void toggleEffect(int index) {
        if (compositor == null) return;
        Object effects = callOn(compositor, "get", "compositor_effects");
        if (effects instanceof Object[]) {
            Object[] effectsArray = (Object[]) effects;
            if (index < effectsArray.length && effectsArray[index] != null) {
                boolean enabled = (boolean) callOn(effectsArray[index], "get", "enabled");
                callOn(effectsArray[index], "set", "enabled", !enabled);
            }
        }
    }

    private void updateInfoText() {
        Object info = getNode("Info");
        if (info == null || compositor == null) return;

        Object effects = callOn(compositor, "get", "compositor_effects");
        String grayscaleStatus = "Disabled";
        String shaderStatus = "Disabled";

        if (effects instanceof Object[]) {
            Object[] effectsArray = (Object[]) effects;
            if (effectsArray.length > 0 && effectsArray[0] != null) {
                boolean enabled = (boolean) callOn(effectsArray[0], "get", "enabled");
                grayscaleStatus = enabled ? "Enabled" : "Disabled";
            }
            if (effectsArray.length > 1 && effectsArray[1] != null) {
                boolean enabled = (boolean) callOn(effectsArray[1], "get", "enabled");
                shaderStatus = enabled ? "Enabled" : "Disabled";
            }
        }

        callOn(info, "set", "text",
            "Grayscale effect: " + grayscaleStatus + "\n" +
            "Shader effect: " + shaderStatus + "\n");
    }

    private Object callOn(Object obj, String method, Object... args) {
        if (obj instanceof org.godot.Godot) {
            return ((org.godot.Godot) obj).call(method, args);
        }
        return null;
    }
}
