package demos.gui.bidi_and_font_features;

import org.godot.Godot;
import org.godot.annotation.GodotClass;
import org.godot.annotation.GodotMethod;
import org.godot.node.Control;

@GodotClass(name = "Bidi", parent = "Control")
public class Bidi extends Control {

    private Godot variableFontVariation;
    private boolean initialized = false;

    @Override
    public void _ready() {
        if (initialized) return;
        initialized = true;

        org.godot.singleton.OS os = org.godot.singleton.OS.singleton();
        if ((boolean) os.call("has_feature", "web")) {
            Godot labelVarInfo = (Godot) call("get_node", "TabContainer/System fonts/LabelVarInfo");
            Godot valueSetter = (Godot) call("get_node", "TabContainer/System fonts/ValueSetter");
            Godot italic = (Godot) call("get_node", "TabContainer/System fonts/Italic");
            Godot weight = (Godot) call("get_node", "TabContainer/System fonts/Weight");
            Godot vbox = (Godot) call("get_node", "TabContainer/System fonts/VBoxContainer");

            if (labelVarInfo != null) {
                labelVarInfo.call("set", "text", "Loading system fonts is not supported on the Web platform.");
            }
            if (valueSetter != null) valueSetter.call("set", "visible", false);
            if (italic != null) italic.call("set", "visible", false);
            if (weight != null) weight.call("set", "visible", false);
            if (vbox != null) vbox.call("set", "visible", false);
        }

        Godot tree = (Godot) call("get_node", "TabContainer/Text direction/Tree");
        if (tree != null) {
            Godot root = (Godot) tree.call("create_item");
            tree.call("set_hide_root", true);
            Godot first = (Godot) tree.call("create_item", root);
            first.call("set_text", 0, "רֵאשִׁית"); // Hebrew text
            Godot second = (Godot) tree.call("create_item", first);
            second.call("set_text", 0, "שֵׁנִי");
            Godot third = (Godot) tree.call("create_item", second);
            third.call("set_text", 0, "שְׁלִישִׁי");
            Godot fourth = (Godot) tree.call("create_item", third);
            fourth.call("set_text", 0, "fourth");
        }

        Godot fontPreview = (Godot) call("get_node", "TabContainer/Variable fonts/VariableFontPreview");
        if (fontPreview != null) {
            Godot font = (Godot) fontPreview.call("get_theme_font", "font");
            variableFontVariation = font;
        }
    }

    @GodotMethod
    public void _on_Tree_item_selected() {
        Godot tree = (Godot) call("get_node", "TabContainer/Text direction/Tree");
        if (tree == null) return;

        String path = "";
        Godot item = (Godot) tree.call("get_selected");
        while (item != null) {
            String text = (String) item.call("get_text", 0);
            path = text + "/" + path;
            item = (Godot) item.call("get_parent");
        }

        Godot lineEditST = (Godot) call("get_node", "TabContainer/Text direction/LineEditST");
        Godot lineEditNoST = (Godot) call("get_node", "TabContainer/Text direction/LineEditNoST");
        if (lineEditST != null) lineEditST.call("set", "text", path);
        if (lineEditNoST != null) lineEditNoST.call("set", "text", path);
    }

    @GodotMethod
    public void _on_LineEditCustomSTDst_text_changed(String newText) {
        Godot source = (Godot) call("get_node", "TabContainer/Text direction/LineEditCustomSTSource");
        if (source != null) source.call("set", "text", newText);
    }

    @GodotMethod
    public void _on_LineEditCustomSTSource_text_changed(String newText) {
        Godot dst = (Godot) call("get_node", "TabContainer/Text direction/LineEditCustomSTDst");
        if (dst != null) dst.call("set", "text", newText);
    }

    @GodotMethod
    public void _on_LineEditCustomSTDst_tree_entered() {
        Godot source = (Godot) call("get_node", "TabContainer/Text direction/LineEditCustomSTSource");
        Godot dst = (Godot) call("get_node", "TabContainer/Text direction/LineEditCustomSTDst");
        if (source != null && dst != null) {
            dst.call("set", "text", source.call("get", "text"));
        }
    }

    @GodotMethod
    public void _on_variable_size_value_changed(double value) {
        Godot valueLabel = (Godot) call("get_node", "TabContainer/Variable fonts/Variables/Size/Value");
        Godot fontPreview = (Godot) call("get_node", "TabContainer/Variable fonts/VariableFontPreview");
        if (valueLabel != null) valueLabel.call("set", "text", String.valueOf((int) value));
        if (fontPreview != null) fontPreview.call("add_theme_font_size_override", "font_size", (long) value);
    }

    @GodotMethod
    public void _on_variable_weight_value_changed(double value) {
        Godot valueLabel = (Godot) call("get_node", "TabContainer/Variable fonts/Variables/Weight/Value");
        if (valueLabel != null) valueLabel.call("set", "text", String.valueOf((int) value));
        if (variableFontVariation != null) {
            org.godot.collection.GodotDictionary dict = (org.godot.collection.GodotDictionary) variableFontVariation.call("get", "variation_opentype");
            if (dict != null) {
                org.godot.collection.GodotDictionary newDict = (org.godot.collection.GodotDictionary) dict.call("duplicate");
                newDict.put("weight", value);
                variableFontVariation.call("set", "variation_opentype", newDict);
            }
        }
    }

    @GodotMethod
    public void _on_variable_slant_value_changed(double value) {
        Godot valueLabel = (Godot) call("get_node", "TabContainer/Variable fonts/Variables/Slant/Value");
        if (valueLabel != null) valueLabel.call("set", "text", String.valueOf((int) value));
        if (variableFontVariation != null) {
            org.godot.collection.GodotDictionary dict = (org.godot.collection.GodotDictionary) variableFontVariation.call("get", "variation_opentype");
            if (dict != null) {
                org.godot.collection.GodotDictionary newDict = (org.godot.collection.GodotDictionary) dict.call("duplicate");
                newDict.put("slant", value);
                variableFontVariation.call("set", "variation_opentype", newDict);
            }
        }
    }

