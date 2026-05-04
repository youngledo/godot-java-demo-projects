package demos.misc.multiple_windows;

import org.godot.annotation.GodotClass;
import org.godot.annotation.GodotMethod;
import org.godot.math.Vector2;
import org.godot.math.Vector2i;
import org.godot.node.Control;
import org.godot.math.Rect2;

@GodotClass(name = "MWMainScene", parent = "Control")
public class MainScene extends Control {

    private org.godot.Godot window;
    private org.godot.Godot draggableWindow;
    private org.godot.Godot fileDialog;
    private org.godot.Godot fileDialogOutput;
    private org.godot.Godot acceptDialog;
    private org.godot.Godot acceptDialogOutput;
    private org.godot.Godot confirmationDialog;
    private org.godot.Godot confirmationDialogOutput;
    private org.godot.Godot popup;
    private org.godot.Godot popupMenu;
    private org.godot.Godot popupMenuOutput;
    private org.godot.Godot popupPanel;
    private org.godot.Godot statusIndicator;
    private boolean initialized = false;

    @Override
    public void _ready() {
        if (initialized) return;
        initialized = true;

        window = (org.godot.Godot) call("get_node", "Window");
        draggableWindow = (org.godot.Godot) call("get_node", "DraggableWindow");
        fileDialog = (org.godot.Godot) call("get_node", "FileDialog");
        fileDialogOutput = (org.godot.Godot) call("get_node", "HBoxContainer/VBoxContainer2/FileDialogOutput");
        acceptDialog = (org.godot.Godot) call("get_node", "AcceptDialog");
        acceptDialogOutput = (org.godot.Godot) call("get_node", "HBoxContainer/VBoxContainer2/AcceptOutput");
        confirmationDialog = (org.godot.Godot) call("get_node", "ConfirmationDialog");
        confirmationDialogOutput = (org.godot.Godot) call("get_node", "HBoxContainer/VBoxContainer2/ConfirmationOutput");
        popup = (org.godot.Godot) call("get_node", "Popup");
        popupMenu = (org.godot.Godot) call("get_node", "PopupMenu");
        popupMenuOutput = (org.godot.Godot) call("get_node", "HBoxContainer/VBoxContainer3/PopupMenuOutput");
        popupPanel = (org.godot.Godot) call("get_node", "PopupPanel");
        statusIndicator = (org.godot.Godot) call("get_node", "StatusIndicator");
    }

    @GodotMethod
    public void _on_embed_subwindows_toggled(boolean toggledOn) {
        java.util.List<org.godot.Godot> hiddenWindows = new java.util.ArrayList<>();
        for (Object child : call("get_children") instanceof Object[] ? (Object[]) call("get_children") : new Object[0]) {
            if (child instanceof org.godot.Godot) {
                org.godot.Godot c = (org.godot.Godot) child;
                String className = (String) c.call("get_class");
                if ("Window".equals(className) && (boolean) c.call("is_visible")) {
                    c.call("hide");
                    hiddenWindows.add(c);
                }
            }
        }
        embedSubwindows(toggledOn);
        for (org.godot.Godot w : hiddenWindows) {
            w.call("show");
        }
    }

    private void embedSubwindows(boolean state) {
        org.godot.Godot viewport = (org.godot.Godot) call("get_viewport");
        if (viewport != null) {
            viewport.setProperty("gui_embed_subwindows", state);
        }
    }

    @GodotMethod
    public void _on_window_button_pressed() {
        if (window != null) {
            window.call("show");
            window.call("grab_focus");
        }
    }

    @GodotMethod
    public void _on_transient_window_toggled(boolean toggledOn) {
        if (window != null) window.setProperty("transient", toggledOn);
    }

    @GodotMethod
    public void _on_exclusive_window_toggled(boolean toggledOn) {
        if (window != null) window.setProperty("exclusive", toggledOn);
    }

    @GodotMethod
    public void _on_unresizable_window_toggled(boolean toggledOn) {
        if (window != null) window.setProperty("unresizable", toggledOn);
    }

    @GodotMethod
    public void _on_borderless_window_toggled(boolean toggledOn) {
        if (window != null) window.setProperty("borderless", toggledOn);
    }

    @GodotMethod
    public void _on_always_on_top_window_toggled(boolean toggledOn) {
        if (window != null) window.setProperty("always_on_top", toggledOn);
    }

    @GodotMethod
    public void _on_transparent_window_toggled(boolean toggledOn) {
        if (window != null) window.setProperty("transparent", toggledOn);
    }

    @GodotMethod
    public void _on_window_title_edit_text_changed(String newText) {
        if (window != null) window.setProperty("title", newText);
    }

    @GodotMethod
    public void _on_draggable_window_button_pressed() {
        if (draggableWindow != null) {
            draggableWindow.call("show");
            draggableWindow.call("grab_focus");
        }
    }

    @GodotMethod
    public void _on_draggable_window_close_pressed() {
        if (draggableWindow != null) draggableWindow.call("hide");
    }

