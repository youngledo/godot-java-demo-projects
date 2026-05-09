package demos.gui.bidi_and_font_features;

import org.godot.annotation.GodotClass;
import org.godot.annotation.GodotMethod;
import org.godot.collection.GodotDictionary;
import org.godot.node.BaseButton;
import org.godot.node.Control;
import org.godot.node.Font;
import org.godot.node.FontVariation;
import org.godot.node.Label;
import org.godot.node.LineEdit;
import org.godot.node.SystemFont;
import org.godot.node.Tree;
import org.godot.node.TreeItem;
import org.godot.singleton.OS;

@GodotClass(name = "Bidi", parent = "Control")
public class Bidi extends Control {

    private static final String[] SYSTEM_FONT_VALUE_PATHS = {
        "TabContainer/System fonts/VBoxContainer/SansSerif/Value",
        "TabContainer/System fonts/VBoxContainer/Serif/Value",
        "TabContainer/System fonts/VBoxContainer/Monospace/Value",
        "TabContainer/System fonts/VBoxContainer/Cursive/Value",
        "TabContainer/System fonts/VBoxContainer/Fantasy/Value",
        "TabContainer/System fonts/VBoxContainer/Custom/Value"
    };

    private FontVariation variableFontVariation;
    private boolean initialized = false;

    @Override
    public void _ready() {
        if (initialized) return;
        initialized = true;

        if (OS.singleton().hasFeature("web")) {
            Label labelVarInfo = getNodeAs("TabContainer/System fonts/LabelVarInfo", Label.class);
            Control valueSetter = getNodeAs("TabContainer/System fonts/ValueSetter", Control.class);
            Control italic = getNodeAs("TabContainer/System fonts/Italic", Control.class);
            Control weight = getNodeAs("TabContainer/System fonts/Weight", Control.class);
            Control vbox = getNodeAs("TabContainer/System fonts/VBoxContainer", Control.class);

            if (labelVarInfo != null) labelVarInfo.setText("Loading system fonts is not supported on the Web platform.");
            if (valueSetter != null) valueSetter.setVisible(false);
            if (italic != null) italic.setVisible(false);
            if (weight != null) weight.setVisible(false);
            if (vbox != null) vbox.setVisible(false);
        }

        Tree tree = getNodeAs("TabContainer/Text direction/Tree", Tree.class);
        if (tree != null) {
            TreeItem root = tree.createItem();
            tree.setHideRoot(true);
            TreeItem first = tree.createItem(root);
            first.setText(0, "רֵאשִׁית");
            TreeItem second = tree.createItem(first);
            second.setText(0, "שֵׁנִי");
            TreeItem third = tree.createItem(second);
            third.setText(0, "שְׁלִישִׁי");
            TreeItem fourth = tree.createItem(third);
            fourth.setText(0, "fourth");
        }

        Control fontPreview = getNodeAs("TabContainer/Variable fonts/VariableFontPreview", Control.class);
        if (fontPreview != null && fontPreview.getThemeFont("font") instanceof FontVariation fontVariation) {
            variableFontVariation = fontVariation;
        }
    }

    @GodotMethod
    public void OnTreeItemSelected() {
        Tree tree = getNodeAs("TabContainer/Text direction/Tree", Tree.class);
        if (tree == null) return;

        String path = "";
        TreeItem item = tree.getSelected();
        while (item != null) {
            path = item.getText(0) + "/" + path;
            item = item.getParent();
        }

        LineEdit lineEditST = getNodeAs("TabContainer/Text direction/LineEditST", LineEdit.class);
        LineEdit lineEditNoST = getNodeAs("TabContainer/Text direction/LineEditNoST", LineEdit.class);
        if (lineEditST != null) lineEditST.setText(path);
        if (lineEditNoST != null) lineEditNoST.setText(path);
    }

    @GodotMethod
    public void OnLineEditCustomSTDstTextChanged(String newText) {
        LineEdit source = getNodeAs("TabContainer/Text direction/LineEditCustomSTSource", LineEdit.class);
        if (source != null) source.setText(newText);
    }

    @GodotMethod
    public void OnLineEditCustomSTSourceTextChanged(String newText) {
        LineEdit dst = getNodeAs("TabContainer/Text direction/LineEditCustomSTDst", LineEdit.class);
        if (dst != null) dst.setText(newText);
    }

