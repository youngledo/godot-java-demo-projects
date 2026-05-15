package demos.misc.multiple_windows;

import java.util.ArrayList;
import java.util.List;
import org.godot.annotation.GodotClass;
import org.godot.annotation.GodotMethod;
import org.godot.math.Rect2i;
import org.godot.math.Vector2;
import org.godot.math.Vector2i;
import org.godot.node.AcceptDialog;
import org.godot.node.CanvasItem;
import org.godot.node.ConfirmationDialog;
import org.godot.node.Control;
import org.godot.node.FileDialog;
import org.godot.node.Label;
import org.godot.node.Node;
import org.godot.node.Popup;
import org.godot.node.PopupMenu;
import org.godot.node.PopupPanel;
import org.godot.node.Viewport;
import org.godot.node.Window;
import org.godot.singleton.DisplayServer;

@GodotClass(name = "MWMainScene", parent = "Control")
public class MainScene extends Control {

    private Window window;
    private Window draggableWindow;
    private FileDialog fileDialog;
    private Label fileDialogOutput;
    private AcceptDialog acceptDialog;
    private Label acceptDialogOutput;
    private ConfirmationDialog confirmationDialog;
    private Label confirmationDialogOutput;
    private Popup popup;
    private PopupMenu popupMenu;
    private Label popupMenuOutput;
    private PopupPanel popupPanel;
    private CanvasItem statusIndicator;
    private boolean initialized = false;

    @Override
    public void _ready() {
        if (initialized) return;
        initialized = true;

        window = getNodeAs("Window", Window.class);
        draggableWindow = getNodeAs("DraggableWindow", Window.class);
        fileDialog = getNodeAs("FileDialog", FileDialog.class);
        fileDialogOutput = getNodeAs("HBoxContainer/VBoxContainer2/FileDialogOutput", Label.class);
        acceptDialog = getNodeAs("AcceptDialog", AcceptDialog.class);
        acceptDialogOutput = getNodeAs("HBoxContainer/VBoxContainer2/AcceptOutput", Label.class);
        confirmationDialog = getNodeAs("ConfirmationDialog", ConfirmationDialog.class);
        confirmationDialogOutput = getNodeAs("HBoxContainer/VBoxContainer2/ConfirmationOutput", Label.class);
        popup = getNodeAs("Popup", Popup.class);
        popupMenu = getNodeAs("PopupMenu", PopupMenu.class);
        popupMenuOutput = getNodeAs("HBoxContainer/VBoxContainer3/PopupMenuOutput", Label.class);
        popupPanel = getNodeAs("PopupPanel", PopupPanel.class);
        statusIndicator = getNodeAs("StatusIndicator", CanvasItem.class);
    }

    @GodotMethod
    public void OnEmbedSubwindowsToggled(boolean toggledOn) {
        List<Window> hiddenWindows = new ArrayList<>();
        var children = getChildren();
        for (int i = 0; i < children.size(); i++) {
            Node child = children.get(i);
            if (child instanceof Window childWindow && childWindow.isVisible()) {
                childWindow.hide();
                hiddenWindows.add(childWindow);
            }
        }
        embedSubwindows(toggledOn);
        for (Window hiddenWindow : hiddenWindows) {
            hiddenWindow.show();
        }
    }

    private void embedSubwindows(boolean state) {
        Viewport viewport = getViewport();
        if (viewport != null) {
            viewport.setGuiEmbedSubwindows(state);
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
        if (window != null) window.setTransient_(toggledOn);
    }

    @GodotMethod
    public void OnExclusiveWindowToggled(boolean toggledOn) {
        if (window != null) window.setExclusive(toggledOn);
    }

    @GodotMethod
    public void OnUnresizableWindowToggled(boolean toggledOn) {
        if (window != null) window.setUnresizable(toggledOn);
    }

    @GodotMethod
    public void OnBorderlessWindowToggled(boolean toggledOn) {
        if (window != null) window.setBorderless(toggledOn);
    }

    @GodotMethod
    public void OnAlwaysOnTopWindowToggled(boolean toggledOn) {
        if (window != null) window.setAlwaysOnTop(toggledOn);
    }

    @GodotMethod
    public void OnTransparentWindowToggled(boolean toggledOn) {
        if (window != null) window.setTransparent(toggledOn);
    }

    @GodotMethod
    public void OnWindowTitleEditTextChanged(String newText) {
        if (window != null) window.setTitle(newText);
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
            CanvasItem bg = draggableWindow.getNodeAs("BG", CanvasItem.class);
            if (bg != null) bg.setVisible(toggledOn);
        }
    }

