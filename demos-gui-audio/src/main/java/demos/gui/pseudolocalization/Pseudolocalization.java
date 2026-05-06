package demos.gui.pseudolocalization;

import org.godot.annotation.GodotClass;
import org.godot.annotation.GodotMethod;
import org.godot.node.Control;
import org.godot.node.Node;

@GodotClass(name = "Pseudolocalization", parent = "Control")
public class Pseudolocalization extends Control {

    private boolean initialized = false;

    @Override
    public void _ready() {
        if (initialized) return;
        initialized = true;

        org.godot.singleton.ProjectSettings ps = org.godot.singleton.ProjectSettings.singleton();
        org.godot.singleton.TranslationServer ts = org.godot.singleton.TranslationServer.singleton();

        org.godot.node.Node accents = getNode("Main/Pseudolocalization_options/accents");
        org.godot.node.Node toggle = getNode("Main/Pseudolocalization_options/toggle");
        org.godot.node.Node fakebidi = getNode("Main/Pseudolocalization_options/fakebidi");
        org.godot.node.Node doublevowels = getNode("Main/Pseudolocalization_options/doublevowels");
        org.godot.node.Node overrideBtn = getNode("Main/Pseudolocalization_options/override");
        org.godot.node.Node skipplaceholders = getNode("Main/Pseudolocalization_options/skipplaceholders");
        org.godot.node.TextEdit prefixTextEdit = (org.godot.node.TextEdit) getNode("Main/Pseudolocalization_options/prefix/TextEdit");
        org.godot.node.TextEdit suffixTextEdit = (org.godot.node.TextEdit) getNode("Main/Pseudolocalization_options/suffix/TextEdit");
        org.godot.node.SpinBox expRatioSpinBox = (org.godot.node.SpinBox) getNode("Main/Pseudolocalization_options/exp_ratio/SpinBox");

        if (accents != null) accents.setProperty("button_pressed", ps.call("get_setting", "internationalization/pseudolocalization/replace_with_accents"));
        if (toggle != null) toggle.setProperty("button_pressed", ts.getProperty("pseudolocalization_enabled"));
        if (fakebidi != null) fakebidi.setProperty("button_pressed", ps.call("get_setting", "internationalization/pseudolocalization/fake_bidi"));
        if (doublevowels != null) doublevowels.setProperty("button_pressed", ps.call("get_setting", "internationalization/pseudolocalization/double_vowels"));
        if (overrideBtn != null) overrideBtn.setProperty("button_pressed", ps.call("get_setting", "internationalization/pseudolocalization/override"));
        if (skipplaceholders != null) skipplaceholders.setProperty("button_pressed", ps.call("get_setting", "internationalization/pseudolocalization/skip_placeholders"));
        if (prefixTextEdit != null) prefixTextEdit.setProperty("text", ps.call("get_setting", "internationalization/pseudolocalization/prefix"));
        if (suffixTextEdit != null) suffixTextEdit.setProperty("text", ps.call("get_setting", "internationalization/pseudolocalization/suffix"));
        if (expRatioSpinBox != null) {
            Object val = ps.call("get_setting", "internationalization/pseudolocalization/expansion_ratio");
            expRatioSpinBox.setProperty("value", val instanceof Number ? ((Number) val).doubleValue() : 0.0);
        }
    }

    @GodotMethod
    public void OnAccentsToggled(boolean buttonPressed) {
        org.godot.singleton.ProjectSettings ps = org.godot.singleton.ProjectSettings.singleton();
        org.godot.singleton.TranslationServer ts = org.godot.singleton.TranslationServer.singleton();
        ps.call("set_setting", "internationalization/pseudolocalization/replace_with_accents", buttonPressed);
        ts.call("reload_pseudolocalization");
    }

    @GodotMethod
    public void OnToggleToggled(boolean buttonPressed) {
        org.godot.singleton.TranslationServer ts = org.godot.singleton.TranslationServer.singleton();
        ts.setProperty("pseudolocalization_enabled", buttonPressed);
    }

    @GodotMethod
    public void OnFakeBidiToggled(boolean buttonPressed) {
        org.godot.singleton.ProjectSettings ps = org.godot.singleton.ProjectSettings.singleton();
        org.godot.singleton.TranslationServer ts = org.godot.singleton.TranslationServer.singleton();
        ps.call("set_setting", "internationalization/pseudolocalization/fake_bidi", buttonPressed);
        ts.call("reload_pseudolocalization");
    }

    @GodotMethod
    public void OnPrefixChanged(String newText) {
        org.godot.singleton.ProjectSettings ps = org.godot.singleton.ProjectSettings.singleton();
        org.godot.singleton.TranslationServer ts = org.godot.singleton.TranslationServer.singleton();
        ps.call("set_setting", "internationalization/pseudolocalization/prefix", newText);
        ts.call("reload_pseudolocalization");
    }

    @GodotMethod
    public void OnSuffixChanged(String newText) {
        org.godot.singleton.ProjectSettings ps = org.godot.singleton.ProjectSettings.singleton();
        org.godot.singleton.TranslationServer ts = org.godot.singleton.TranslationServer.singleton();
        ps.call("set_setting", "internationalization/pseudolocalization/suffix", newText);
        ts.call("reload_pseudolocalization");
    }

    @GodotMethod
    public void OnPseudolocalizePressed() {
        org.godot.singleton.TranslationServer ts = org.godot.singleton.TranslationServer.singleton();
        org.godot.node.Node keyTextEdit = getNode("Main/Pseudolocalizer/Key");
        org.godot.node.Node resultTextEdit = getNode("Main/Pseudolocalizer/Result");
        if (keyTextEdit != null && resultTextEdit != null) {
            String keyText = (String) keyTextEdit.getProperty("text");
            String result = (String) ts.call("pseudolocalize", keyText);
            resultTextEdit.setProperty("text", result);
        }
    }

    @GodotMethod
    public void OnDoubleVowelsToggled(boolean buttonPressed) {
        org.godot.singleton.ProjectSettings ps = org.godot.singleton.ProjectSettings.singleton();
        org.godot.singleton.TranslationServer ts = org.godot.singleton.TranslationServer.singleton();
        ps.call("set_setting", "internationalization/pseudolocalization/double_vowels", buttonPressed);
        ts.call("reload_pseudolocalization");
    }

    @GodotMethod
    public void OnExpansionRatioValueChanged(double value) {
        org.godot.singleton.ProjectSettings ps = org.godot.singleton.ProjectSettings.singleton();
        org.godot.singleton.TranslationServer ts = org.godot.singleton.TranslationServer.singleton();
        ps.call("set_setting", "internationalization/pseudolocalization/expansion_ratio", value);
        ts.call("reload_pseudolocalization");
    }

    @GodotMethod
    public void OnOverrideToggled(boolean buttonPressed) {
        org.godot.singleton.ProjectSettings ps = org.godot.singleton.ProjectSettings.singleton();
        org.godot.singleton.TranslationServer ts = org.godot.singleton.TranslationServer.singleton();
        ps.call("set_setting", "internationalization/pseudolocalization/override", buttonPressed);
        ts.call("reload_pseudolocalization");
    }

    @GodotMethod
    public void OnSkipPlaceholdersToggled(boolean buttonPressed) {
        org.godot.singleton.ProjectSettings ps = org.godot.singleton.ProjectSettings.singleton();
        org.godot.singleton.TranslationServer ts = org.godot.singleton.TranslationServer.singleton();
        ps.call("set_setting", "internationalization/pseudolocalization/skip_placeholders", buttonPressed);
        ts.call("reload_pseudolocalization");
    }
}
