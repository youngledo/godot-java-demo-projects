package demos.misc.window_management;

import org.godot.annotation.GodotClass;
import org.godot.annotation.GodotMethod;
import org.godot.math.Vector2;
import org.godot.node.Control;
import org.godot.node.Node;

@GodotClass(name = "WindowControl", parent = "Control")
public class WindowControl extends Control {

    private org.godot.node.Node observer;
    private org.godot.math.Vector2 mousePosition = new Vector2();
    private boolean initialized = false;

    @Override
    public void _ready() {
        if (initialized) return;
        initialized = true;

        observer = getNode("../Observer");

        org.godot.singleton.OS os = org.godot.singleton.OS.singleton();
        org.godot.singleton.DisplayServer ds = org.godot.singleton.DisplayServer.singleton();

        if ((boolean) os.call("has_feature", "web")) {
            String[] buttonPaths = {
                "Buttons/Button_FixedSize",
                "Buttons/Button_Minimized",
                "Buttons/Button_Maximized",
                "Buttons/Button_MoveTo",
                "Buttons/Button_Resize",
                "Buttons/Button_MouseModeConfined",
                "Buttons/Button_MouseModeConfinedHidden",
                "CheckButton"
            };
            for (String path : buttonPaths) {
                org.godot.node.Node btn = getNode(path);
                if (btn != null) {
                    btn.setProperty("disabled", true);
                    String text = (String) btn.getProperty("text");
                    btn.setProperty("text", text + " (not supported on Web)");
                }
            }
        }

        if (!checkWmApi() ) {
            setPhysicsProcess(false);
            setProcessInput(false);
        }

        org.godot.node.Node refreshLabel = getNode("Labels/Label_Screen0_RefreshRate");
        if (refreshLabel != null) {
            double rate = (double) ds.call("screen_get_refresh_rate");
            refreshLabel.setProperty("text", String.format("Screen0 Refresh Rate: %.2f Hz", rate));
        }

        long screenCount = (long) ds.call("get_screen_count");
        if (screenCount > 1) {
            org.godot.node.Node refreshLabel1 = getNode("Labels/Label_Screen1_RefreshRate");
            if (refreshLabel1 != null) {
                double rate1 = (double) ds.call("screen_get_refresh_rate", 1);
                refreshLabel1.setProperty("text", String.format("Screen1 Refresh Rate: %.2f Hz", rate1));
            }
        }
    }

