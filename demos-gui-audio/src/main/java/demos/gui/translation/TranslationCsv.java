package demos.gui.translation;

import org.godot.annotation.GodotClass;
import org.godot.annotation.GodotMethod;
import org.godot.node.AudioStreamPlayer;
import org.godot.node.PackedScene;
import org.godot.node.Panel;
import org.godot.node.SceneTree;
import org.godot.singleton.ResourceLoader;
import org.godot.singleton.TranslationServer;

@GodotClass(name = "TranslationCsv", parent = "Panel")
public class TranslationCsv extends Panel {

    private boolean initialized = false;

    @Override
    public void _ready() {
        if (initialized) return;
        initialized = true;

        printIntro();
    }

    @GodotMethod
    public void OnEnglishPressed() {
        setLocale("en");
    }

    @GodotMethod
    public void OnSpanishPressed() {
        setLocale("es");
    }

    @GodotMethod
    public void OnJapanesePressed() {
        setLocale("ja");
    }

    @GodotMethod
    public void OnRussianPressed() {
        setLocale("ru");
    }

    @GodotMethod
    public void OnPlayAudioPressed() {
        AudioStreamPlayer audio = getNodeAs("Audio", AudioStreamPlayer.class);
        if (audio != null) audio.play();
    }

    private void setLocale(String locale) {
        TranslationServer.singleton().setLocale(locale);
        printIntro();
    }

    private void printIntro() {
        TranslationServer ts = TranslationServer.singleton();
        String locale = ts.getLocale();
        String localeName = ts.getLocaleName(locale);
        System.out.println("\nLanguage: " + localeName + " (" + locale + ")");
        System.out.println(tr("KEY_INTRO"));
    }

    @GodotMethod
    public void OnGoToPoTranslationDemoPressed() {
        SceneTree tree = getTree();
        if (tree != null && ResourceLoader.singleton().load("res://translation_demo_po.tscn") instanceof PackedScene packedScene) {
            tree.changeSceneToPacked(packedScene);
        }
    }
}
