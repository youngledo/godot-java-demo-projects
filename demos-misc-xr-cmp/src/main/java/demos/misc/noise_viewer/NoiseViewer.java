package demos.misc.noise_viewer;

import org.godot.annotation.GodotClass;
import org.godot.annotation.GodotMethod;
import org.godot.node.Control;

@GodotClass(name = "NoiseViewer", parent = "Control")
public class NoiseViewer extends Control {

    private org.godot.Godot noise;
    private double minNoise = -1.0;
    private double maxNoise = 1.0;
    private boolean initialized = false;

    @Override
    public void _ready() {
        if (initialized) return;
        initialized = true;

        org.godot.Godot seamlessTexture = (org.godot.Godot) call("get_node", "SeamlessNoiseTexture");
        if (seamlessTexture != null) {
            Object texture = seamlessTexture.getProperty("texture");
            if (texture instanceof org.godot.Godot) {
                noise = (org.godot.Godot) ((org.godot.Godot) texture).getProperty("noise");
            }
        }

        // Set up noise with basic info
        if (noise != null) {
            org.godot.Godot seedBox = (org.godot.Godot) call("get_node", "ParameterContainer/SeedSpinBox");
            org.godot.Godot freqBox = (org.godot.Godot) call("get_node", "ParameterContainer/FrequencySpinBox");
            org.godot.Godot octavesBox = (org.godot.Godot) call("get_node", "ParameterContainer/FractalOctavesSpinBox");
            org.godot.Godot gainBox = (org.godot.Godot) call("get_node", "ParameterContainer/FractalGainSpinBox");
            org.godot.Godot lacBox = (org.godot.Godot) call("get_node", "ParameterContainer/FractalLacunaritySpinBox");

            if (seedBox != null) seedBox.setProperty("value", noise.getProperty("seed"));
            if (freqBox != null) freqBox.setProperty("value", noise.getProperty("frequency"));
            if (octavesBox != null) octavesBox.setProperty("value", noise.getProperty("fractal_octaves"));
            if (gainBox != null) gainBox.setProperty("value", noise.getProperty("fractal_gain"));
            if (lacBox != null) lacBox.setProperty("value", noise.getProperty("fractal_lacunarity"));
        }

        refreshShaderParams();
    }

    private void refreshShaderParams() {
        double min = (minNoise + 1) / 2.0;
        double max = (maxNoise + 1) / 2.0;

        org.godot.Godot seamlessTexture = (org.godot.Godot) call("get_node", "SeamlessNoiseTexture");
        if (seamlessTexture != null) {
            org.godot.Godot material = (org.godot.Godot) seamlessTexture.getProperty("material");
            if (material != null) {
                material.call("set_shader_parameter", "min_value", min);
                material.call("set_shader_parameter", "max_value", max);
            }
        }
    }

    @GodotMethod
    public void _on_documentation_button_pressed() {
        org.godot.singleton.OS.singleton().call("shell_open",
            "https://docs.godotengine.org/en/latest/classes/class_fastnoiselite.html");
    }

    @GodotMethod
    public void _on_random_seed_button_pressed() {
        double randomVal = Math.random() * (2147483648.0 - (-2147483648.0)) + (-2147483648.0);
        org.godot.Godot seedBox = (org.godot.Godot) call("get_node", "ParameterContainer/SeedSpinBox");
        if (seedBox != null) {
            seedBox.setProperty("value", Math.floor(randomVal));
        }
    }

    @GodotMethod
    public void _on_seed_spin_box_value_changed(double value) {
        if (noise != null) noise.setProperty("seed", (int) value);
    }

    @GodotMethod
    public void _on_frequency_spin_box_value_changed(double value) {
        if (noise != null) noise.setProperty("frequency", value);
    }

    @GodotMethod
    public void _on_fractal_octaves_spin_box_value_changed(double value) {
        if (noise != null) noise.setProperty("fractal_octaves", (int) value);
    }

    @GodotMethod
    public void _on_fractal_gain_spin_box_value_changed(double value) {
        if (noise != null) noise.setProperty("fractal_gain", value);
    }

    @GodotMethod
    public void _on_fractal_lacunarity_spin_box_value_changed(double value) {
        if (noise != null) noise.setProperty("fractal_lacunarity", value);
    }

    @GodotMethod
    public void _on_min_clip_spin_box_value_changed(double value) {
        minNoise = value;
        refreshShaderParams();
    }

    @GodotMethod
    public void _on_max_clip_spin_box_value_changed(double value) {
        maxNoise = value;
        refreshShaderParams();
    }
}