    @Override
    public void _physicsProcess(double delta) {
        org.godot.singleton.DisplayServer ds = org.godot.singleton.DisplayServer.singleton();
        org.godot.singleton.Input input = org.godot.singleton.Input.singleton();

        long windowMode = (long) ds.call("window_get_mode");
        String modeText = "Mode: ";
        if (windowMode == 3) { // WINDOW_MODE_FULLSCREEN
            modeText += "Fullscreen\n";
        } else {
            modeText += "Windowed\n";
        }

        boolean resizeDisabled = (boolean) ds.call("window_get_flag", 0); // WINDOW_FLAG_RESIZE_DISABLED
        if (resizeDisabled) modeText += "Fixed Size\n";
        if (windowMode == 2) modeText += "Minimized\n"; // WINDOW_MODE_MINIMIZED
        if (windowMode == 3) modeText += "Maximized\n";

        long mouseMode = (long) input.getMouseMode();
        org.godot.node.CanvasItem keyInfoLabel = (org.godot.node.CanvasItem) getNode("Buttons/Label_MouseModeCaptured_KeyInfo");
        if (mouseMode == 2) { // MOUSE_MODE_CAPTURED
            modeText += "Mouse Captured\n";
            if (keyInfoLabel != null) keyInfoLabel.show();
        } else {
            if (keyInfoLabel != null) keyInfoLabel.hide();
        }

        setLabelText("Labels/Label_Mode", modeText);
        setLabelText("Labels/Label_Position", "Position: " + ds.call("window_get_position"));
        setLabelText("Labels/Label_Size", "Size: " + ds.call("window_get_size"));
        setLabelText("Labels/Label_MousePosition", String.format("Mouse Position: %.4s", mousePosition));
        setLabelText("Labels/Label_Screen_Count", "Screen_Count: " + ds.call("get_screen_count"));
        setLabelText("Labels/Label_Screen_Current", "Screen: " + ds.call("window_get_current_screen"));
        setLabelText("Labels/Label_Screen0_Resolution", "Screen0 Resolution:\n" + ds.call("screen_get_size"));
        setLabelText("Labels/Label_Screen0_Position", "Screen0 Position:\n" + ds.call("screen_get_position"));
        setLabelText("Labels/Label_Screen0_DPI", "Screen0 DPI: " + ds.call("screen_get_dpi"));

        long screenCount = (long) ds.call("get_screen_count");
        org.godot.node.CanvasItem screen0Btn = (org.godot.node.CanvasItem) getNode("Buttons/Button_Screen0");
        org.godot.node.CanvasItem screen1Btn = (org.godot.node.CanvasItem) getNode("Buttons/Button_Screen1");

        if (screenCount > 1) {
            if (screen0Btn != null) screen0Btn.show();
            if (screen1Btn != null) screen1Btn.show();
            setNodeVisible("Labels/Label_Screen1_Resolution", true);
            setNodeVisible("Labels/Label_Screen1_Position", true);
            setNodeVisible("Labels/Label_Screen1_DPI", true);
            setLabelText("Labels/Label_Screen1_Resolution", "Screen1 Resolution:\n" + ds.call("screen_get_size", 1));
            setLabelText("Labels/Label_Screen1_Position", "Screen1 Position:\n" + ds.call("screen_get_position", 1));
            setLabelText("Labels/Label_Screen1_DPI", "Screen1 DPI: " + ds.call("screen_get_dpi", 1));
        } else {
            if (screen0Btn != null) screen0Btn.hide();
            if (screen1Btn != null) screen1Btn.hide();
            setNodeVisible("Labels/Label_Screen1_Resolution", false);
            setNodeVisible("Labels/Label_Screen1_Position", false);
            setNodeVisible("Labels/Label_Screen1_DPI", false);
            setNodeVisible("Labels/Label_Screen1_RefreshRate", false);
        }

        setButtonPressed("Buttons/Button_Fullscreen", windowMode == 3);
        setButtonPressed("Buttons/Button_FixedSize", resizeDisabled);
        setButtonPressed("Buttons/Button_Minimized", windowMode == 2);
        setButtonPressed("Buttons/Button_Maximized", windowMode == 3);
        setButtonPressed("Buttons/Button_MouseModeVisible", mouseMode == 0);
        setButtonPressed("Buttons/Button_MouseModeHidden", mouseMode == 1);
        setButtonPressed("Buttons/Button_MouseModeCaptured", mouseMode == 2);
    }

    @Override
    public boolean _input(Object inputEvent) {
        if (inputEvent instanceof org.godot.Godot) {
            org.godot.Godot evt = (org.godot.Godot) inputEvent;
            String className = (String) evt.call("get_class");

            if ("InputEventMouseMotion".equals(className)) {
                mousePosition = (Vector2) evt.getProperty("position");
            }

            if ("InputEventKey".equals(className)) {
                org.godot.singleton.Input input = org.godot.singleton.Input.singleton();

                if ((boolean) input.isActionPressed("mouse_mode_visible")) {
                    setObserverState(0); // MENU
                    OnButtonMouseModeVisiblePressed();
                }
                if ((boolean) input.isActionPressed("mouse_mode_hidden")) {
                    setObserverState(0);
                    OnButtonMouseModeHiddenPressed();
                }
                if ((boolean) input.isActionPressed("mouse_mode_captured")) {
                    OnButtonMouseModeCapturedPressed();
                }
                if ((boolean) input.isActionPressed("mouse_mode_confined")) {
                    setObserverState(0);
                    OnButtonMouseModeConfinedPressed();
                }
                if ((boolean) input.isActionPressed("mouse_mode_confined_hidden")) {
                    setObserverState(0);
                    OnButtonMouseModeConfinedHiddenPressed();
                }
            }
        }
        return false;
    }

    private void setObserverState(long state) {
        if (observer != null) {
            observer.setProperty("state", state);
        }
    }

    private void setLabelText(String path, String text) {
        org.godot.node.Node label = getNode(path);
        if (label != null) label.setProperty("text", text);
    }

    private void setNodeVisible(String path, boolean visible) {
        org.godot.node.CanvasItem node = (org.godot.node.CanvasItem) getNode(path);
        if (node != null) {
            if (visible) node.show();
            else node.hide();
        }
    }

    private void setButtonPressed(String path, boolean pressed) {
        org.godot.node.Node btn = getNode(path);
        if (btn != null) btn.setProperty("button_pressed", pressed);
    }

