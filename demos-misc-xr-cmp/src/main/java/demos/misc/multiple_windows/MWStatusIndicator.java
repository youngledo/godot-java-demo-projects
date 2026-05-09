package demos.misc.multiple_windows;

import org.godot.annotation.GodotClass;
import org.godot.annotation.GodotMethod;
import org.godot.core.Callable;
import org.godot.math.Vector2i;
import org.godot.node.Node;
import org.godot.node.PopupMenu;
import org.godot.node.SceneTree;
import org.godot.node.StatusIndicator;
import org.godot.node.Window;

@GodotClass(name = "MWStatusIndicator", parent = "StatusIndicator")
public class MWStatusIndicator extends StatusIndicator {

    private PopupMenu popupMenu;
    private boolean initialized = false;

    @Override
    public void _ready() {
        if (initialized) return;
        initialized = true;

        Object menuPath = getProperty("menu");
        if (menuPath != null) {
            popupMenu = getNodeAs(menuPath.toString(), PopupMenu.class);
        }

        if (popupMenu != null) {
            popupMenu.setPreferNativeMenu(true);
            popupMenu.addItem("Quit");
            popupMenu.connect("index_pressed", new Callable(this, "OnPopupMenuIndexPressed"), 0);
        }

        connect("pressed", new Callable(this, "OnPressed"), 0);
    }

    @GodotMethod
    public void OnPressed(long mouseButton, Vector2i mousePosition) {
        if (mouseButton == 1) {
            Window window = getWindow();
            if (window != null) {
                if (window.getMode() == 2) {
                    window.setMode(0);
                }
                window.grabFocus();
            }
        }
    }

    @GodotMethod
    public void OnPopupMenuIndexPressed(long index) {
        if (index == 0) {
            SceneTree tree = getTree();
            if (tree != null) {
                Node root = tree.getRoot();
                if (root != null) root.propagateNotification(1006);
                tree.quit();
            }
        }
    }
}
