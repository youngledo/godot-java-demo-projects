package demos.misc.multiple_windows;

import org.godot.annotation.GodotClass;
import org.godot.annotation.GodotMethod;
import org.godot.math.Vector2;
import org.godot.math.Vector2i;
import org.godot.node.Control;
import org.godot.math.Rect2;
import org.godot.node.Node;
import org.godot.node.Viewport;

@GodotClass(name = "MWMainScene", parent = "Control")
public class MainScene extends Control {

    private org.godot.node.Control window;
    private org.godot.node.Control draggableWindow;
    private org.godot.node.CanvasItem fileDialog;
    private org.godot.node.CanvasItem fileDialogOutput;
    private org.godot.node.Node acceptDialog;
    private org.godot.node.Node acceptDialogOutput;
    private org.godot.node.CanvasItem confirmationDialog;
    private org.godot.node.CanvasItem confirmationDialogOutput;
    private org.godot.node.Node popup;
    private org.godot.node.Node popupMenu;
    private org.godot.node.Node popupMenuOutput;
    private org.godot.node.Node popupPanel;
    private org.godot.node.Node statusIndicator;
    private boolean initialized = false;

    @Override
    public void _ready() {
        if (initialized) return;
        initialized = true;

        window = (org.godot.node.Control) getNode("Window");
        draggableWindow = (org.godot.node.Control) getNode("DraggableWindow");
        fileDialog = (org.godot.node.CanvasItem) getNode("FileDialog");
        fileDialogOutput = (org.godot.node.CanvasItem) getNode("HBoxContainer/VBoxContainer2/FileDialogOutput");
        acceptDialog = getNode("AcceptDialog");
        acceptDialogOutput = getNode("HBoxContainer/VBoxContainer2/AcceptOutput");
        confirmationDialog = (org.godot.node.CanvasItem) getNode("ConfirmationDialog");
        confirmationDialogOutput = (org.godot.node.CanvasItem) getNode("HBoxContainer/VBoxContainer2/ConfirmationOutput");
        popup = getNode("Popup");
        popupMenu = getNode("PopupMenu");
        popupMenuOutput = getNode("HBoxContainer/VBoxContainer3/PopupMenuOutput");
        popupPanel = getNode("PopupPanel");
        statusIndicator = getNode("StatusIndicator");
    }

    @GodotMethod
    public void OnEmbedSubwindowsToggled(boolean toggledOn) {
        java.util.List<org.godot.Godot> hiddenWindows = new java.util.ArrayList<>();
        for (Object child : getChildren() instanceof Object[] ? (Object[]) getChildren() : new Object[0]) {
            if (child instanceof org.godot.Godot) {
                org.godot.node.CanvasItem c = (org.godot.node.CanvasItem) child;
                String className = (String) c.call("get_class");
                if ("Window".equals(className) && (boolean) c.isVisible()) {
                    c.hide();
                    hiddenWindows.add(c);
                }
            }
        }
        embedSubwindows(toggledOn);
        for (org.godot.Godot w : hiddenWindows) {
            ((org.godot.node.CanvasItem) w).show();
        }
    }

    private void embedSubwindows(boolean state) {
        org.godot.node.Viewport viewport = getViewport();
        if (viewport != null) {
            viewport.setProperty("gui_embed_subwindows", state);
        }
    }

    @GodotMethod
    public void OnWindowButtonPressed() {
        if (window != null) {
            window.show();
            window.grabFocus();
        }
    }

    @GodotMethod
    public void OnTransientWindowToggled(boolean toggledOn) {
        if (window != null) window.setProperty("transient", toggledOn);
    }

    @GodotMethod
    public void OnExclusiveWindowToggled(boolean toggledOn) {
        if (window != null) window.setProperty("exclusive", toggledOn);
    }

    @GodotMethod
    public void OnUnresizableWindowToggled(boolean toggledOn) {
        if (window != null) window.setProperty("unresizable", toggledOn);
    }

    @GodotMethod
    public void OnBorderlessWindowToggled(boolean toggledOn) {
        if (window != null) window.setProperty("borderless", toggledOn);
    }

    @GodotMethod
    public void OnAlwaysOnTopWindowToggled(boolean toggledOn) {
        if (window != null) window.setProperty("always_on_top", toggledOn);
    }

    @GodotMethod
    public void OnTransparentWindowToggled(boolean toggledOn) {
        if (window != null) window.setProperty("transparent", toggledOn);
    }

    @GodotMethod
    public void OnWindowTitleEditTextChanged(String newText) {
        if (window != null) window.setProperty("title", newText);
    }

    @GodotMethod
    public void OnDraggableWindowButtonPressed() {
        if (draggableWindow != null) {
            draggableWindow.show();
            draggableWindow.grabFocus();
        }
    }

    @GodotMethod
    public void OnDraggableWindowClosePressed() {
        if (draggableWindow != null) draggableWindow.hide();
    }

    @GodotMethod
    public void OnBgDraggableWindowToggled(boolean toggledOn) {
        if (draggableWindow != null) {
            org.godot.Godot bg = (org.godot.Godot) draggableWindow.getNode("BG");
            if (bg != null) bg.setProperty("visible", toggledOn);
        }
    }

