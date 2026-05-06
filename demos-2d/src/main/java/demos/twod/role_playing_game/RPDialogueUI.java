package demos.twod.role_playing_game;

import org.godot.annotation.GodotClass;
import org.godot.annotation.GodotMethod;
import org.godot.node.Control;
import org.godot.node.Node;

@GodotClass(name = "RPDialogueUI", parent = "Control")
public class RPDialogueUI extends Control {

    private org.godot.Godot dialogueNode = null;
    private org.godot.Godot playerPawn = null;
    private boolean initialized = false;

    @Override
    public void _ready() {
        if (initialized) return;
        initialized = true;
        setProperty("visible", false);

        // Connect Button signal
        org.godot.node.Button button = (org.godot.node.Button) getNode("Button");
        if (button != null) {
            button.connect("button_up", new org.godot.core.Callable(this, "_on_Button_button_up"), 0);
        }
    }

    public void showDialogue(org.godot.Godot player, org.godot.Godot dialogue) {
        setProperty("visible", true);

        org.godot.node.Button button = (org.godot.node.Button) getNode("Button");
        if (button != null) button.grabFocus();

        dialogueNode = dialogue;
        playerPawn = player;

        // Connect signals
        dialogue.connect("dialogue_finished", new org.godot.core.Callable(this, "on_dialogue_finished"), 0);
        dialogue.connect("dialogue_finished", new org.godot.core.Callable(this, "hide"), 0);
        player.call("set_active", false);

        dialogue.call("start_dialogue");
        updateDialogueText();
    }

    public void OnButtonButtonUp() {
        if (dialogueNode == null) return;
        dialogueNode.call("next_dialogue");
        updateDialogueText();
    }

    @GodotMethod
    public void onDialogueFinished() {
        if (dialogueNode != null) {
            dialogueNode.call("disconnect", "dialogue_finished", new org.godot.core.Callable(this, "on_dialogue_finished"));
            dialogueNode.call("disconnect", "dialogue_finished", new org.godot.core.Callable(this, "hide"));
        }
        if (playerPawn != null) {
            playerPawn.call("set_active", true);
        }
        setProperty("visible", false);
    }

    private void updateDialogueText() {
        if (dialogueNode == null) return;
        Object nameObj = dialogueNode.getProperty("dialogue_name");
        Object textObj = dialogueNode.getProperty("dialogue_text");

        org.godot.node.Node nameLabel = getNode("Name");
        org.godot.node.Node textLabel = getNode("Text");

        if (nameLabel != null && nameObj != null) {
            nameLabel.setProperty("text", "[center]" + nameObj.toString() + "[/center]");
        }
        if (textLabel != null && textObj != null) {
            textLabel.setProperty("text", textObj.toString());
        }
    }
}
