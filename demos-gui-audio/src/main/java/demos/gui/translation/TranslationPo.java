package demos.gui.translation;

import org.godot.annotation.GodotClass;
import org.godot.annotation.GodotMethod;
import org.godot.node.Panel;
import org.godot.node.Node;
import org.godot.node.SceneTree;

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
        org.godot.singleton.TranslationServer ts = org.godot.singleton.TranslationServer.singleton();
        ts.call("set_locale", "en");
        printIntro();
    }

    @GodotMethod
    public void OnSpanishPressed() {
        org.godot.singleton.TranslationServer ts = org.godot.singleton.TranslationServer.singleton();
        ts.call("set_locale", "es");
        printIntro();
    }

    @GodotMethod
    public void OnJapanesePressed() {
        org.godot.singleton.TranslationServer ts = org.godot.singleton.TranslationServer.singleton();
        ts.call("set_locale", "ja");
        printIntro();
    }

    @GodotMethod
    public void OnPlayAudioPressed() {
        org.godot.node.Node audio = getNode("Audio");
        if (audio != null) audio.call("play");
    }

    private void printIntro() {
        org.godot.singleton.TranslationServer ts = org.godot.singleton.TranslationServer.singleton();
        String locale = (String) ts.call("get_locale");
        String localeName = (String) ts.call("get_locale_name", locale);
        System.out.println("\nLanguage: " + localeName + " (" + locale + ")");
        System.out.println(call("tr", "Hello, this is a translation demo project."));

        // PO plural translation example
        int daysPassed = 1 + (int) (Math.random() * 3);
        String plural = (String) call("tr_n", "One day ago.", "{days} days ago.", daysPassed);
        if (plural != null) {
            plural = plural.replace("{days}", String.valueOf(daysPassed));
            System.out.println(plural);
        }
    }

    @GodotMethod
    public void OnGoToCsvTranslationDemoPressed() {
        org.godot.node.SceneTree tree = getTree();
        if (tree != null) {
            org.godot.node.PackedScene packedScene = (org.godot.node.PackedScene) org.godot.singleton.ResourceLoader.singleton().load("res://translation_demo_csv.tscn");
            if (packedScene != null) {
                tree.changeSceneToPacked(packedScene);
            }
        }
    }
}