    private boolean checkWmApi() {
        org.godot.singleton.DisplayServer ds = org.godot.singleton.DisplayServer.singleton();
        String s = "";
        if (!(boolean) ds.hasMethod("get_screen_count")) s += " - getScreenCount()\n";
        if (!(boolean) ds.hasMethod("window_get_current_screen")) s += " - windowGetCurrentScreen()\n";
        if (!(boolean) ds.hasMethod("window_set_current_screen")) s += " - windowSetCurrentScreen()\n";
        if (!(boolean) ds.hasMethod("screen_get_position")) s += " - screenGetPosition()\n";
        if (!(boolean) ds.hasMethod("window_get_size")) s += " - windowGetSize()\n";
        if (!(boolean) ds.hasMethod("window_get_position")) s += " - windowGetPosition()\n";
        if (!(boolean) ds.hasMethod("window_set_position")) s += " - windowSetPosition()\n";
        if (!(boolean) ds.hasMethod("window_set_size")) s += " - windowSetSize()\n";

        if (s.isEmpty()) return true;;

        org.godot.node.CanvasItem dialogText = (org.godot.node.CanvasItem) getNode("ImplementationDialog/Text");
        if (dialogText != null) {
            String currentText = (String) dialogText.getProperty("text");
            dialogText.setProperty("text", currentText + s);
        }
        org.godot.node.CanvasItem dialog = (org.godot.node.CanvasItem) getNode("ImplementationDialog");
        if (dialog != null) dialog.show();
        return false;
    }

    @GodotMethod
    public void OnButtonMoveToPressed() {
        org.godot.singleton.DisplayServer.singleton().call("window_set_position", new Vector2(100, 100));
    }

    @GodotMethod
    public void OnButtonResizePressed() {
        org.godot.singleton.DisplayServer.singleton().call("window_set_size", new Vector2(1280, 720));
    }

    @GodotMethod
    public void OnButtonScreen0Pressed() {
        org.godot.singleton.DisplayServer.singleton().call("window_set_current_screen", 0);
    }

    @GodotMethod
    public void OnButtonScreen1Pressed() {
        org.godot.singleton.DisplayServer.singleton().call("window_set_current_screen", 1);
    }

    @GodotMethod
    public void OnButtonFullscreenPressed() {
        org.godot.singleton.DisplayServer ds = org.godot.singleton.DisplayServer.singleton();
        long mode = (long) ds.call("window_get_mode");
        if (mode == 3) { // WINDOW_MODE_FULLSCREEN
            ds.call("window_set_mode", 0); // WINDOW_MODE_WINDOWED
        } else {
            ds.call("window_set_mode", 3);
        }
    }

    @GodotMethod
    public void OnButtonFixedSizePressed() {
        org.godot.singleton.DisplayServer ds = org.godot.singleton.DisplayServer.singleton();
        boolean flag = (boolean) ds.call("window_get_flag", 0); // WINDOW_FLAG_RESIZE_DISABLED
        ds.call("window_set_flag", 0, !flag);
    }

    @GodotMethod
    public void OnButtonMinimizedPressed() {
        org.godot.singleton.DisplayServer ds = org.godot.singleton.DisplayServer.singleton();
        long mode = (long) ds.call("window_get_mode");
        if (mode == 2) { // WINDOW_MODE_MINIMIZED
            ds.call("window_set_mode", 0);
        } else {
            ds.call("window_set_mode", 2);
        }
    }

    @GodotMethod
    public void OnButtonMaximizedPressed() {
        org.godot.singleton.DisplayServer ds = org.godot.singleton.DisplayServer.singleton();
        long mode = (long) ds.call("window_get_mode");
        if (mode == 3) { // WINDOW_MODE_MAXIMIZED
            ds.call("window_set_mode", 2);
        } else {
            ds.call("window_set_mode", 3);
        }
    }

    @GodotMethod
    public void OnButtonMouseModeVisiblePressed() {
        org.godot.singleton.Input.singleton().setProperty("mouse_mode", 0);
    }

    @GodotMethod
    public void OnButtonMouseModeHiddenPressed() {
        org.godot.singleton.Input.singleton().setProperty("mouse_mode", 1);
    }

    @GodotMethod
    public void OnButtonMouseModeCapturedPressed() {
        org.godot.singleton.Input.singleton().setProperty("mouse_mode", 2);
        setObserverState(1); // GRAB
    }

    @GodotMethod
    public void OnButtonMouseModeConfinedPressed() {
        org.godot.singleton.Input.singleton().setProperty("mouse_mode", 3);
    }

    @GodotMethod
    public void OnButtonMouseModeConfinedHiddenPressed() {
        org.godot.singleton.Input.singleton().setProperty("mouse_mode", 4);
    }
}