    @GodotMethod
    public void OnPassthroughPolygonItemSelected(long index) {
        if (draggableWindow == null) return;
        if (index == 0) {
            draggableWindow.setMousePassthroughPolygon(new double[0][0]);
        } else if (index == 1) {
            SpritePolygonPassthrough gen = draggableWindow.getNodeAs("PassthroughGenerator", SpritePolygonPassthrough.class);
            if (gen != null) gen.generatePolygon();
        } else if (index == 2) {
            draggableWindow.setMousePassthroughPolygon(new double[][] {
                    {16, 0}, {16, 128}, {116, 128}, {116, 0}
            });
        }
    }

    @GodotMethod
    public void OnFileDialogButtonPressed() {
        if (fileDialog != null) fileDialog.show();
    }

    @GodotMethod
    public void OnFileDialogDirSelected(String dir) {
        if (fileDialogOutput != null) fileDialogOutput.setText("Directory Path: " + dir);
    }

    @GodotMethod
    public void OnFileDialogFileSelected(String path) {
        if (fileDialogOutput != null) fileDialogOutput.setText("File Path: " + path);
    }

    @GodotMethod
    public void OnFileDialogFilesSelected(Object paths) {
        if (fileDialogOutput != null) fileDialogOutput.setText("Chosen Paths: " + String.valueOf(paths));
    }

    @GodotMethod
    public void OnFileDialogOptionsItemSelected(long index) {
        if (fileDialog == null) return;
        int[] modes = {0, 1, 2, 3, 4};
        if (index >= 0 && index < modes.length) {
            fileDialog.setFileMode(modes[(int) index]);
        }
    }

    @GodotMethod
    public void OnNativeDialogToggled(boolean toggledOn) {
        if (fileDialog != null) fileDialog.setUseNativeDialog(toggledOn);
    }

    @GodotMethod
    public void OnAcceptButtonTextSubmitted(String newText) {
        if (!newText.isEmpty() && acceptDialog != null) {
            acceptDialog.addButton(newText, false, newText);
        }
    }

    @GodotMethod
    public void OnAcceptDialogCanceled() {
        if (acceptDialogOutput != null) acceptDialogOutput.setText("Cancelled");
    }

    @GodotMethod
    public void OnAcceptDialogConfirmed() {
        if (acceptDialogOutput != null) acceptDialogOutput.setText("Accepted");
    }

    @GodotMethod
    public void OnAcceptDialogCustomAction(String action) {
        if (acceptDialogOutput != null) acceptDialogOutput.setText("Custom Action: " + action);
        if (acceptDialog != null) acceptDialog.hide();
    }

    @GodotMethod
    public void OnAcceptButtonPressed() {
        if (acceptDialog != null) acceptDialog.show();
    }

    @GodotMethod
    public void OnConfirmationButtonPressed() {
        if (confirmationDialog != null) confirmationDialog.show();
    }

    @GodotMethod
    public void OnConfirmationDialogCanceled() {
        if (confirmationDialogOutput != null) confirmationDialogOutput.setText("Cancelled");
    }

    @GodotMethod
    public void OnConfirmationDialogConfirmed() {
        if (confirmationDialogOutput != null) confirmationDialogOutput.setText("Accepted");
    }

    private void showPopup(Window popupWindow) {
        Vector2i mousePosition;
        Viewport viewport = getViewport();
        boolean embed = viewport != null && viewport.isGuiEmbedSubwindows();
        if (embed) {
            Vector2 globalMousePosition = getGlobalMousePosition();
            mousePosition = new Vector2i((int) globalMousePosition.x, (int) globalMousePosition.y);
        } else {
            mousePosition = DisplayServer.singleton().mouseGetPosition();
        }

        Vector2i size = popupWindow.getSize();
        popupWindow.popup(new Rect2i(mousePosition.x, mousePosition.y, size.x, size.y));
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
        if (popupMenuOutput != null) popupMenuOutput.setText(option + " was pressed.");
    }

    @GodotMethod
    public void OnStatusIndicatorVisibleToggled(boolean toggledOn) {
        if (statusIndicator != null) statusIndicator.setVisible(toggledOn);
    }
}
