package demos.gui.translation;

import org.godot.annotation.GodotClass;
import org.godot.annotation.GodotMethod;
import org.godot.node.Panel;

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
    public void _on_russian_pressed() {
        org.godot.singleton.TranslationServer ts = org.godot.singleton.TranslationServer.singleton();
        ts.call("set_locale", "ru");
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
        System.out.println(call("tr", "KEY_INTRO"));
    }

    @GodotMethod
    public void _on_go_to_po_translation_demo_pressed() {
        org.godot.Godot tree = (org.godot.Godot) call("get_tree");
        if (tree != null) {
            Object packedScene = call("load", "res://translation_demo_po.tscn");
            if (packedScene != null) {
                tree.call("change_scene_to_packed", packedScene);
            }
        }
    }
}
