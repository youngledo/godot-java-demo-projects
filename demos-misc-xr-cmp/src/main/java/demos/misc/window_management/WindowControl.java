package demos.misc.window_management;

import org.godot.annotation.GodotClass;
import org.godot.annotation.GodotMethod;
import org.godot.math.Vector2;
import org.godot.math.Vector2i;
import org.godot.node.Button;
import org.godot.node.CanvasItem;
import org.godot.node.Control;
import org.godot.node.InputEventKey;
import org.godot.node.InputEventMouseMotion;
import org.godot.node.Label;
import org.godot.singleton.DisplayServer;
import org.godot.singleton.Input;
import org.godot.singleton.OS;

@GodotClass(name = "WindowControl", parent = "Control")
public class WindowControl extends Control {

    private Observer observer;
    private Vector2 mousePosition = new Vector2();
    private boolean initialized = false;

    @Override
    public void _ready() {
        if (initialized) return;
        initialized = true;

        observer = getNodeAs("../Observer", Observer.class);

        OS os = OS.singleton();
        DisplayServer displayServer = DisplayServer.singleton();

        if (os.hasFeature("web")) {
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
                Button button = getNodeAs(path, Button.class);
                if (button != null) {
                    button.setDisabled(true);
                    button.setText(button.getText() + " (not supported on Web)");
                }
            }
        }

        if (!checkWmApi()) {
            setPhysicsProcess(false);
            setProcessInput(false);
        }

        Label refreshLabel = getNodeAs("Labels/Label_Screen0_RefreshRate", Label.class);
        if (refreshLabel != null) {
            double rate = displayServer.screenGetRefreshRate();
            refreshLabel.setText(String.format("Screen0 Refresh Rate: %.2f Hz", rate));
        }

