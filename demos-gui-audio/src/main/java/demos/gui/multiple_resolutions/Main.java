package demos.gui.multiple_resolutions;

import org.godot.annotation.GodotClass;
import org.godot.annotation.GodotMethod;
import org.godot.math.Vector2;
import org.godot.node.Control;
import org.godot.node.Node;

@GodotClass(name = "Main", parent = "Control")
public class Main extends Control {

    private org.godot.math.Vector2 baseWindowSize;
    private int stretchMode = 1; // Window.CONTENT_SCALE_MODE_CANVAS_ITEMS
    private int stretchAspect = 4; // Window.CONTENT_SCALE_ASPECT_EXPAND
    private double scaleFactor = 1.0;
    private double guiAspectRatio = -1.0;
    private double guiMargin = 0.0;

    private org.godot.node.Panel panel;
    private org.godot.node.Node arc;
    private boolean initialized = false;

    @Override
    public void _ready() {
        if (initialized) return;
        initialized = true;

        org.godot.singleton.ProjectSettings ps = org.godot.singleton.ProjectSettings.singleton();
        double viewportWidth = (double) ps.call("get_setting", "display/window/size/viewport_width");
        double viewportHeight = (double) ps.call("get_setting", "display/window/size/viewport_height");
        baseWindowSize = new Vector2(viewportWidth, viewportHeight);

        panel = (org.godot.node.Panel) getNode("Panel");
        arc = getNode("Panel/AspectRatioContainer");

        connect("resized", new org.godot.core.Callable(this, "_on_resized"));
        callDeferred("updateContainer");
    }

    @org.godot.annotation.GodotMethod
    public void updateContainer() {
        for (int i = 0; i < 2; i++) {
            org.godot.math.Vector2 panelSize = panel != null ? (org.godot.math.Vector2) panel.getProperty("size") : null;
            if (panelSize == null) continue;

            double panelAspect = panelSize.getX() / panelSize.getY();

            if (Math.abs(guiAspectRatio - (-1.0)) < 0.0001) {
                // Fit to Window
                if (arc != null) arc.setProperty("ratio", panelAspect);
                if (panel != null) {
                    panel.setProperty("offset_top", guiMargin);
                    panel.setProperty("offset_bottom", -guiMargin);
                }
            } else {
                double ratio = Math.min(panelAspect, guiAspectRatio);
                if (arc != null) arc.setProperty("ratio", ratio);
                if (panel != null) {
                    panel.setProperty("offset_top", guiMargin / guiAspectRatio);
                    panel.setProperty("offset_bottom", -guiMargin / guiAspectRatio);
                }
            }

            if (panel != null) {
                panel.setProperty("offset_left", guiMargin);
                panel.setProperty("offset_right", -guiMargin);
            }
        }
    }

    @GodotMethod
    public void OnGuiAspectRatioItemSelected(int index) {
        switch (index) {
            case 0: guiAspectRatio = -1.0; break; // Fit to Window
            case 1: guiAspectRatio = 5.0 / 4.0; break;
            case 2: guiAspectRatio = 4.0 / 3.0; break;
            case 3: guiAspectRatio = 3.0 / 2.0; break;
            case 4: guiAspectRatio = 16.0 / 10.0; break;
            case 5: guiAspectRatio = 16.0 / 9.0; break;
            case 6: guiAspectRatio = 21.0 / 9.0; break;
        }
        callDeferred("updateContainer");
    }

    @org.godot.annotation.GodotMethod
    public void OnResized() {
        callDeferred("updateContainer");
    }

    @GodotMethod
    public void OnGuiMarginDragEnded(boolean valueChanged) {
        org.godot.node.Slider slider = (org.godot.node.Slider) getNode("Panel/AspectRatioContainer/Panel/CenterContainer/Options/GUIMargin/HSlider");
        org.godot.node.Label valueLabel = (org.godot.node.Label) getNode("Panel/AspectRatioContainer/Panel/CenterContainer/Options/GUIMargin/Value");
        if (slider != null) {
            guiMargin = slider.getProperty("value") != null ? (double) slider.getProperty("value") : 0.0;
        }
        if (valueLabel != null) {
            valueLabel.setProperty("text", String.valueOf(guiMargin));
        }
        callDeferred("updateContainer");
    }

    @GodotMethod
    public void OnWindowBaseSizeItemSelected(int index) {
        switch (index) {
            case 0: baseWindowSize = new Vector2(648, 648); break;
            case 1: baseWindowSize = new Vector2(640, 480); break;
            case 2: baseWindowSize = new Vector2(720, 480); break;
            case 3: baseWindowSize = new Vector2(800, 600); break;
            case 4: baseWindowSize = new Vector2(1152, 648); break;
            case 5: baseWindowSize = new Vector2(1280, 720); break;
            case 6: baseWindowSize = new Vector2(1280, 800); break;
            case 7: baseWindowSize = new Vector2(1680, 720); break;
        }
        org.godot.Godot window = getWindow();
        if (window != null) {
            window.setProperty("content_scale_size", baseWindowSize);
        }
        callDeferred("updateContainer");
    }

    @GodotMethod
    public void OnWindowStretchModeItemSelected(int index) {
        stretchMode = index;
        org.godot.Godot window = getWindow();
        if (window != null) {
            window.setProperty("content_scale_mode", stretchMode);
        }

        org.godot.node.OptionButton baseSizeOption = (org.godot.node.OptionButton) getNode("Panel/AspectRatioContainer/Panel/CenterContainer/Options/WindowBaseSize/OptionButton");
        org.godot.node.OptionButton stretchAspectOption = (org.godot.node.OptionButton) getNode("Panel/AspectRatioContainer/Panel/CenterContainer/Options/WindowStretchAspect/OptionButton");
        if (baseSizeOption != null) baseSizeOption.setProperty("disabled", stretchMode == 0);
        if (stretchAspectOption != null) stretchAspectOption.setProperty("disabled", stretchMode == 0);
    }

    @GodotMethod
    public void OnWindowStretchAspectItemSelected(int index) {
        stretchAspect = index;
        org.godot.Godot window = getWindow();
        if (window != null) {
            window.setProperty("content_scale_aspect", stretchAspect);
        }
    }

    @GodotMethod
    public void OnWindowScaleFactorDragEnded(boolean valueChanged) {
        org.godot.node.Slider slider = (org.godot.node.Slider) getNode("Panel/AspectRatioContainer/Panel/CenterContainer/Options/WindowScaleFactor/HSlider");
        org.godot.node.Label valueLabel = (org.godot.node.Label) getNode("Panel/AspectRatioContainer/Panel/CenterContainer/Options/WindowScaleFactor/Value");
        if (slider != null) {
            scaleFactor = slider.getProperty("value") != null ? (double) slider.getProperty("value") : 1.0;
        }
        if (valueLabel != null) {
            valueLabel.setProperty("text", (int)(scaleFactor * 100) + "%");
        }
        org.godot.Godot window = getWindow();
        if (window != null) {
            window.setProperty("content_scale_factor", scaleFactor);
        }
    }

    @GodotMethod
    public void OnWindowStretchScaleModeItemSelected(int index) {
        org.godot.Godot window = getWindow();
        if (window != null) {
            window.setProperty("content_scale_stretch", index);
        }
    }
}
