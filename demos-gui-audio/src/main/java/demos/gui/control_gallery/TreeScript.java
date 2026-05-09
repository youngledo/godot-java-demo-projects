package demos.gui.control_gallery;

import org.godot.annotation.GodotClass;
import org.godot.node.Font;
import org.godot.node.StyleBox;
import org.godot.node.Tree;
import org.godot.node.TreeItem;

@GodotClass(name = "TreeScript", parent = "Tree")
public class TreeScript extends Tree {

    private boolean initialized = false;

    @Override
    public void _ready() {
        if (initialized) return;
        initialized = true;

        Font defaultFont = getThemeDefaultFont();
        if (defaultFont != null) {
            addThemeFontOverride("title_button_font", defaultFont);
        }

        StyleBox defaultStylebox = getThemeStylebox("title_button");
        if (defaultStylebox == null) {
            StyleBox panelStylebox = getThemeStylebox("panel", "Tree");
            if (panelStylebox != null) {
                addThemeStyleboxOverride("title_button", panelStylebox);
            }
        }

        TreeItem root = createItem();
        root.setText(0, "Tree - Root");
        TreeItem child1 = createItem(root);
        child1.setText(0, "Tree - Child 1");
        TreeItem child2 = createItem(root);
        child2.setText(0, "Tree - Child 2");
        TreeItem subchild1 = createItem(child1);
        subchild1.setText(0, "Tree - Subchild 1");
    }
}
