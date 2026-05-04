package demos.twod.role_playing_game;

import org.godot.annotation.GodotClass;
import org.godot.annotation.GodotMethod;
import org.godot.node.Node;

@GodotClass(name = "RPGame", parent = "Node")
public class RPGame extends Node {

    private static final String PLAYER_WIN = "res://dialogue/dialogue_data/player_won.json";
    private static final String PLAYER_LOSE = "res://dialogue/dialogue_data/player_lose.json";

    private org.godot.Godot combatScreen;
    private org.godot.Godot explorationScreen;
    private boolean initialized = false;

    @Override
    public void _ready() {
        if (initialized) return;
        initialized = true;

        combatScreen = (org.godot.Godot) getProperty("combat_screen");
        explorationScreen = (org.godot.Godot) getProperty("exploration_screen");

        if (combatScreen != null) {
            combatScreen.call("connect", "combat_finished", new org.godot.core.Callable(this, "on_combat_finished"));
        }

        // Connect opponent dialogue_finished signals
        org.godot.Godot grid = (org.godot.Godot) call("get_node", "Exploration/Grid");
        if (grid != null) {
            Object children = grid.call("get_children");
            if (children instanceof org.godot.Godot[]) {
                for (org.godot.Godot n : (org.godot.Godot[]) children) {
                    Object typeObj = n.getProperty("type");
                    int type = typeObj instanceof Number ? ((Number) typeObj).intValue() : 0;
                    if (type != RPPawn.CELL_TYPE_ACTOR) continue;
                    boolean hasDialogue = (boolean) n.call("has_node", "DialoguePlayer");
                    if (!hasDialogue) continue;
                    org.godot.Godot dialoguePlayer = (org.godot.Godot) n.call("get_node", "DialoguePlayer");
                    if (dialoguePlayer != null) {
                        dialoguePlayer.call("connect", "dialogue_finished", new org.godot.core.Callable(this, "on_opponent_dialogue_finished"));
                        // Store opponent reference
                        dialoguePlayer.setProperty("_opponent_ref", n);
                    }
                }
            }
        }

        if (combatScreen != null) {
            call("remove_child", combatScreen);
        }
    }

    @GodotMethod
    public void on_opponent_dialogue_finished() {
        // Find the opponent that triggered this
        org.godot.Godot grid = (org.godot.Godot) call("get_node", "Exploration/Grid");
        if (grid == null) return;

        Object children = grid.call("get_children");
        if (!(children instanceof org.godot.Godot[])) return;

        for (org.godot.Godot n : (org.godot.Godot[]) children) {
            Object lostObj = n.getProperty("lost");
            boolean lost = lostObj instanceof Boolean && (Boolean) lostObj;
            if (lost) continue;

            Object typeObj = n.getProperty("type");
            int type = typeObj instanceof Number ? ((Number) typeObj).intValue() : 0;
            if (type != RPPawn.CELL_TYPE_ACTOR) continue;

            boolean hasDialogue = (boolean) n.call("has_node", "DialoguePlayer");
            if (!hasDialogue) continue;
            org.godot.Godot dp = (org.godot.Godot) n.call("get_node", "DialoguePlayer");
            if (dp == null) continue;

            // Check if this was the one that finished (compare with signal source)
            // Simplified: trigger combat for any active opponent
            org.godot.Godot player = (org.godot.Godot) call("get_node", "Exploration/Grid/Player");
            if (player == null) continue;

            Object playerCombatActor = player.getProperty("combat_actor");
            Object opponentCombatActor = n.getProperty("combat_actor");

            startCombat(playerCombatActor, opponentCombatActor);
            break;
        }
    }

    private void startCombat(Object playerActor, Object opponentActor) {
        org.godot.Godot animPlayer = (org.godot.Godot) call("get_node", "AnimationPlayer");
        if (animPlayer != null) {
            animPlayer.call("play", "fade_to_black");
            animPlayer.call("connect", "animation_finished", new org.godot.core.Callable(this, "on_fade_out_complete"));
        }
        setProperty("_pending_combat_actors", new Object[]{playerActor, opponentActor});
    }

