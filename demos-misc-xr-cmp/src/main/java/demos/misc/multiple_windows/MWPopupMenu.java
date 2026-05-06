package demos.misc.multiple_windows;

import org.godot.annotation.GodotClass;
import org.godot.annotation.GodotMethod;
import org.godot.node.PopupMenu;
import org.godot.node.Node;

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

        org.godot.node.Node submenu = getNode("SubPopupMenu");
        if (submenu != null) {
            submenu.setProperty("transparent", true);
            submenu.call("add_item", "Submenu Item 1");
            submenu.call("add_item", "Submenu Item 2");
        }
    }

    @GodotMethod
    public void OnIndexPressed(long index) {
        boolean checkable = (boolean) call("is_item_checkable", index);
        if (checkable) {
            boolean checked = (boolean) call("is_item_checked", index);
            setItemChecked(index, !checked);
        }

        if (index == 2) setItemChecked(3, false);
        if (index == 3) setItemChecked(2, false);

        String text = (String) call("get_item_text", index);
        emitSignal("option_pressed", text);
    }
}