    @GodotMethod
    public void OnLineEditCustomSTDstTreeEntered() {
        LineEdit source = getNodeAs("TabContainer/Text direction/LineEditCustomSTSource", LineEdit.class);
        LineEdit dst = getNodeAs("TabContainer/Text direction/LineEditCustomSTDst", LineEdit.class);
        if (source != null && dst != null) {
            dst.setText(source.getText());
        }
    }

    @GodotMethod
    public void OnVariableSizeValueChanged(double value) {
        Label valueLabel = getNodeAs("TabContainer/Variable fonts/Variables/Size/Value", Label.class);
        Control fontPreview = getNodeAs("TabContainer/Variable fonts/VariableFontPreview", Control.class);
        if (valueLabel != null) valueLabel.setText(String.valueOf((int) value));
        if (fontPreview != null) fontPreview.addThemeFontSizeOverride("font_size", (long) value);
    }

    @GodotMethod
    public void OnVariableWeightValueChanged(double value) {
        Label valueLabel = getNodeAs("TabContainer/Variable fonts/Variables/Weight/Value", Label.class);
        if (valueLabel != null) valueLabel.setText(String.valueOf((int) value));
        setVariableFontAxis("weight", value);
    }

    @GodotMethod
    public void OnVariableSlantValueChanged(double value) {
        Label valueLabel = getNodeAs("TabContainer/Variable fonts/Variables/Slant/Value", Label.class);
        if (valueLabel != null) valueLabel.setText(String.valueOf((int) value));
        setVariableFontAxis("slant", value);
    }

    @GodotMethod
    public void OnVariableCursiveToggled(boolean buttonPressed) {
        BaseButton cursiveBtn = getNodeAs("TabContainer/Variable fonts/Variables/Cursive", BaseButton.class);
        if (cursiveBtn != null) cursiveBtn.setButtonPressed(buttonPressed);
        setVariableFontAxis("custom_CRSV", buttonPressed ? 1 : 0);
    }

    @GodotMethod
    public void OnVariableCasualToggled(boolean buttonPressed) {
        BaseButton casualBtn = getNodeAs("TabContainer/Variable fonts/Variables/Casual", BaseButton.class);
        if (casualBtn != null) casualBtn.setButtonPressed(buttonPressed);
        setVariableFontAxis("custom_CASL", buttonPressed ? 1 : 0);
    }

    @GodotMethod
    public void OnVariableMonospaceToggled(boolean buttonPressed) {
        BaseButton monoBtn = getNodeAs("TabContainer/Variable fonts/Variables/Monospace", BaseButton.class);
        if (monoBtn != null) monoBtn.setButtonPressed(buttonPressed);
        setVariableFontAxis("custom_MONO", buttonPressed ? 1 : 0);
    }

    @GodotMethod
    public void OnSystemFontValueTextChanged(String newText) {
        for (String path : SYSTEM_FONT_VALUE_PATHS) {
            Label label = getNodeAs(path, Label.class);
            if (label != null) label.setText(newText);
        }
    }

    @GodotMethod
    public void OnSystemFontWeightValueChanged(double value) {
        Label valueLabel = getNodeAs("TabContainer/System fonts/Weight/Value", Label.class);
        if (valueLabel != null) valueLabel.setText(String.valueOf((int) value));

        for (String path : SYSTEM_FONT_VALUE_PATHS) {
            Control label = getNodeAs(path, Control.class);
            if (label != null && label.getThemeFont("font") instanceof SystemFont systemFont) {
                systemFont.setProperty("font_weight", (int) value);
            }
        }
    }

    @GodotMethod
    public void OnSystemFontItalicToggled(boolean buttonPressed) {
        for (String path : SYSTEM_FONT_VALUE_PATHS) {
            Control label = getNodeAs(path, Control.class);
            if (label != null && label.getThemeFont("font") instanceof SystemFont systemFont) {
                systemFont.setFontItalic(buttonPressed);
            }
        }
    }

    @GodotMethod
    public void OnSystemFontNameTextChanged(String newText) {
        Control fontLabel = getNodeAs("TabContainer/System fonts/VBoxContainer/Custom/FontName", Control.class);
        if (fontLabel != null) {
            Font font = fontLabel.getThemeFont("font");
            if (font instanceof SystemFont systemFont) {
                systemFont.setFontNames(new String[] { newText });
            }
        }
    }

    private void setVariableFontAxis(String axis, Object value) {
        if (variableFontVariation == null) return;
        GodotDictionary variation = variableFontVariation.getVariationOpentype();
        if (variation == null) return;
        variation.put(axis, value);
        variableFontVariation.setVariationOpentype(variation);
    }
}
