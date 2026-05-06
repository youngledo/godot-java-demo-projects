package demos.misc.multiple_windows;

import org.godot.annotation.GodotClass;
import org.godot.annotation.GodotMethod;
import org.godot.node.StatusIndicator;
import org.godot.node.Node;
import org.godot.node.SceneTree;

@GodotClass(name = "MWStatusIndicator", parent = "StatusIndicator")
public class MWStatusIndicator extends StatusIndicator {

    private org.godot.node.Node popupMenu;
    private boolean initialized = false;

    @Override
    public void _ready() {
        if (initialized) return;
        initialized = true;

        Object menuPath = getProperty("menu");
        if (menuPath != null) {
            popupMenu = getNode(menuPath.toString());
        }

        if (popupMenu != null) {
            popupMenu.setProperty("prefer_native_menu", true);
            popupMenu.call("add_item", "Quit");
            popupMenu.connect("index_pressed", new org.godot.core.Callable(this, "_on_popup_menu_index_pressed"), 0);
        }

        connect("pressed",
            new org.godot.core.Callable(this, "_on_pressed"), 0);
    }

    @GodotMethod
    public void OnPressed(long mouseButton, org.godot.math.Vector2i mousePosition) {
        if (mouseButton == 1) { // MOUSE_BUTTON_LEFT
            org.godot.Godot win = getWindow();
            if (win != null) {
                long mode = (long) win.getProperty("mode");
                if (mode == 2) { // MODE_MINIMIZED
                    win.setProperty("mode", 0); // MODE_WINDOWED
                }
                win.call("grab_focus");
            }
        }
    }

    @GodotMethod
    public void OnPopupMenuIndexPressed(long index) {
        if (index == 0) {
            org.godot.node.SceneTree tree = getTree();
            if (tree != null) {
                Object root = tree.call("root");
                if (root instanceof org.godot.Godot) {
                    ((org.godot.Godot) root).call("propagate_notification", 1006); // NOTIFICATION_WM_CLOSE_REQUEST
                }
                tree.quit();
            }
        }
    }
}
