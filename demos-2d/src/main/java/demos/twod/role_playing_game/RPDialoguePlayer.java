package demos.twod.role_playing_game;

import org.godot.annotation.GodotClass;
import org.godot.annotation.Signal;
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
    public void dialogue_started() {}

    @Signal
    public void dialogue_finished() {}

    @Override
    public void _ready() {
        if (initialized) return;
        initialized = true;

        Object fileObj = getProperty("dialogue_file");
        if (fileObj instanceof String) dialogueFile = (String) fileObj;
    }

    public void start_dialogue() {
        call("emit_signal", "dialogue_started");
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

    public void next_dialogue() {
        current++;
        if (current >= dialogueKeys.size()) {
            call("emit_signal", "dialogue_finished");
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

        Object file = call("call", "FileAccess.open", dialogueFile, 1); // READ = 1
        if (file == null) return;

        Object textObj = ((org.godot.Godot) file).call("get_as_text");
        if (textObj == null) return;

        Object json = call("call", "JSON.new");
        if (json instanceof org.godot.Godot) {
            ((org.godot.Godot) json).call("parse", textObj);
            Object data = ((org.godot.Godot) json).getProperty("data");
            if (data instanceof org.godot.Godot) {
                // Iterate dictionary keys
                Object keys = ((org.godot.Godot) data).call("keys");
                if (keys instanceof org.godot.Godot[]) {
                    for (org.godot.Godot key : (org.godot.Godot[]) keys) {
                        Object val = ((org.godot.Godot) data).call("get", key);
                        if (val != null) dialogueKeys.add(val);
                    }
                }
            }
        }
    }

    public String getDialogueName() { return dialogueName; }
    public String getDialogueText() { return dialogueText; }
}
