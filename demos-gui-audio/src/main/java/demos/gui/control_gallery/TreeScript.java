package demos.gui.control_gallery;

import org.godot.annotation.GodotClass;
import org.godot.node.Tree;

@GodotClass(name = "TreeScript", parent = "Tree")
public class TreeScript extends Tree {

    private boolean initialized = false;

    @Override
    public void _ready() {
        if (initialized) return;
        initialized = true;

        // Fix: The default theme sets title_button_font to null, which triggers
        // ERR_FAIL_COND_V in Tree::_get_title_button_height(). Override it with
        // the default font so the theme cache has a valid reference.
        Object defaultFont = call("get_theme_default_font");
        if (defaultFont != null) {
            call("add_theme_font_override", "title_button_font", defaultFont);
        }

        // Also override the title_button StyleBox to prevent null check error
        Object defaultStylebox = call("get_theme_stylebox", "title_button");
        if (defaultStylebox == null) {
            Object panelStylebox = call("get_theme_stylebox", "panel", "Tree");
            if (panelStylebox != null) {
                call("add_theme_stylebox_override", "title_button", panelStylebox);
            }
        }

        Object root = call("create_item");
        call("set_text", 0, "Tree - Root");
        Object child1 = call("create_item", root);
        callOnItem(child1, "set_text", 0, "Tree - Child 1");
        Object child2 = call("create_item", root);
        callOnItem(child2, "set_text", 0, "Tree - Child 2");
        Object subchild1 = call("create_item", child1);
        callOnItem(subchild1, "set_text", 0, "Tree - Subchild 1");
    }

    private void callOnItem(Object item, String method, int column, String text) {
        if (item instanceof org.godot.Godot) {
            ((org.godot.Godot) item).call(method, column, text);
        }
    }
}
