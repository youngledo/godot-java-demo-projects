package demos.gui.pseudolocalization;

import org.godot.annotation.GodotClass;
import org.godot.annotation.GodotMethod;
import org.godot.node.BaseButton;
import org.godot.node.Control;
import org.godot.node.SpinBox;
import org.godot.node.TextEdit;
import org.godot.singleton.ProjectSettings;
import org.godot.singleton.TranslationServer;

@GodotClass(name = "Pseudolocalization", parent = "Control")
public class Pseudolocalization extends Control {

    private boolean initialized = false;

    @Override
    public void _ready() {
        if (initialized) return;
        initialized = true;

        ProjectSettings ps = ProjectSettings.singleton();
        TranslationServer ts = TranslationServer.singleton();

        BaseButton accents = getNodeAs("Main/Pseudolocalization_options/accents", BaseButton.class);
        BaseButton toggle = getNodeAs("Main/Pseudolocalization_options/toggle", BaseButton.class);
        BaseButton fakebidi = getNodeAs("Main/Pseudolocalization_options/fakebidi", BaseButton.class);
        BaseButton doublevowels = getNodeAs("Main/Pseudolocalization_options/doublevowels", BaseButton.class);
        BaseButton overrideBtn = getNodeAs("Main/Pseudolocalization_options/override", BaseButton.class);
        BaseButton skipplaceholders = getNodeAs("Main/Pseudolocalization_options/skipplaceholders", BaseButton.class);
        TextEdit prefixTextEdit = getNodeAs("Main/Pseudolocalization_options/prefix/TextEdit", TextEdit.class);
        TextEdit suffixTextEdit = getNodeAs("Main/Pseudolocalization_options/suffix/TextEdit", TextEdit.class);
        SpinBox expRatioSpinBox = getNodeAs("Main/Pseudolocalization_options/exp_ratio/SpinBox", SpinBox.class);

        if (accents != null) accents.setButtonPressed(settingBool(ps, "internationalization/pseudolocalization/replace_with_accents"));
        if (toggle != null) toggle.setButtonPressed(ts.isPseudolocalizationEnabled());
        if (fakebidi != null) fakebidi.setButtonPressed(settingBool(ps, "internationalization/pseudolocalization/fake_bidi"));
        if (doublevowels != null) doublevowels.setButtonPressed(settingBool(ps, "internationalization/pseudolocalization/double_vowels"));
        if (overrideBtn != null) overrideBtn.setButtonPressed(settingBool(ps, "internationalization/pseudolocalization/override"));
        if (skipplaceholders != null) skipplaceholders.setButtonPressed(settingBool(ps, "internationalization/pseudolocalization/skip_placeholders"));
        if (prefixTextEdit != null) prefixTextEdit.setText(settingString(ps, "internationalization/pseudolocalization/prefix"));
        if (suffixTextEdit != null) suffixTextEdit.setText(settingString(ps, "internationalization/pseudolocalization/suffix"));
        if (expRatioSpinBox != null) {
            Object val = ps.getSetting("internationalization/pseudolocalization/expansion_ratio");
            expRatioSpinBox.setValue(val instanceof Number number ? number.doubleValue() : 0.0);
        }
    }

    @GodotMethod
    public void OnAccentsToggled(boolean buttonPressed) {
        setSettingAndReload("internationalization/pseudolocalization/replace_with_accents", buttonPressed);
    }

    @GodotMethod
    public void OnToggleToggled(boolean buttonPressed) {
        TranslationServer.singleton().setPseudolocalizationEnabled(buttonPressed);
    }

    @GodotMethod
    public void OnFakeBidiToggled(boolean buttonPressed) {
        setSettingAndReload("internationalization/pseudolocalization/fake_bidi", buttonPressed);
    }

    @GodotMethod
    public void OnPrefixChanged(String newText) {
        setSettingAndReload("internationalization/pseudolocalization/prefix", newText);
    }

    @GodotMethod
    public void OnSuffixChanged(String newText) {
        setSettingAndReload("internationalization/pseudolocalization/suffix", newText);
    }

    @GodotMethod
    public void OnPseudolocalizePressed() {
        TextEdit keyTextEdit = getNodeAs("Main/Pseudolocalizer/Key", TextEdit.class);
        TextEdit resultTextEdit = getNodeAs("Main/Pseudolocalizer/Result", TextEdit.class);
        if (keyTextEdit != null && resultTextEdit != null) {
            resultTextEdit.setText(TranslationServer.singleton().pseudolocalize(keyTextEdit.getText()));
        }
    }

    @GodotMethod
    public void OnDoubleVowelsToggled(boolean buttonPressed) {
        setSettingAndReload("internationalization/pseudolocalization/double_vowels", buttonPressed);
    }

    @GodotMethod
    public void OnExpansionRatioValueChanged(double value) {
        setSettingAndReload("internationalization/pseudolocalization/expansion_ratio", value);
    }

    @GodotMethod
    public void OnOverrideToggled(boolean buttonPressed) {
        setSettingAndReload("internationalization/pseudolocalization/override", buttonPressed);
    }

    @GodotMethod
    public void OnSkipPlaceholdersToggled(boolean buttonPressed) {
        setSettingAndReload("internationalization/pseudolocalization/skip_placeholders", buttonPressed);
    }

    private void setSettingAndReload(String setting, Object value) {
        ProjectSettings.singleton().setSetting(setting, value);
        TranslationServer.singleton().reloadPseudolocalization();
    }

    private boolean settingBool(ProjectSettings projectSettings, String setting) {
        return Boolean.TRUE.equals(projectSettings.getSetting(setting));
    }

    private String settingString(ProjectSettings projectSettings, String setting) {
        Object value = projectSettings.getSetting(setting);
        return value == null ? "" : String.valueOf(value);
    }
}
