package demos.gui.pseudolocalization;

import org.godot.annotation.GodotClass;
import org.godot.annotation.GodotMethod;
import org.godot.node.Control;

@GodotClass(name = "Pseudolocalization", parent = "Control")
public class Pseudolocalization extends Control {

    private boolean initialized = false;

    @Override
    public void _ready() {
        if (initialized) return;
        initialized = true;

        org.godot.singleton.ProjectSettings ps = org.godot.singleton.ProjectSettings.singleton();
        org.godot.singleton.TranslationServer ts = org.godot.singleton.TranslationServer.singleton();

        org.godot.Godot accents = (org.godot.Godot) call("get_node", "Main/Pseudolocalization_options/accents");
        org.godot.Godot toggle = (org.godot.Godot) call("get_node", "Main/Pseudolocalization_options/toggle");
        org.godot.Godot fakebidi = (org.godot.Godot) call("get_node", "Main/Pseudolocalization_options/fakebidi");
        org.godot.Godot doublevowels = (org.godot.Godot) call("get_node", "Main/Pseudolocalization_options/doublevowels");
        org.godot.Godot overrideBtn = (org.godot.Godot) call("get_node", "Main/Pseudolocalization_options/override");
        org.godot.Godot skipplaceholders = (org.godot.Godot) call("get_node", "Main/Pseudolocalization_options/skipplaceholders");
        org.godot.Godot prefixTextEdit = (org.godot.Godot) call("get_node", "Main/Pseudolocalization_options/prefix/TextEdit");
        org.godot.Godot suffixTextEdit = (org.godot.Godot) call("get_node", "Main/Pseudolocalization_options/suffix/TextEdit");
        org.godot.Godot expRatioSpinBox = (org.godot.Godot) call("get_node", "Main/Pseudolocalization_options/exp_ratio/SpinBox");

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
    public void _on_accents_toggled(boolean buttonPressed) {
        org.godot.singleton.ProjectSettings ps = org.godot.singleton.ProjectSettings.singleton();
        org.godot.singleton.TranslationServer ts = org.godot.singleton.TranslationServer.singleton();
        ps.call("set_setting", "internationalization/pseudolocalization/replace_with_accents", buttonPressed);
        ts.call("reload_pseudolocalization");
    }

    @GodotMethod
    public void _on_toggle_toggled(boolean buttonPressed) {
        org.godot.singleton.TranslationServer ts = org.godot.singleton.TranslationServer.singleton();
        ts.setProperty("pseudolocalization_enabled", buttonPressed);
    }

    @GodotMethod
    public void _on_fake_bidi_toggled(boolean buttonPressed) {
        org.godot.singleton.ProjectSettings ps = org.godot.singleton.ProjectSettings.singleton();
        org.godot.singleton.TranslationServer ts = org.godot.singleton.TranslationServer.singleton();
        ps.call("set_setting", "internationalization/pseudolocalization/fake_bidi", buttonPressed);
        ts.call("reload_pseudolocalization");
    }

    @GodotMethod
    public void _on_prefix_changed(String newText) {
        org.godot.singleton.ProjectSettings ps = org.godot.singleton.ProjectSettings.singleton();
        org.godot.singleton.TranslationServer ts = org.godot.singleton.TranslationServer.singleton();
        ps.call("set_setting", "internationalization/pseudolocalization/prefix", newText);
        ts.call("reload_pseudolocalization");
    }

    @GodotMethod
    public void _on_suffix_changed(String newText) {
        org.godot.singleton.ProjectSettings ps = org.godot.singleton.ProjectSettings.singleton();
        org.godot.singleton.TranslationServer ts = org.godot.singleton.TranslationServer.singleton();
        ps.call("set_setting", "internationalization/pseudolocalization/suffix", newText);
        ts.call("reload_pseudolocalization");
    }

    @GodotMethod
    public void _on_pseudolocalize_pressed() {
        org.godot.singleton.TranslationServer ts = org.godot.singleton.TranslationServer.singleton();
        org.godot.Godot keyTextEdit = (org.godot.Godot) call("get_node", "Main/Pseudolocalizer/Key");
        org.godot.Godot resultTextEdit = (org.godot.Godot) call("get_node", "Main/Pseudolocalizer/Result");
        if (keyTextEdit != null && resultTextEdit != null) {
            String keyText = (String) keyTextEdit.getProperty("text");
            String result = (String) ts.call("pseudolocalize", keyText);
            resultTextEdit.setProperty("text", result);
        }
    }

    @GodotMethod
    public void _on_double_vowels_toggled(boolean buttonPressed) {
        org.godot.singleton.ProjectSettings ps = org.godot.singleton.ProjectSettings.singleton();
        org.godot.singleton.TranslationServer ts = org.godot.singleton.TranslationServer.singleton();
        ps.call("set_setting", "internationalization/pseudolocalization/double_vowels", buttonPressed);
        ts.call("reload_pseudolocalization");
    }

    @GodotMethod
    public void _on_expansion_ratio_value_changed(double value) {
        org.godot.singleton.ProjectSettings ps = org.godot.singleton.ProjectSettings.singleton();
        org.godot.singleton.TranslationServer ts = org.godot.singleton.TranslationServer.singleton();
        ps.call("set_setting", "internationalization/pseudolocalization/expansion_ratio", value);
        ts.call("reload_pseudolocalization");
    }

    @GodotMethod
    public void _on_override_toggled(boolean buttonPressed) {
        org.godot.singleton.ProjectSettings ps = org.godot.singleton.ProjectSettings.singleton();
        org.godot.singleton.TranslationServer ts = org.godot.singleton.TranslationServer.singleton();
        ps.call("set_setting", "internationalization/pseudolocalization/override", buttonPressed);
        ts.call("reload_pseudolocalization");
    }

    @GodotMethod
    public void _on_skip_placeholders_toggled(boolean buttonPressed) {
        org.godot.singleton.ProjectSettings ps = org.godot.singleton.ProjectSettings.singleton();
        org.godot.singleton.TranslationServer ts = org.godot.singleton.TranslationServer.singleton();
        ps.call("set_setting", "internationalization/pseudolocalization/skip_placeholders", buttonPressed);
        ts.call("reload_pseudolocalization");
    }
}