        int screenCount = displayServer.getScreenCount();
        if (screenCount > 1) {
            Label refreshLabel1 = getNodeAs("Labels/Label_Screen1_RefreshRate", Label.class);
            if (refreshLabel1 != null) {
                double rate1 = displayServer.screenGetRefreshRate(1);
                refreshLabel1.setText(String.format("Screen1 Refresh Rate: %.2f Hz", rate1));
            }
        }
    }

    @Override
    public void _physicsProcess(double delta) {
        DisplayServer displayServer = DisplayServer.singleton();
        Input input = Input.singleton();

        DisplayServer.WindowMode windowMode = displayServer.windowGetMode();
        String modeText = "Mode: ";
        if (windowMode == DisplayServer.WindowMode.WINDOW_MODE_FULLSCREEN) {
            modeText += "Fullscreen\n";
        } else {
            modeText += "Windowed\n";
        }

        boolean resizeDisabled = displayServer.windowGetFlag(DisplayServer.WindowFlags.WINDOW_FLAG_RESIZE_DISABLED);
        if (resizeDisabled) modeText += "Fixed Size\n";
        if (windowMode == DisplayServer.WindowMode.WINDOW_MODE_MINIMIZED) modeText += "Minimized\n";
        if (windowMode == DisplayServer.WindowMode.WINDOW_MODE_MAXIMIZED) modeText += "Maximized\n";

        long mouseMode = input.getMouseMode();
        CanvasItem keyInfoLabel = getNodeAs("Buttons/Label_MouseModeCaptured_KeyInfo", CanvasItem.class);
        if (mouseMode == 2) {
            modeText += "Mouse Captured\n";
            if (keyInfoLabel != null) keyInfoLabel.show();
        } else if (keyInfoLabel != null) {
            keyInfoLabel.hide();
        }

        setLabelText("Labels/Label_Mode", modeText);
        setLabelText("Labels/Label_Position", "Position: " + displayServer.windowGetPosition());
        setLabelText("Labels/Label_Size", "Size: " + displayServer.windowGetSize());
        setLabelText("Labels/Label_MousePosition", String.format("Mouse Position: %.4s", mousePosition));
        setLabelText("Labels/Label_Screen_Count", "Screen_Count: " + displayServer.getScreenCount());
        setLabelText("Labels/Label_Screen_Current", "Screen: " + displayServer.windowGetCurrentScreen());
        setLabelText("Labels/Label_Screen0_Resolution", "Screen0 Resolution:\n" + displayServer.screenGetSize());
        setLabelText("Labels/Label_Screen0_Position", "Screen0 Position:\n" + displayServer.screenGetPosition());
        setLabelText("Labels/Label_Screen0_DPI", "Screen0 DPI: " + displayServer.screenGetDpi());

        int screenCount = displayServer.getScreenCount();
        CanvasItem screen0Btn = getNodeAs("Buttons/Button_Screen0", CanvasItem.class);
        CanvasItem screen1Btn = getNodeAs("Buttons/Button_Screen1", CanvasItem.class);

        if (screenCount > 1) {
            if (screen0Btn != null) screen0Btn.show();
            if (screen1Btn != null) screen1Btn.show();
            setNodeVisible("Labels/Label_Screen1_Resolution", true);
            setNodeVisible("Labels/Label_Screen1_Position", true);
            setNodeVisible("Labels/Label_Screen1_DPI", true);
            setLabelText("Labels/Label_Screen1_Resolution", "Screen1 Resolution:\n" + displayServer.screenGetSize(1));
            setLabelText("Labels/Label_Screen1_Position", "Screen1 Position:\n" + displayServer.screenGetPosition(1));
            setLabelText("Labels/Label_Screen1_DPI", "Screen1 DPI: " + displayServer.screenGetDpi(1));
        } else {
            if (screen0Btn != null) screen0Btn.hide();
            if (screen1Btn != null) screen1Btn.hide();
            setNodeVisible("Labels/Label_Screen1_Resolution", false);
            setNodeVisible("Labels/Label_Screen1_Position", false);
            setNodeVisible("Labels/Label_Screen1_DPI", false);
            setNodeVisible("Labels/Label_Screen1_RefreshRate", false);
        }

        setButtonPressed("Buttons/Button_Fullscreen", windowMode == DisplayServer.WindowMode.WINDOW_MODE_FULLSCREEN);
        setButtonPressed("Buttons/Button_FixedSize", resizeDisabled);
        setButtonPressed("Buttons/Button_Minimized", windowMode == DisplayServer.WindowMode.WINDOW_MODE_MINIMIZED);
        setButtonPressed("Buttons/Button_Maximized", windowMode == DisplayServer.WindowMode.WINDOW_MODE_MAXIMIZED);
        setButtonPressed("Buttons/Button_MouseModeVisible", mouseMode == 0);
        setButtonPressed("Buttons/Button_MouseModeHidden", mouseMode == 1);
        setButtonPressed("Buttons/Button_MouseModeCaptured", mouseMode == 2);
    }

    @Override
    public boolean _input(Object inputEvent) {
        if (inputEvent instanceof InputEventMouseMotion event) {
            mousePosition = event.getPosition();
        }

        if (inputEvent instanceof InputEventKey) {
            Input input = Input.singleton();

            if (input.isActionPressed("mouse_mode_visible")) {
                setObserverState(0);
                OnButtonMouseModeVisiblePressed();
            }
            if (input.isActionPressed("mouse_mode_hidden")) {
                setObserverState(0);
                OnButtonMouseModeHiddenPressed();
            }
            if (input.isActionPressed("mouse_mode_captured")) {
                OnButtonMouseModeCapturedPressed();
            }
            if (input.isActionPressed("mouse_mode_confined")) {
                setObserverState(0);
                OnButtonMouseModeConfinedPressed();
            }
            if (input.isActionPressed("mouse_mode_confined_hidden")) {
                setObserverState(0);
                OnButtonMouseModeConfinedHiddenPressed();
            }
        }
        return false;
    }

    private void setObserverState(long state) {
        if (observer != null) {
            observer.state = state;
        }
    }

    private void setLabelText(String path, String text) {
        Label label = getNodeAs(path, Label.class);
        if (label != null) label.setText(text);
    }

    private void setNodeVisible(String path, boolean visible) {
        CanvasItem node = getNodeAs(path, CanvasItem.class);
        if (node != null) {
            if (visible) node.show();
            else node.hide();
        }
    }

    private void setButtonPressed(String path, boolean pressed) {
        Button button = getNodeAs(path, Button.class);
        if (button != null) button.setButtonPressed(pressed);
    }

    private boolean checkWmApi() {
        DisplayServer displayServer = DisplayServer.singleton();
        String s = "";
        if (!displayServer.hasMethod("get_screen_count")) s += " - getScreenCount()\n";
        if (!displayServer.hasMethod("window_get_current_screen")) s += " - windowGetCurrentScreen()\n";
        if (!displayServer.hasMethod("window_set_current_screen")) s += " - windowSetCurrentScreen()\n";
        if (!displayServer.hasMethod("screen_get_position")) s += " - screenGetPosition()\n";
        if (!displayServer.hasMethod("window_get_size")) s += " - windowGetSize()\n";
        if (!displayServer.hasMethod("window_get_position")) s += " - windowGetPosition()\n";
        if (!displayServer.hasMethod("window_set_position")) s += " - windowSetPosition()\n";
        if (!displayServer.hasMethod("window_set_size")) s += " - windowSetSize()\n";

        if (s.isEmpty()) return true;

        Label dialogText = getNodeAs("ImplementationDialog/Text", Label.class);
        if (dialogText != null) {
            dialogText.setText(dialogText.getText() + s);
        }
        CanvasItem dialog = getNodeAs("ImplementationDialog", CanvasItem.class);
        if (dialog != null) dialog.show();
        return false;
    }

    @GodotMethod
    public void OnButtonMoveToPressed() {
        DisplayServer.singleton().windowSetPosition(new Vector2i(100, 100));
    }

    @GodotMethod
    public void OnButtonResizePressed() {
        DisplayServer.singleton().windowSetSize(new Vector2i(1280, 720));
    }

    @GodotMethod
    public void OnButtonScreen0Pressed() {
        DisplayServer.singleton().windowSetCurrentScreen(0);
    }

    @GodotMethod
    public void OnButtonScreen1Pressed() {
        DisplayServer.singleton().windowSetCurrentScreen(1);
    }

    @GodotMethod
    public void OnButtonFullscreenPressed() {
        DisplayServer displayServer = DisplayServer.singleton();
        if (displayServer.windowGetMode() == DisplayServer.WindowMode.WINDOW_MODE_FULLSCREEN) {
            displayServer.windowSetMode(DisplayServer.WindowMode.WINDOW_MODE_WINDOWED);
        } else {
            displayServer.windowSetMode(DisplayServer.WindowMode.WINDOW_MODE_FULLSCREEN);
        }
    }

    @GodotMethod
    public void OnButtonFixedSizePressed() {
        DisplayServer displayServer = DisplayServer.singleton();
        displayServer.windowSetFlag(DisplayServer.WindowFlags.WINDOW_FLAG_RESIZE_DISABLED, !displayServer.windowGetFlag(DisplayServer.WindowFlags.WINDOW_FLAG_RESIZE_DISABLED));
    }

    @GodotMethod
    public void OnButtonMinimizedPressed() {
        DisplayServer displayServer = DisplayServer.singleton();
        if (displayServer.windowGetMode() == DisplayServer.WindowMode.WINDOW_MODE_MINIMIZED) {
            displayServer.windowSetMode(DisplayServer.WindowMode.WINDOW_MODE_WINDOWED);
        } else {
            displayServer.windowSetMode(DisplayServer.WindowMode.WINDOW_MODE_MINIMIZED);
        }
    }

    @GodotMethod
    public void OnButtonMaximizedPressed() {
        DisplayServer displayServer = DisplayServer.singleton();
        if (displayServer.windowGetMode() == DisplayServer.WindowMode.WINDOW_MODE_FULLSCREEN) {
            displayServer.windowSetMode(DisplayServer.WindowMode.WINDOW_MODE_MINIMIZED);
        } else {
            displayServer.windowSetMode(DisplayServer.WindowMode.WINDOW_MODE_FULLSCREEN);
        }
    }

    @GodotMethod
    public void OnButtonMouseModeVisiblePressed() {
        Input.singleton().setMouseMode(0);
    }

    @GodotMethod
    public void OnButtonMouseModeHiddenPressed() {
        Input.singleton().setMouseMode(1);
    }

    @GodotMethod
    public void OnButtonMouseModeCapturedPressed() {
        Input.singleton().setMouseMode(2);
        setObserverState(1);
    }

    @GodotMethod
    public void OnButtonMouseModeConfinedPressed() {
        Input.singleton().setMouseMode(3);
    }

    @GodotMethod
    public void OnButtonMouseModeConfinedHiddenPressed() {
        Input.singleton().setMouseMode(4);
    }
}