    @GodotMethod
    public void on_fade_out_complete(String animName) {
        org.godot.Godot animPlayer = (org.godot.Godot) call("get_node", "AnimationPlayer");
        if (animPlayer != null) {
            animPlayer.call("disconnect", "animation_finished", new org.godot.core.Callable(this, "on_fade_out_complete"));
        }

        org.godot.Godot exploration = (org.godot.Godot) call("get_node", "Exploration");
        if (exploration != null) call("remove_child", exploration);
        if (combatScreen != null) {
            call("add_child", combatScreen);
            combatScreen.call("show");
            Object actorsObj = getProperty("_pending_combat_actors");
            if (actorsObj instanceof Object[]) {
                combatScreen.call("initialize", (Object[]) actorsObj);
            }
        }
        if (animPlayer != null) animPlayer.call("play_backwards", "fade_to_black");
    }

    @GodotMethod
    public void on_combat_finished(org.godot.Godot winner, org.godot.Godot loser) {
        if (combatScreen != null) call("remove_child", combatScreen);

        org.godot.Godot animPlayer = (org.godot.Godot) call("get_node", "AnimationPlayer");
        if (animPlayer != null) animPlayer.call("play_backwards", "fade_to_black");

        if (explorationScreen != null) call("add_child", explorationScreen);

        // Create dialogue player for win/lose
        Object dialogueSceneObj = call("load", "res://dialogue/dialogue_player/dialogue_player.tscn");
        if (dialogueSceneObj == null) return;

        org.godot.Godot dialogue = (org.godot.Godot) ((org.godot.Godot) dialogueSceneObj).call("instantiate");
        if (dialogue == null) return;

        Object winnerNameObj = winner.getProperty("name");
        String winnerName = winnerNameObj != null ? winnerNameObj.toString() : "";

        String dialogueFile = winnerName.equals("Player") ? PLAYER_WIN : PLAYER_LOSE;
        dialogue.setProperty("dialogue_file", dialogueFile);

        // Connect animation finished to show dialogue
        if (animPlayer != null) {
            setProperty("_pending_dialogue", dialogue);
            animPlayer.call("connect", "animation_finished", new org.godot.core.Callable(this, "on_fade_in_for_dialogue"));
        }
    }

    @GodotMethod
    public void on_fade_in_for_dialogue(String animName) {
        org.godot.Godot animPlayer = (org.godot.Godot) call("get_node", "AnimationPlayer");
        if (animPlayer != null) {
            animPlayer.call("disconnect", "animation_finished", new org.godot.core.Callable(this, "on_fade_in_for_dialogue"));
        }

        Object dialogueObj = getProperty("_pending_dialogue");
        if (!(dialogueObj instanceof org.godot.Godot)) return;
        org.godot.Godot dialogue = (org.godot.Godot) dialogueObj;

        org.godot.Godot player = (org.godot.Godot) call("get_node", "Exploration/Grid/Player");
        org.godot.Godot dialogueUI = null;
        if (explorationScreen != null) {
            dialogueUI = (org.godot.Godot) explorationScreen.call("get_node", "DialogueCanvas/DialogueUI");
        }

        if (dialogueUI != null && player != null) {
            dialogueUI.call("show_dialogue", player, dialogue);
        }

        if (combatScreen != null) combatScreen.call("clear_combat");

        dialogue.call("connect", "dialogue_finished", new org.godot.core.Callable(this, "on_result_dialogue_finished"));
    }

    @GodotMethod
    public void on_result_dialogue_finished() {
        Object dialogueObj = getProperty("_pending_dialogue");
        if (dialogueObj instanceof org.godot.Godot) {
            ((org.godot.Godot) dialogueObj).call("queue_free");
        }
        setProperty("_pending_dialogue", null);
    }
}
