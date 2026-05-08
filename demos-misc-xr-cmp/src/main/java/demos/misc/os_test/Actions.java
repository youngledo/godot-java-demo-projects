package demos.misc.os_test;

import org.godot.annotation.GodotClass;
import org.godot.annotation.GodotMethod;
import org.godot.node.Node;
import org.godot.node.SceneTree;

@GodotClass(name = "OSActions", parent = "Node")
public class Actions extends Node {

    private boolean initialized = false;

    @Override
    public void _ready() {
        if (initialized) return;
        initialized = true;

        org.godot.singleton.OS os = org.godot.singleton.OS.singleton();
        if ((boolean) os.call("has_feature", "web")) {
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
                org.godot.node.Node btn = getNode(path);
                if (btn != null) {
                    btn.setProperty("disabled", true);
                    String text = (String) btn.getProperty("text");
                    btn.setProperty("text", text + "\n(not supported on Web)");
                }
            }
        }
    }

    @GodotMethod
    public void OnOpenShellWebPressed() {
        org.godot.singleton.OS.singleton().shellOpen("https://example.com");
    }

    @GodotMethod
    public void OnOpenShellFolderPressed() {
        org.godot.singleton.OS os = org.godot.singleton.OS.singleton();
        String path = os.getEnvironment("HOME");
        if (path == null || path.isEmpty() ) {
            path = os.getEnvironment("USERPROFILE");
        }
        String osName = (String) os.getName();
        if ("macOS".equals(osName)) {
            path = "file://" + path;
        }
        os.call("shell_show_in_file_manager", path);
    }

    @GodotMethod
    public void OnChangeWindowTitlePressed() {
        org.godot.singleton.DisplayServer.singleton().windowSetTitle("Modified window title. Unicode characters for testing: é € × Ù ¨");
    }

    @GodotMethod
    public void OnChangeWindowIconPressed() {
        org.godot.singleton.DisplayServer ds = org.godot.singleton.DisplayServer.singleton();
        if (!(boolean) ds.call("has_feature", 3)) { // FEATURE_ICON
            org.godot.singleton.OS.singleton().alert("Changing the window icon is not supported by the current display server (" +
                ds.getName() + ").");
            return;
        }
        org.godot.Godot image = (org.godot.Godot) call("Image.create", 128, 128, false, 3); // FORMAT_RGB8
        if (image != null) {
            image.call("fill", new org.godot.math.Color(1, 0.6, 0.3));
            ds.call("set_icon", image);
        }
    }

    @GodotMethod
    public void OnMoveWindowToForegroundPressed() {
        org.godot.singleton.DisplayServer ds = org.godot.singleton.DisplayServer.singleton();
        ds.call("window_set_title", "Will move window to foreground in 5 seconds, try unfocusing the window...");
        // Use a timer via call_deferred
        org.godot.node.SceneTree tree = getTree();
        if (tree != null) {
            org.godot.Godot timer = (org.godot.Godot) tree.createTimer(5.0);
            if (timer != null) {
                timer.connect("timeout", new org.godot.core.Callable(this, "_move_to_foreground"), 0);
            }
        }
    }

    @GodotMethod
    public void MoveToForeground() {
        org.godot.singleton.DisplayServer.singleton().call("window_move_to_foreground");
        org.godot.singleton.DisplayServer.singleton().call("window_set_title", call("ProjectSettings.get_setting", "application/config/name"));
    }

    @GodotMethod
    public void OnRequestAttentionPressed() {
        org.godot.singleton.DisplayServer ds = org.godot.singleton.DisplayServer.singleton();
        ds.call("window_set_title", "Will request attention in 5 seconds, try unfocusing the window...");
        org.godot.node.SceneTree tree = getTree();
        if (tree != null) {
            org.godot.Godot timer = (org.godot.Godot) tree.createTimer(5.0);
            if (timer != null) {
                timer.connect("timeout", new org.godot.core.Callable(this, "_request_attention"), 0);
            }
        }
    }

    @GodotMethod
    public void RequestAttention() {
        org.godot.singleton.DisplayServer.singleton().call("window_request_attention");
        org.godot.singleton.DisplayServer.singleton().call("window_set_title", call("ProjectSettings.get_setting", "application/config/name"));
    }

    @GodotMethod
    public void OnVibrateDeviceShortPressed() {
        org.godot.singleton.Input.singleton().vibrateHandheld(200);
    }

    @GodotMethod
    public void OnVibrateDeviceLongPressed() {
        org.godot.singleton.Input.singleton().vibrateHandheld(1000);
    }

    @GodotMethod
    public void OnAddGlobalMenuItemsPressed() {
        org.godot.singleton.DisplayServer ds = org.godot.singleton.DisplayServer.singleton();
        if (!(boolean) ds.call("has_feature", 7)) { // FEATURE_GLOBAL_MENU
            org.godot.singleton.OS.singleton().alert("Global menus are not supported by the current display server (" +
                ds.getName() + ").");
            return;
        }
        ds.call("global_menu_add_submenu_item", "_main", "Hello", "_main/Hello");
        ds.call("global_menu_add_item", "_main/Hello", "World",
            new org.godot.core.Callable(this, "_global_menu_clicked"), null, null, 0);
        ds.call("global_menu_add_separator", "_main/Hello");
        ds.call("global_menu_add_item", "_main/Hello", "World2",
            new org.godot.core.Callable(this, "_global_menu_clicked"));

        ds.call("global_menu_add_submenu_item", "_dock", "Hello", "_dock/Hello");
        ds.call("global_menu_add_item", "_dock/Hello", "World",
            new org.godot.core.Callable(this, "_global_menu_clicked"));
        ds.call("global_menu_add_separator", "_dock/Hello");
        ds.call("global_menu_add_item", "_dock/Hello", "World2",
            new org.godot.core.Callable(this, "_global_menu_clicked"));
    }

    @GodotMethod
    public void GlobalMenuClicked(String tag) {
        System.out.println("Clicked menu item: " + tag);
    }

    @GodotMethod
    public void OnRemoveGlobalMenuItemPressed() {
        org.godot.singleton.DisplayServer ds = org.godot.singleton.DisplayServer.singleton();
        if (!(boolean) ds.call("has_feature", 7)) {
            org.godot.singleton.OS.singleton().alert("Global menus are not supported by the current display server (" +
                ds.getName() + ").");
            return;
        }
        ds.call("global_menu_remove_item", "_main/Hello", 2);
        ds.call("global_menu_remove_item", "_main/Hello", 1);
        ds.call("global_menu_remove_item", "_main/Hello", 0);
        ds.call("global_menu_remove_item", "_main", 0);
        ds.call("global_menu_remove_item", "_dock/Hello", 2);
        ds.call("global_menu_remove_item", "_dock/Hello", 1);
        ds.call("global_menu_remove_item", "_dock/Hello", 0);
        ds.call("global_menu_remove_item", "_dock", 0);
    }

    @GodotMethod
    public void OnGetClipboardPressed() {
        org.godot.singleton.DisplayServer ds = org.godot.singleton.DisplayServer.singleton();
        if (!(boolean) ds.call("has_feature", 1)) { // FEATURE_CLIPBOARD
            org.godot.singleton.OS.singleton().alert("Clipboard I/O is not supported by the current display server (" +
                ds.getName() + ").");
            return;
        }
        org.godot.singleton.OS.singleton().alert("Clipboard contents:\n\n" + ds.call("clipboard_get"));
    }

    @GodotMethod
    public void OnSetClipboardPressed() {
        org.godot.singleton.DisplayServer ds = org.godot.singleton.DisplayServer.singleton();
        if (!(boolean) ds.call("has_feature", 1)) {
            org.godot.singleton.OS.singleton().alert("Clipboard I/O is not supported by the current display server (" +
                ds.getName() + ").");
            return;
        }
        ds.call("clipboard_set", "Modified clipboard contents. Unicode characters for testing: é € × Ù ¨");
    }

    @GodotMethod
    public void OnDisplayAlertPressed() {
        org.godot.singleton.OS.singleton().alert("Hello from Godot! Close this dialog to resume the main window.");
    }

    @GodotMethod
    public void OnKillCurrentProcessPressed() {
        org.godot.singleton.OS.singleton().kill(org.godot.singleton.OS.singleton().getProcessId());
    }
}