    @GodotMethod
    public void _on_variable_cursive_toggled(boolean buttonPressed) {
        Godot cursiveBtn = (Godot) call("get_node", "TabContainer/Variable fonts/Variables/Cursive");
        if (cursiveBtn != null) cursiveBtn.call("set", "button_pressed", buttonPressed);
        if (variableFontVariation != null) {
            org.godot.collection.GodotDictionary dict = (org.godot.collection.GodotDictionary) variableFontVariation.call("get", "variation_opentype");
            if (dict != null) {
                org.godot.collection.GodotDictionary newDict = (org.godot.collection.GodotDictionary) dict.call("duplicate");
                newDict.put("custom_CRSV", buttonPressed ? 1 : 0);
                variableFontVariation.call("set", "variation_opentype", newDict);
            }
        }
    }

    @GodotMethod
    public void _on_variable_casual_toggled(boolean buttonPressed) {
        Godot casualBtn = (Godot) call("get_node", "TabContainer/Variable fonts/Variables/Casual");
        if (casualBtn != null) casualBtn.call("set", "button_pressed", buttonPressed);
        if (variableFontVariation != null) {
            org.godot.collection.GodotDictionary dict = (org.godot.collection.GodotDictionary) variableFontVariation.call("get", "variation_opentype");
            if (dict != null) {
                org.godot.collection.GodotDictionary newDict = (org.godot.collection.GodotDictionary) dict.call("duplicate");
                newDict.put("custom_CASL", buttonPressed ? 1 : 0);
                variableFontVariation.call("set", "variation_opentype", newDict);
            }
        }
    }

    @GodotMethod
    public void _on_variable_monospace_toggled(boolean buttonPressed) {
        Godot monoBtn = (Godot) call("get_node", "TabContainer/Variable fonts/Variables/Monospace");
        if (monoBtn != null) monoBtn.call("set", "button_pressed", buttonPressed);
        if (variableFontVariation != null) {
            org.godot.collection.GodotDictionary dict = (org.godot.collection.GodotDictionary) variableFontVariation.call("get", "variation_opentype");
            if (dict != null) {
                org.godot.collection.GodotDictionary newDict = (org.godot.collection.GodotDictionary) dict.call("duplicate");
                newDict.put("custom_MONO", buttonPressed ? 1 : 0);
                variableFontVariation.call("set", "variation_opentype", newDict);
            }
        }
    }

    @GodotMethod
    public void _on_system_font_value_text_changed(String newText) {
        String[] paths = {
            "TabContainer/System fonts/VBoxContainer/SansSerif/Value",
            "TabContainer/System fonts/VBoxContainer/Serif/Value",
            "TabContainer/System fonts/VBoxContainer/Monospace/Value",
            "TabContainer/System fonts/VBoxContainer/Cursive/Value",
            "TabContainer/System fonts/VBoxContainer/Fantasy/Value",
            "TabContainer/System fonts/VBoxContainer/Custom/Value"
        };
        for (String path : paths) {
            Godot label = (Godot) call("get_node", path);
            if (label != null) label.call("set", "text", newText);
        }
    }

    @GodotMethod
    public void _on_system_font_weight_value_changed(double value) {
        Godot valueLabel = (Godot) call("get_node", "TabContainer/System fonts/Weight/Value");
        if (valueLabel != null) valueLabel.call("set", "text", String.valueOf((int) value));

        String[] paths = {
            "TabContainer/System fonts/VBoxContainer/SansSerif/Value",
            "TabContainer/System fonts/VBoxContainer/Serif/Value",
            "TabContainer/System fonts/VBoxContainer/Monospace/Value",
            "TabContainer/System fonts/VBoxContainer/Cursive/Value",
            "TabContainer/System fonts/VBoxContainer/Fantasy/Value",
            "TabContainer/System fonts/VBoxContainer/Custom/Value"
        };
        for (String path : paths) {
            Godot label = (Godot) call("get_node", path);
            if (label != null) {
                Godot systemFont = (Godot) label.call("get_theme_font", "font");
                if (systemFont != null) systemFont.call("set", "font_weight", (int) value);
            }
        }
    }

    @GodotMethod
    public void _on_system_font_italic_toggled(boolean buttonPressed) {
        String[] paths = {
            "TabContainer/System fonts/VBoxContainer/SansSerif/Value",
            "TabContainer/System fonts/VBoxContainer/Serif/Value",
            "TabContainer/System fonts/VBoxContainer/Monospace/Value",
            "TabContainer/System fonts/VBoxContainer/Cursive/Value",
            "TabContainer/System fonts/VBoxContainer/Fantasy/Value",
            "TabContainer/System fonts/VBoxContainer/Custom/Value"
        };
        for (String path : paths) {
            Godot label = (Godot) call("get_node", path);
            if (label != null) {
                Godot systemFont = (Godot) label.call("get_theme_font", "font");
                if (systemFont != null) systemFont.call("set", "font_italic", buttonPressed);
            }
        }
    }

    @GodotMethod
    public void _on_system_font_name_text_changed(String newText) {
        Godot fontLabel = (Godot) call("get_node", "TabContainer/System fonts/VBoxContainer/Custom/FontName");
        if (fontLabel != null) {
            Godot systemFont = (Godot) fontLabel.call("get_theme_font", "font");
            if (systemFont != null) {
                systemFont.call("set", "font_names", new Object[]{newText});
            }
        }
    }
}
