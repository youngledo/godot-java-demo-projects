package demos.twod.role_playing_game;

import org.godot.annotation.GodotClass;
import org.godot.annotation.Signal;
import org.godot.collection.GodotArray;
import org.godot.collection.GodotDictionary;
import org.godot.node.FileAccess;
import org.godot.node.JSON;
import org.godot.node.Node;
import java.util.ArrayList;
import java.util.Map;

@GodotClass(name = "RPDialoguePlayer", parent = "Node")
public class RPDialoguePlayer extends Node {

    private String dialogueFile = "";
    private ArrayList<Object> dialogueKeys = new ArrayList<>();
    private String dialogueName = "";
    private int current = 0;
    private String dialogueText = "";
    private boolean initialized = false;

    @Signal
    public void dialogueStarted() {}

    @Signal
    public void dialogueFinished() {}

    @Override
    public void _ready() {
        if (initialized) return;
        initialized = true;

        Object fileObj = getProperty("dialogue_file");
        if (fileObj instanceof String) dialogueFile = (String) fileObj;
    }

    public void startDialogue() {
        emitSignal("dialogue_started");
        current = 0;
        indexDialogue();
        if (!dialogueKeys.isEmpty()) {
            Object entry = dialogueKeys.get(current);
            if (entry instanceof Map) {
                Map<?, ?> m = (Map<?, ?>) entry;
                dialogueText = m.get("text") != null ? m.get("text").toString() : "";
                dialogueName = m.get("name") != null ? m.get("name").toString() : "";
            } else if (entry instanceof org.godot.Godot) {
                org.godot.Godot g = (org.godot.Godot) entry;
                Object text = g.getProperty("text");
                Object name = g.getProperty("name");
                dialogueText = text != null ? text.toString() : "";
                dialogueName = name != null ? name.toString() : "";
            }
        }
    }

    public void nextDialogue() {
        current++;
        if (current >= dialogueKeys.size()) {
            emitSignal("dialogue_finished");
            return;
        }
        Object entry = dialogueKeys.get(current);
        if (entry instanceof Map) {
            Map<?, ?> m = (Map<?, ?>) entry;
            dialogueText = m.get("text") != null ? m.get("text").toString() : "";
            dialogueName = m.get("name") != null ? m.get("name").toString() : "";
        } else if (entry instanceof org.godot.Godot) {
            org.godot.Godot g = (org.godot.Godot) entry;
            dialogueText = g.getProperty("text") != null ? g.getProperty("text").toString() : "";
            dialogueName = g.getProperty("name") != null ? g.getProperty("name").toString() : "";
        }
    }

    private void indexDialogue() {
        dialogueKeys.clear();
        if (dialogueFile.isEmpty()) return;

        String text = FileAccess.getFileAsString(dialogueFile);
        if (text == null || text.isEmpty()) return;

        Object data = JSON.parseString(text);
        if (data instanceof GodotArray array) {
            for (int i = 0; i < array.size(); i++) {
                Object entry = array.get(i);
                if (entry != null) dialogueKeys.add(entry);
            }
        } else if (data instanceof Object[] entries) {
            for (Object entry : entries) {
                if (entry != null) dialogueKeys.add(entry);
            }
        } else if (data instanceof Map<?, ?> map) {
            dialogueKeys.addAll(map.values());
        } else if (data instanceof GodotDictionary dictionary) {
            addNumericDictionaryEntries(dictionary);
        }
    }

    private void addNumericDictionaryEntries(GodotDictionary dictionary) {
        for (int i = 0; i < 1000; i++) {
            Object entry = dictionary.get(String.valueOf(i));
            if (entry == null) entry = dictionary.get(i);
            if (entry != null) dialogueKeys.add(entry);
        }
    }

    public String getDialogueName() { return dialogueName; }
    public String getDialogueText() { return dialogueText; }
}
