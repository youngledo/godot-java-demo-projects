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
            Godot labelVarInfo = (Godot) getNode("TabContainer/System fonts/LabelVarInfo");
            Godot valueSetter = (Godot) getNode("TabContainer/System fonts/ValueSetter");
            Godot italic = (Godot) getNode("TabContainer/System fonts/Italic");
            Godot weight = (Godot) getNode("TabContainer/System fonts/Weight");
            Godot vbox = (Godot) getNode("TabContainer/System fonts/VBoxContainer");

            if (labelVarInfo != null) {
                labelVarInfo.setProperty("text", "Loading system fonts is not supported on the Web platform.");
            }
            if (valueSetter != null) valueSetter.setProperty("visible", false);
            if (italic != null) italic.setProperty("visible", false);
            if (weight != null) weight.setProperty("visible", false);
            if (vbox != null) vbox.setProperty("visible", false);
        }

        Godot tree = (Godot) getNode("TabContainer/Text direction/Tree");
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

        Godot fontPreview = (Godot) getNode("TabContainer/Variable fonts/VariableFontPreview");
        if (fontPreview != null) {
            Godot font = (Godot) fontPreview.call("get_theme_font", "font");
            variableFontVariation = font;
        }
    }

    @GodotMethod
    public void OnTreeItemSelected() {
        Godot tree = (Godot) getNode("TabContainer/Text direction/Tree");
        if (tree == null) return;

        String path = "";
        Godot item = (Godot) tree.call("get_selected");
        while (item != null) {
            String text = (String) item.call("get_text", 0);
            path = text + "/" + path;
            item = (Godot) item.call("get_parent");
        }

        Godot lineEditST = (Godot) getNode("TabContainer/Text direction/LineEditST");
        Godot lineEditNoST = (Godot) getNode("TabContainer/Text direction/LineEditNoST");
        if (lineEditST != null) lineEditST.setProperty("text", path);
        if (lineEditNoST != null) lineEditNoST.setProperty("text", path);
    }

    @GodotMethod
    public void OnLineEditCustomSTDstTextChanged(String newText) {
        Godot source = (Godot) getNode("TabContainer/Text direction/LineEditCustomSTSource");
        if (source != null) source.setProperty("text", newText);
    }

    @GodotMethod
    public void OnLineEditCustomSTSourceTextChanged(String newText) {
        Godot dst = (Godot) getNode("TabContainer/Text direction/LineEditCustomSTDst");
        if (dst != null) dst.setProperty("text", newText);
    }

    @GodotMethod
    public void OnLineEditCustomSTDstTreeEntered() {
        Godot source = (Godot) getNode("TabContainer/Text direction/LineEditCustomSTSource");
        Godot dst = (Godot) getNode("TabContainer/Text direction/LineEditCustomSTDst");
        if (source != null && dst != null) {
            dst.setProperty("text", source.getProperty("text"));
        }
    }

    @GodotMethod
    public void OnVariableSizeValueChanged(double value) {
        Godot valueLabel = (Godot) getNode("TabContainer/Variable fonts/Variables/Size/Value");
        Godot fontPreview = (Godot) getNode("TabContainer/Variable fonts/VariableFontPreview");
        if (valueLabel != null) valueLabel.setProperty("text", String.valueOf((int) value));
        if (fontPreview != null) fontPreview.call("add_theme_font_size_override", "font_size", (long) value);
    }

    @GodotMethod
    public void OnVariableWeightValueChanged(double value) {
        Godot valueLabel = (Godot) getNode("TabContainer/Variable fonts/Variables/Weight/Value");
        if (valueLabel != null) valueLabel.setProperty("text", String.valueOf((int) value));
        if (variableFontVariation != null) {
            org.godot.collection.GodotDictionary dict = (org.godot.collection.GodotDictionary) variableFontVariation.getProperty("variation_opentype");
            if (dict != null) {
                org.godot.collection.GodotDictionary newDict = (org.godot.collection.GodotDictionary) dict.call("duplicate");
                newDict.put("weight", value);
                variableFontVariation.setProperty("variation_opentype", newDict);
            }
        }
    }

    @GodotMethod
    public void OnVariableSlantValueChanged(double value) {
        Godot valueLabel = (Godot) getNode("TabContainer/Variable fonts/Variables/Slant/Value");
        if (valueLabel != null) valueLabel.setProperty("text", String.valueOf((int) value));
        if (variableFontVariation != null) {
            org.godot.collection.GodotDictionary dict = (org.godot.collection.GodotDictionary) variableFontVariation.getProperty("variation_opentype");
            if (dict != null) {
                org.godot.collection.GodotDictionary newDict = (org.godot.collection.GodotDictionary) dict.call("duplicate");
                newDict.put("slant", value);
                variableFontVariation.setProperty("variation_opentype", newDict);
            }
        }
    }

    @GodotMethod
    public void OnVariableCursiveToggled(boolean buttonPressed) {
        Godot cursiveBtn = (Godot) getNode("TabContainer/Variable fonts/Variables/Cursive");
        if (cursiveBtn != null) cursiveBtn.setProperty("button_pressed", buttonPressed);
        if (variableFontVariation != null) {
            org.godot.collection.GodotDictionary dict = (org.godot.collection.GodotDictionary) variableFontVariation.getProperty("variation_opentype");
            if (dict != null) {
                org.godot.collection.GodotDictionary newDict = (org.godot.collection.GodotDictionary) dict.call("duplicate");
                newDict.put("custom_CRSV", buttonPressed ? 1 : 0);
                variableFontVariation.setProperty("variation_opentype", newDict);
            }
        }
    }

    @GodotMethod
    public void OnVariableCasualToggled(boolean buttonPressed) {
        Godot casualBtn = (Godot) getNode("TabContainer/Variable fonts/Variables/Casual");
        if (casualBtn != null) casualBtn.setProperty("button_pressed", buttonPressed);
        if (variableFontVariation != null) {
            org.godot.collection.GodotDictionary dict = (org.godot.collection.GodotDictionary) variableFontVariation.getProperty("variation_opentype");
            if (dict != null) {
                org.godot.collection.GodotDictionary newDict = (org.godot.collection.GodotDictionary) dict.call("duplicate");
                newDict.put("custom_CASL", buttonPressed ? 1 : 0);
                variableFontVariation.setProperty("variation_opentype", newDict);
            }
        }
    }

    @GodotMethod
    public void OnVariableMonospaceToggled(boolean buttonPressed) {
        Godot monoBtn = (Godot) getNode("TabContainer/Variable fonts/Variables/Monospace");
        if (monoBtn != null) monoBtn.setProperty("button_pressed", buttonPressed);
        if (variableFontVariation != null) {
            org.godot.collection.GodotDictionary dict = (org.godot.collection.GodotDictionary) variableFontVariation.getProperty("variation_opentype");
            if (dict != null) {
                org.godot.collection.GodotDictionary newDict = (org.godot.collection.GodotDictionary) dict.call("duplicate");
                newDict.put("custom_MONO", buttonPressed ? 1 : 0);
                variableFontVariation.setProperty("variation_opentype", newDict);
            }
        }
    }

    @GodotMethod
    public void OnSystemFontValueTextChanged(String newText) {
        String[] paths = {
            "TabContainer/System fonts/VBoxContainer/SansSerif/Value",
            "TabContainer/System fonts/VBoxContainer/Serif/Value",
            "TabContainer/System fonts/VBoxContainer/Monospace/Value",
            "TabContainer/System fonts/VBoxContainer/Cursive/Value",
            "TabContainer/System fonts/VBoxContainer/Fantasy/Value",
            "TabContainer/System fonts/VBoxContainer/Custom/Value"
        };
        for (String path : paths) {
            Godot label = (Godot) getNode(path);
            if (label != null) label.setProperty("text", newText);
        }
    }

    @GodotMethod
    public void OnSystemFontWeightValueChanged(double value) {
        Godot valueLabel = (Godot) getNode("TabContainer/System fonts/Weight/Value");
        if (valueLabel != null) valueLabel.setProperty("text", String.valueOf((int) value));

        String[] paths = {
            "TabContainer/System fonts/VBoxContainer/SansSerif/Value",
            "TabContainer/System fonts/VBoxContainer/Serif/Value",
            "TabContainer/System fonts/VBoxContainer/Monospace/Value",
            "TabContainer/System fonts/VBoxContainer/Cursive/Value",
            "TabContainer/System fonts/VBoxContainer/Fantasy/Value",
            "TabContainer/System fonts/VBoxContainer/Custom/Value"
        };
        for (String path : paths) {
            Godot label = (Godot) getNode(path);
            if (label != null) {
                Godot systemFont = (Godot) label.call("get_theme_font", "font");
                if (systemFont != null) systemFont.setProperty("font_weight", (int) value);
            }
        }
    }

    @GodotMethod
    public void OnSystemFontItalicToggled(boolean buttonPressed) {
        String[] paths = {
            "TabContainer/System fonts/VBoxContainer/SansSerif/Value",
            "TabContainer/System fonts/VBoxContainer/Serif/Value",
            "TabContainer/System fonts/VBoxContainer/Monospace/Value",
            "TabContainer/System fonts/VBoxContainer/Cursive/Value",
            "TabContainer/System fonts/VBoxContainer/Fantasy/Value",
            "TabContainer/System fonts/VBoxContainer/Custom/Value"
        };
        for (String path : paths) {
            Godot label = (Godot) getNode(path);
            if (label != null) {
                Godot systemFont = (Godot) label.call("get_theme_font", "font");
                if (systemFont != null) systemFont.setProperty("font_italic", buttonPressed);
            }
        }
    }

    @GodotMethod
    public void OnSystemFontNameTextChanged(String newText) {
        Godot fontLabel = (Godot) getNode("TabContainer/System fonts/VBoxContainer/Custom/FontName");
        if (fontLabel != null) {
            Godot systemFont = (Godot) fontLabel.call("get_theme_font", "font");
            if (systemFont != null) {
                systemFont.setProperty("font_names", new Object[]{newText});
            }
        }
    }
}
