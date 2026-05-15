package demos.misc.os_test;

import org.godot.annotation.GodotClass;
import org.godot.annotation.GodotMethod;
import org.godot.core.Callable;
import org.godot.math.Color;
import org.godot.node.Button;
import org.godot.node.Image;
import org.godot.node.Node;
import org.godot.node.SceneTree;
import org.godot.node.SceneTreeTimer;
import org.godot.singleton.DisplayServer;
import org.godot.singleton.Input;
import org.godot.singleton.OS;
import org.godot.singleton.ProjectSettings;

@GodotClass(name = "OSActions", parent = "Node")
public class Actions extends Node {

    private boolean initialized = false;

    @Override
    public void _ready() {
        if (initialized) return;
        initialized = true;

        OS os = OS.singleton();
        if (os.hasFeature("web")) {
            String[] buttonPaths = {
                    "GridContainer/OpenShellFolder",
                    "GridContainer/MoveWindowToForeground",
                    "GridContainer/RequestAttention",
                    "GridContainer/VibrateDeviceShort",
                    "GridContainer/VibrateDeviceLong",
                    "GridContainer/AddGlobalMenuItems",
                    "GridContainer/RemoveGlobalMenuItem",
                    "GridContainer/KillCurrentProcess"
            };
            for (String path : buttonPaths) {
                Button button = getNodeAs(path, Button.class);
                if (button != null) {
                    button.setDisabled(true);
                    button.setText(button.getText() + "\n(not supported on Web)");
                }
            }
        }
    }

    @GodotMethod
    public void OnOpenShellWebPressed() {
        OS.singleton().shellOpen("https://example.com");
    }

    @GodotMethod
    public void OnOpenShellFolderPressed() {
        OS os = OS.singleton();
        String path = os.getEnvironment("HOME");
        if (path == null || path.isEmpty()) {
            path = os.getEnvironment("USERPROFILE");
        }
        if ("macOS".equals(os.getName())) {
            path = "file://" + path;
        }
        os.shellShowInFileManager(path);
    }

    @GodotMethod
    public void OnChangeWindowTitlePressed() {
        DisplayServer.singleton().windowSetTitle("Modified window title. Unicode characters for testing: é € × Ù ¨");
    }

    @GodotMethod
    public void OnChangeWindowIconPressed() {
        DisplayServer displayServer = DisplayServer.singleton();
        if (!displayServer.hasFeature(DisplayServer.Feature.FEATURE_ICON)) {
            OS.singleton().alert("Changing the window icon is not supported by the current display server ("
                    + displayServer.getName() + ").");
            return;
        }
        Image image = Image.create(128, 128, false, Image.Format.FORMAT_RGBA8);
        image.fill(new Color(1, 0.6, 0.3));
        displayServer.setIcon(image);
    }

    @GodotMethod
    public void OnMoveWindowToForegroundPressed() {
        DisplayServer.singleton().windowSetTitle("Will move window to foreground in 5 seconds, try unfocusing the window...");
        SceneTree tree = getTree();
        if (tree != null) {
            SceneTreeTimer timer = tree.createTimer(5.0);
            if (timer != null) {
                timer.connect("timeout", new Callable(this, "MoveToForeground"));
            }
        }
    }

    @GodotMethod
    public void MoveToForeground() {
        DisplayServer displayServer = DisplayServer.singleton();
        displayServer.windowMoveToForeground();
        displayServer.windowSetTitle(String.valueOf(ProjectSettings.singleton().getSetting("application/config/name")));
    }

    @GodotMethod
    public void OnRequestAttentionPressed() {
        DisplayServer.singleton().windowSetTitle("Will request attention in 5 seconds, try unfocusing the window...");
        SceneTree tree = getTree();
        if (tree != null) {
            SceneTreeTimer timer = tree.createTimer(5.0);
            if (timer != null) {
                timer.connect("timeout", new Callable(this, "RequestAttention"));
            }
        }
    }

