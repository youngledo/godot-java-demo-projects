package demos.misc.multiple_windows;

import org.godot.annotation.GodotClass;
import org.godot.annotation.GodotMethod;
import org.godot.node.PopupMenu;

@GodotClass(name = "MWPopupMenu", parent = "PopupMenu")
public class MWPopupMenu extends PopupMenu {

    private boolean initialized = false;

    @Override
    public void _ready() {
        if (initialized) return;
        initialized = true;

        addItem("Normal Item");
        addMultistateItem("Multistate Item", 3, 0);
        addRadioCheckItem("Radio Check Item 1");
        addRadioCheckItem("Radio Check Item 2");
        addCheckItem("Check Item");
        addSeparator("Separator");
        addSubmenuItem("Submenu", "SubPopupMenu");

        PopupMenu submenu = getNodeAs("SubPopupMenu", PopupMenu.class);
        if (submenu != null) {
            submenu.setTransparent(true);
            submenu.addItem("Submenu Item 1");
            submenu.addItem("Submenu Item 2");
        }
    }

    @GodotMethod
    public void OnIndexPressed(long index) {
        boolean checkable = isItemCheckable(index);
        if (checkable) {
            boolean checked = isItemChecked(index);
            setItemChecked(index, !checked);
        }

        if (index == 2) setItemChecked(3, false);
        if (index == 3) setItemChecked(2, false);

        emitSignal("option_pressed", getItemText(index));
    }
}
