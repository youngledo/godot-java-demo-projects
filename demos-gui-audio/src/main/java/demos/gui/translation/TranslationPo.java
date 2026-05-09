package demos.gui.translation;

import org.godot.annotation.GodotClass;
import org.godot.annotation.GodotMethod;
import org.godot.node.AudioStreamPlayer;
import org.godot.node.PackedScene;
import org.godot.node.Panel;
import org.godot.node.SceneTree;
import org.godot.singleton.ResourceLoader;
import org.godot.singleton.TranslationServer;

@GodotClass(name = "TranslationPo", parent = "Panel")
public class TranslationPo extends Panel {

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
        System.out.println(tr("Hello, this is a translation demo project."));

        int daysPassed = 1 + (int) (Math.random() * 3);
        String plural = trN("One day ago.", "{days} days ago.", daysPassed);
        if (plural != null) {
            plural = plural.replace("{days}", String.valueOf(daysPassed));
            System.out.println(plural);
        }
    }

    @GodotMethod
    public void OnGoToCsvTranslationDemoPressed() {
        SceneTree tree = getTree();
        if (tree != null && ResourceLoader.singleton().load("res://translation_demo_csv.tscn") instanceof PackedScene packedScene) {
            tree.changeSceneToPacked(packedScene);
        }
    }
}
