package demos.gui.translation;

import org.godot.annotation.GodotClass;
import org.godot.annotation.GodotMethod;
import org.godot.node.Panel;

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
    public void _on_english_pressed() {
        org.godot.singleton.TranslationServer ts = org.godot.singleton.TranslationServer.singleton();
        ts.call("set_locale", "en");
        printIntro();
    }

    @GodotMethod
    public void _on_spanish_pressed() {
        org.godot.singleton.TranslationServer ts = org.godot.singleton.TranslationServer.singleton();
        ts.call("set_locale", "es");
        printIntro();
    }

    @GodotMethod
    public void _on_japanese_pressed() {
        org.godot.singleton.TranslationServer ts = org.godot.singleton.TranslationServer.singleton();
        ts.call("set_locale", "ja");
        printIntro();
    }

    @GodotMethod
    public void _on_play_audio_pressed() {
        org.godot.Godot audio = (org.godot.Godot) call("get_node", "Audio");
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
    public void _on_go_to_csv_translation_demo_pressed() {
        org.godot.Godot tree = (org.godot.Godot) call("get_tree");
        if (tree != null) {
            Object packedScene = call("load", "res://translation_demo_csv.tscn");
            if (packedScene != null) {
                tree.call("change_scene_to_packed", packedScene);
            }
        }
    }
}