    @GodotMethod
    public void OnPassthroughPolygonItemSelected(long index) {
        if (draggableWindow == null) return;
        if (index == 0) {
            draggableWindow.setProperty("mouse_passthrough_polygon", new Object[0]);
        } else if (index == 1) {
            org.godot.Godot gen = (org.godot.Godot) draggableWindow.getNode("PassthroughGenerator");
            if (gen != null) gen.call("generate_polygon");
        } else if (index == 2) {
            draggableWindow.setProperty("mouse_passthrough_polygon", new org.godot.math.Vector2[] {
                new org.godot.math.Vector2(16, 0), new org.godot.math.Vector2(16, 128),
                new org.godot.math.Vector2(116, 128), new org.godot.math.Vector2(116, 0)
            });
        }
    }

    @GodotMethod
    public void OnFileDialogButtonPressed() {
        if (fileDialog != null) fileDialog.show();
    }

    @GodotMethod
    public void OnFileDialogDirSelected(String dir) {
        if (fileDialogOutput != null) fileDialogOutput.setProperty("text", "Directory Path: " + dir);
    }

    @GodotMethod
    public void OnFileDialogFileSelected(String path) {
        if (fileDialogOutput != null) fileDialogOutput.setProperty("text", "File Path: " + path);
    }

    @GodotMethod
    public void OnFileDialogFilesSelected(Object paths) {
        if (fileDialogOutput != null) fileDialogOutput.setProperty("text", "Chosen Paths: " + String.valueOf(paths));
    }

    @GodotMethod
    public void OnFileDialogOptionsItemSelected(long index) {
        if (fileDialog == null) return;
        int[] modes = {0, 1, 2, 3, 4}; // FILE_MODE_OPEN_FILE through FILE_MODE_SAVE_FILE
        if (index >= 0 && index < modes.length) {
            fileDialog.setProperty("file_mode", modes[(int) index]);
        }
    }

    @GodotMethod
    public void OnNativeDialogToggled(boolean toggledOn) {
        if (fileDialog != null) fileDialog.setProperty("use_native_dialog", toggledOn);
    }

    @GodotMethod
    public void OnAcceptButtonTextSubmitted(String newText) {
        if (!newText.isEmpty() && acceptDialog != null) {
            acceptDialog.call("add_button", newText, false, newText);
        }
    }

    @GodotMethod
    public void OnAcceptDialogCanceled() {
        if (acceptDialogOutput != null) acceptDialogOutput.setProperty("text", "Cancelled");
    }

    @GodotMethod
    public void OnAcceptDialogConfirmed() {
        if (acceptDialogOutput != null) acceptDialogOutput.setProperty("text", "Accepted");
    }

    @GodotMethod
    public void OnAcceptDialogCustomAction(String action) {
        if (acceptDialogOutput != null) acceptDialogOutput.setProperty("text", "Custom Action: " + action);
        if (acceptDialog != null) acceptDialog.call("hide");
    }

    @GodotMethod
    public void OnAcceptButtonPressed() {
        if (acceptDialog != null) acceptDialog.call("show");
    }

    @GodotMethod
    public void OnConfirmationButtonPressed() {
        if (confirmationDialog != null) confirmationDialog.show();
    }

    @GodotMethod
    public void OnConfirmationDialogCanceled() {
        if (confirmationDialogOutput != null) confirmationDialogOutput.setProperty("text", "Cancelled");
    }

    @GodotMethod
    public void OnConfirmationDialogConfirmed() {
        if (confirmationDialogOutput != null) confirmationDialogOutput.setProperty("text", "Accepted");
    }

    private void showPopup(org.godot.Godot p) {
        org.godot.math.Vector2i mousePosition;
        org.godot.node.Viewport viewport = getViewport();
        boolean embed = (boolean) viewport.call("gui_embed_subwindows");
        if (embed) {
            mousePosition = (org.godot.math.Vector2i) call("get_global_mouse_position");
        } else {
            Object mp = org.godot.singleton.DisplayServer.singleton().call("mouse_get_position");
            if (mp instanceof org.godot.math.Vector2i) {
                mousePosition = (org.godot.math.Vector2i) mp;
            } else {
                mousePosition = new org.godot.math.Vector2i(0, 0);
            }
        }
        Object size = p.getProperty("size");
        org.godot.math.Vector2i sz = size instanceof org.godot.math.Vector2i ? (org.godot.math.Vector2i) size : new org.godot.math.Vector2i(100, 100);
        p.call("popup", new Object[]{new org.godot.math.Rect2(
            new org.godot.math.Vector2(mousePosition.getX(), mousePosition.getY()),
            new org.godot.math.Vector2(sz.getX(), sz.getY())
        )});
    }

    @GodotMethod
    public void OnPopupButtonPressed() {
        if (popup != null) showPopup(popup);
    }

    @GodotMethod
    public void OnPopupMenuButtonPressed() {
        if (popupMenu != null) showPopup(popupMenu);
    }

    @GodotMethod
    public void OnPopupPanelButtonPressed() {
        if (popupPanel != null) showPopup(popupPanel);
    }

    @GodotMethod
    public void OnPopupMenuOptionPressed(String option) {
        if (popupMenuOutput != null) popupMenuOutput.setProperty("text", option + " was pressed.");
    }

    @GodotMethod
    public void OnStatusIndicatorVisibleToggled(boolean toggledOn) {
        if (statusIndicator != null) statusIndicator.setProperty("visible", toggledOn);
    }
}
