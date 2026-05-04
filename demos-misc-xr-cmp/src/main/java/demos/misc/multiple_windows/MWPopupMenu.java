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

        call("add_item", "Normal Item");
        call("add_multistate_item", "Multistate Item", 3, 0);
        call("add_radio_check_item", "Radio Check Item 1");
        call("add_radio_check_item", "Radio Check Item 2");
        call("add_check_item", "Check Item");
        call("add_separator", "Separator");
        call("add_submenu_item", "Submenu", "SubPopupMenu");

        org.godot.Godot submenu = (org.godot.Godot) call("get_node", "SubPopupMenu");
        if (submenu != null) {
            submenu.setProperty("transparent", true);
            submenu.call("add_item", "Submenu Item 1");
            submenu.call("add_item", "Submenu Item 2");
        }
    }

    @GodotMethod
    public void _on_index_pressed(long index) {
        boolean checkable = (boolean) call("is_item_checkable", index);
        if (checkable) {
            boolean checked = (boolean) call("is_item_checked", index);
            call("set_item_checked", index, !checked);
        }

        if (index == 2) call("set_item_checked", 3, false);
        if (index == 3) call("set_item_checked", 2, false);

        String text = (String) call("get_item_text", index);
        call("emit_signal", "option_pressed", text);
    }
}
