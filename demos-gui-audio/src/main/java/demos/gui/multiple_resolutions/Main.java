package demos.gui.multiple_resolutions;

import org.godot.annotation.GodotClass;
import org.godot.annotation.GodotMethod;
import org.godot.core.Callable;
import org.godot.math.Vector2;
import org.godot.math.Vector2i;
import org.godot.node.AspectRatioContainer;
import org.godot.node.Control;
import org.godot.node.Label;
import org.godot.node.OptionButton;
import org.godot.node.Panel;
import org.godot.node.Slider;
import org.godot.node.Window;
import org.godot.singleton.ProjectSettings;

@GodotClass(name = "Main", parent = "Control")
public class Main extends Control {

    private Vector2i baseWindowSize;
    private int stretchMode = 1;
    private int stretchAspect = 4;
    private double scaleFactor = 1.0;
    private double guiAspectRatio = -1.0;
    private double guiMargin = 0.0;

    private Panel panel;
    private AspectRatioContainer arc;
    private boolean initialized = false;

    @Override
    public void _ready() {
        if (initialized) return;
        initialized = true;

        ProjectSettings ps = ProjectSettings.singleton();
        int viewportWidth = settingInt(ps, "display/window/size/viewport_width");
        int viewportHeight = settingInt(ps, "display/window/size/viewport_height");
        baseWindowSize = new Vector2i(viewportWidth, viewportHeight);

        panel = getNodeAs("Panel", Panel.class);
        arc = getNodeAs("Panel/AspectRatioContainer", AspectRatioContainer.class);

        connect("resized", new Callable(this, "_on_resized"));
        callDeferred("updateContainer");
    }

    @GodotMethod
    public void updateContainer() {
        if (panel == null) return;

        for (int i = 0; i < 2; i++) {
            Vector2 panelSize = panel.getSize();
            double panelAspect = panelSize.getX() / panelSize.getY();

            if (Math.abs(guiAspectRatio - (-1.0)) < 0.0001) {
                if (arc != null) arc.setRatio(panelAspect);
                panel.setOffsetTop(guiMargin);
                panel.setOffsetBottom(-guiMargin);
            } else {
                double ratio = Math.min(panelAspect, guiAspectRatio);
                if (arc != null) arc.setRatio(ratio);
                panel.setOffsetTop(guiMargin / guiAspectRatio);
                panel.setOffsetBottom(-guiMargin / guiAspectRatio);
            }

            panel.setOffsetLeft(guiMargin);
            panel.setOffsetRight(-guiMargin);
        }
    }

    @GodotMethod
    public void OnGuiAspectRatioItemSelected(int index) {
        switch (index) {
            case 0 -> guiAspectRatio = -1.0;
            case 1 -> guiAspectRatio = 5.0 / 4.0;
            case 2 -> guiAspectRatio = 4.0 / 3.0;
            case 3 -> guiAspectRatio = 3.0 / 2.0;
            case 4 -> guiAspectRatio = 16.0 / 10.0;
            case 5 -> guiAspectRatio = 16.0 / 9.0;
            case 6 -> guiAspectRatio = 21.0 / 9.0;
        }
        callDeferred("updateContainer");
    }

    @GodotMethod
    public void OnResized() {
        callDeferred("updateContainer");
    }

    @GodotMethod
    public void OnGuiMarginDragEnded(boolean valueChanged) {
        Slider slider = getNodeAs("Panel/AspectRatioContainer/Panel/CenterContainer/Options/GUIMargin/HSlider", Slider.class);
        Label valueLabel = getNodeAs("Panel/AspectRatioContainer/Panel/CenterContainer/Options/GUIMargin/Value", Label.class);
        if (slider != null) {
            guiMargin = slider.getValue();
        }
        if (valueLabel != null) {
            valueLabel.setText(String.valueOf(guiMargin));
        }
        callDeferred("updateContainer");
    }

    @GodotMethod
    public void OnWindowBaseSizeItemSelected(int index) {
        switch (index) {
            case 0 -> baseWindowSize = new Vector2i(648, 648);
            case 1 -> baseWindowSize = new Vector2i(640, 480);
            case 2 -> baseWindowSize = new Vector2i(720, 480);
            case 3 -> baseWindowSize = new Vector2i(800, 600);
            case 4 -> baseWindowSize = new Vector2i(1152, 648);
            case 5 -> baseWindowSize = new Vector2i(1280, 720);
            case 6 -> baseWindowSize = new Vector2i(1280, 800);
            case 7 -> baseWindowSize = new Vector2i(1680, 720);
        }
        Window window = getWindow();
        if (window != null) {
            window.setContentScaleSize(baseWindowSize);
        }
        callDeferred("updateContainer");
    }

    @GodotMethod
    public void OnWindowStretchModeItemSelected(int index) {
        stretchMode = index;
        Window window = getWindow();
        if (window != null) {
            window.setContentScaleMode(stretchMode);
        }

        OptionButton baseSizeOption = getNodeAs("Panel/AspectRatioContainer/Panel/CenterContainer/Options/WindowBaseSize/OptionButton", OptionButton.class);
        OptionButton stretchAspectOption = getNodeAs("Panel/AspectRatioContainer/Panel/CenterContainer/Options/WindowStretchAspect/OptionButton", OptionButton.class);
        if (baseSizeOption != null) baseSizeOption.setDisabled(stretchMode == 0);
        if (stretchAspectOption != null) stretchAspectOption.setDisabled(stretchMode == 0);
    }

    @GodotMethod
    public void OnWindowStretchAspectItemSelected(int index) {
        stretchAspect = index;
        Window window = getWindow();
        if (window != null) {
            window.setContentScaleAspect(stretchAspect);
        }
    }

    @GodotMethod
    public void OnWindowScaleFactorDragEnded(boolean valueChanged) {
        Slider slider = getNodeAs("Panel/AspectRatioContainer/Panel/CenterContainer/Options/WindowScaleFactor/HSlider", Slider.class);
        Label valueLabel = getNodeAs("Panel/AspectRatioContainer/Panel/CenterContainer/Options/WindowScaleFactor/Value", Label.class);
        if (slider != null) {
            scaleFactor = slider.getValue();
        }
        if (valueLabel != null) {
            valueLabel.setText((int) (scaleFactor * 100) + "%");
        }
        Window window = getWindow();
        if (window != null) {
            window.setContentScaleFactor(scaleFactor);
        }
    }

    @GodotMethod
    public void OnWindowStretchScaleModeItemSelected(int index) {
        Window window = getWindow();
        if (window != null) {
            window.setContentScaleStretch(index);
        }
    }

    private int settingInt(ProjectSettings projectSettings, String setting) {
        Object value = projectSettings.getSetting(setting);
        return value instanceof Number number ? number.intValue() : 0;
    }
}