    @GodotMethod
    public void _on_bg_draggable_window_toggled(boolean toggledOn) {
        if (draggableWindow != null) {
            org.godot.Godot bg = (org.godot.Godot) draggableWindow.call("get_node", "BG");
            if (bg != null) bg.setProperty("visible", toggledOn);
        }
    }

    @GodotMethod
    public void _on_passthrough_polygon_item_selected(long index) {
        if (draggableWindow == null) return;
        if (index == 0) {
            draggableWindow.setProperty("mouse_passthrough_polygon", new Object[0]);
        } else if (index == 1) {
            org.godot.Godot gen = (org.godot.Godot) draggableWindow.call("get_node", "PassthroughGenerator");
            if (gen != null) gen.call("generate_polygon");
        } else if (index == 2) {
            draggableWindow.setProperty("mouse_passthrough_polygon", new org.godot.math.Vector2[]{
                new org.godot.math.Vector2(16, 0), new org.godot.math.Vector2(16, 128),
                new org.godot.math.Vector2(116, 128), new org.godot.math.Vector2(116, 0)
            });
        }
    }

    @GodotMethod
    public void _on_file_dialog_button_pressed() {
        if (fileDialog != null) fileDialog.call("show");
    }

    @GodotMethod
    public void _on_file_dialog_dir_selected(String dir) {
        if (fileDialogOutput != null) fileDialogOutput.setProperty("text", "Directory Path: " + dir);
    }

    @GodotMethod
    public void _on_file_dialog_file_selected(String path) {
        if (fileDialogOutput != null) fileDialogOutput.setProperty("text", "File Path: " + path);
    }

    @GodotMethod
    public void _on_file_dialog_files_selected(Object paths) {
        if (fileDialogOutput != null) fileDialogOutput.setProperty("text", "Chosen Paths: " + String.valueOf(paths));
    }

    @GodotMethod
    public void _on_file_dialog_options_item_selected(long index) {
        if (fileDialog == null) return;
        int[] modes = {0, 1, 2, 3, 4}; // FILE_MODE_OPEN_FILE through FILE_MODE_SAVE_FILE
        if (index >= 0 && index < modes.length) {
            fileDialog.setProperty("file_mode", modes[(int) index]);
        }
    }

    @GodotMethod
    public void _on_native_dialog_toggled(boolean toggledOn) {
        if (fileDialog != null) fileDialog.setProperty("use_native_dialog", toggledOn);
    }

    @GodotMethod
    public void _on_accept_button_text_submitted(String newText) {
        if (!newText.isEmpty() && acceptDialog != null) {
            acceptDialog.call("add_button", newText, false, newText);
        }
    }

    @GodotMethod
    public void _on_accept_dialog_canceled() {
        if (acceptDialogOutput != null) acceptDialogOutput.setProperty("text", "Cancelled");
    }

    @GodotMethod
    public void _on_accept_dialog_confirmed() {
        if (acceptDialogOutput != null) acceptDialogOutput.setProperty("text", "Accepted");
    }

    @GodotMethod
    public void _on_accept_dialog_custom_action(String action) {
        if (acceptDialogOutput != null) acceptDialogOutput.setProperty("text", "Custom Action: " + action);
        if (acceptDialog != null) acceptDialog.call("hide");
    }

    @GodotMethod
    public void _on_accept_button_pressed() {
        if (acceptDialog != null) acceptDialog.call("show");
    }

    @GodotMethod
    public void _on_confirmation_button_pressed() {
        if (confirmationDialog != null) confirmationDialog.call("show");
    }

    @GodotMethod
    public void _on_confirmation_dialog_canceled() {
        if (confirmationDialogOutput != null) confirmationDialogOutput.setProperty("text", "Cancelled");
    }

    @GodotMethod
    public void _on_confirmation_dialog_confirmed() {
        if (confirmationDialogOutput != null) confirmationDialogOutput.setProperty("text", "Accepted");
    }

    private void showPopup(org.godot.Godot p) {
        org.godot.math.Vector2i mousePosition;
        org.godot.Godot viewport = (org.godot.Godot) call("get_viewport");
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
    public void _on_popup_button_pressed() {
        if (popup != null) showPopup(popup);
    }

    @GodotMethod
    public void _on_popup_menu_button_pressed() {
        if (popupMenu != null) showPopup(popupMenu);
    }

    @GodotMethod
    public void _on_popup_panel_button_pressed() {
        if (popupPanel != null) showPopup(popupPanel);
    }

    @GodotMethod
    public void _on_popup_menu_option_pressed(String option) {
        if (popupMenuOutput != null) popupMenuOutput.setProperty("text", option + " was pressed.");
    }

    @GodotMethod
    public void _on_status_indicator_visible_toggled(boolean toggledOn) {
        if (statusIndicator != null) statusIndicator.setProperty("visible", toggledOn);
    }
}
