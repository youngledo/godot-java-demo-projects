package demos.gui.translation;

import org.godot.annotation.GodotClass;
import org.godot.annotation.GodotMethod;
import org.godot.node.Panel;
import org.godot.node.Node;
import org.godot.node.SceneTree;

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
    public void OnRussianPressed() {
        org.godot.singleton.TranslationServer ts = org.godot.singleton.TranslationServer.singleton();
        ts.call("set_locale", "ru");
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
        System.out.println(call("tr", "KEY_INTRO"));
    }

    @GodotMethod
    public void OnGoToPoTranslationDemoPressed() {
        org.godot.node.SceneTree tree = getTree();
        if (tree != null) {
            org.godot.node.PackedScene packedScene = (org.godot.node.PackedScene) org.godot.singleton.ResourceLoader.singleton().load("res://translation_demo_po.tscn");
            if (packedScene != null) {
                tree.changeSceneToPacked(packedScene);
            }
        }
    }
}
