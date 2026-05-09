package demos.misc.noise_viewer;

import org.godot.annotation.GodotClass;
import org.godot.annotation.GodotMethod;
import org.godot.node.Control;
import org.godot.node.Node;
import org.godot.node.ShaderMaterial;
import org.godot.singleton.OS;

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

        Node seamlessTexture = getNode("SeamlessNoiseTexture");
        if (seamlessTexture != null) {
            Object texture = seamlessTexture.getProperty("texture");
            if (texture instanceof org.godot.Godot godotTexture) {
                Object noiseValue = godotTexture.getProperty("noise");
                if (noiseValue instanceof org.godot.Godot godotNoise) {
                    noise = godotNoise;
                }
            }
        }

        if (noise != null) {
            Node seedBox = getNode("ParameterContainer/SeedSpinBox");
            Node freqBox = getNode("ParameterContainer/FrequencySpinBox");
            Node octavesBox = getNode("ParameterContainer/FractalOctavesSpinBox");
            Node gainBox = getNode("ParameterContainer/FractalGainSpinBox");
            Node lacBox = getNode("ParameterContainer/FractalLacunaritySpinBox");

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

        Node seamlessTexture = getNode("SeamlessNoiseTexture");
        if (seamlessTexture != null && seamlessTexture.getProperty("material") instanceof ShaderMaterial material) {
            material.setShaderParameter("min_value", min);
            material.setShaderParameter("max_value", max);
        }
    }

    @GodotMethod
    public void OnDocumentationButtonPressed() {
        OS.singleton().shellOpen("https://docs.godotengine.org/en/latest/classes/class_fastnoiselite.html");
    }

    @GodotMethod
    public void OnRandomSeedButtonPressed() {
        double randomVal = Math.random() * (2147483648.0 - (-2147483648.0)) + (-2147483648.0);
        Node seedBox = getNode("ParameterContainer/SeedSpinBox");
        if (seedBox != null) {
            seedBox.setProperty("value", Math.floor(randomVal));
        }
    }

    @GodotMethod
    public void OnSeedSpinBoxValueChanged(double value) {
        if (noise != null) noise.setProperty("seed", (int) value);
    }

    @GodotMethod
    public void OnFrequencySpinBoxValueChanged(double value) {
        if (noise != null) noise.setProperty("frequency", value);
    }

    @GodotMethod
    public void OnFractalOctavesSpinBoxValueChanged(double value) {
        if (noise != null) noise.setProperty("fractal_octaves", (int) value);
    }

    @GodotMethod
    public void OnFractalGainSpinBoxValueChanged(double value) {
        if (noise != null) noise.setProperty("fractal_gain", value);
    }

    @GodotMethod
    public void OnFractalLacunaritySpinBoxValueChanged(double value) {
        if (noise != null) noise.setProperty("fractal_lacunarity", value);
    }

    @GodotMethod
    public void OnMinClipSpinBoxValueChanged(double value) {
        minNoise = value;
        refreshShaderParams();
    }

    @GodotMethod
    public void OnMaxClipSpinBoxValueChanged(double value) {
        maxNoise = value;
        refreshShaderParams();
    }
}