    @GodotMethod
    public void RequestAttention() {
        DisplayServer displayServer = DisplayServer.singleton();
        displayServer.windowRequestAttention();
        displayServer.windowSetTitle(String.valueOf(ProjectSettings.singleton().getSetting("application/config/name")));
    }

    @GodotMethod
    public void OnVibrateDeviceShortPressed() {
        Input.singleton().vibrateHandheld(200);
    }

    @GodotMethod
    public void OnVibrateDeviceLongPressed() {
        Input.singleton().vibrateHandheld(1000);
    }

    @GodotMethod
    public void OnAddGlobalMenuItemsPressed() {
        DisplayServer displayServer = DisplayServer.singleton();
        if (!displayServer.hasFeature(DisplayServer.Feature.FEATURE_GLOBAL_MENU)) {
            OS.singleton().alert("Global menus are not supported by the current display server ("
                    + displayServer.getName() + ").");
            return;
        }

        Callable callback = new Callable(this, "GlobalMenuClicked");
        displayServer.globalMenuAddSubmenuItem("_main", "Hello", "_main/Hello");
        displayServer.globalMenuAddItem("_main/Hello", "World", callback, null, null, 0);
        displayServer.globalMenuAddSeparator("_main/Hello");
        displayServer.globalMenuAddItem("_main/Hello", "World2", callback);

        displayServer.globalMenuAddSubmenuItem("_dock", "Hello", "_dock/Hello");
        displayServer.globalMenuAddItem("_dock/Hello", "World", callback);
        displayServer.globalMenuAddSeparator("_dock/Hello");
        displayServer.globalMenuAddItem("_dock/Hello", "World2", callback);
    }

    @GodotMethod
    public void GlobalMenuClicked(String tag) {
        System.out.println("Clicked menu item: " + tag);
    }

    @GodotMethod
    public void OnRemoveGlobalMenuItemPressed() {
        DisplayServer displayServer = DisplayServer.singleton();
        if (!displayServer.hasFeature(DisplayServer.Feature.FEATURE_GLOBAL_MENU)) {
            OS.singleton().alert("Global menus are not supported by the current display server ("
                    + displayServer.getName() + ").");
            return;
        }
        displayServer.globalMenuRemoveItem("_main/Hello", 2);
        displayServer.globalMenuRemoveItem("_main/Hello", 1);
        displayServer.globalMenuRemoveItem("_main/Hello", 0);
        displayServer.globalMenuRemoveItem("_main", 0);
        displayServer.globalMenuRemoveItem("_dock/Hello", 2);
        displayServer.globalMenuRemoveItem("_dock/Hello", 1);
        displayServer.globalMenuRemoveItem("_dock/Hello", 0);
        displayServer.globalMenuRemoveItem("_dock", 0);
    }

    @GodotMethod
    public void OnGetClipboardPressed() {
        DisplayServer displayServer = DisplayServer.singleton();
        if (!displayServer.hasFeature(DisplayServer.Feature.FEATURE_CLIPBOARD)) {
            OS.singleton().alert("Clipboard I/O is not supported by the current display server ("
                    + displayServer.getName() + ").");
            return;
        }
        OS.singleton().alert("Clipboard contents:\n\n" + displayServer.clipboardGet());
    }

    @GodotMethod
    public void OnSetClipboardPressed() {
        DisplayServer displayServer = DisplayServer.singleton();
        if (!displayServer.hasFeature(DisplayServer.Feature.FEATURE_CLIPBOARD)) {
            OS.singleton().alert("Clipboard I/O is not supported by the current display server ("
                    + displayServer.getName() + ").");
            return;
        }
        displayServer.clipboardSet("Modified clipboard contents. Unicode characters for testing: é € × Ù ¨");
    }

    @GodotMethod
    public void OnDisplayAlertPressed() {
        OS.singleton().alert("Hello from Godot! Close this dialog to resume the main window.");
    }

    @GodotMethod
    public void OnKillCurrentProcessPressed() {
        OS.singleton().kill(OS.singleton().getProcessId());
    }
}
