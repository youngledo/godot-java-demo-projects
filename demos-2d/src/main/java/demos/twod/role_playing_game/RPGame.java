package demos.twod.role_playing_game;

import org.godot.annotation.GodotClass;
import org.godot.annotation.GodotMethod;
import org.godot.node.Node;

@GodotClass(name = "RPGame", parent = "Node")
public class RPGame extends Node {

    private static final String PLAYER_WIN = "res://dialogue/dialogue_data/player_won.json";
    private static final String PLAYER_LOSE = "res://dialogue/dialogue_data/player_lose.json";

    private RPCombat combatScreen;
    private org.godot.node.Node explorationScreen;
    private boolean initialized = false;

    @Override
    public void _ready() {
        if (initialized) return;
        initialized = true;

        combatScreen = (RPCombat) getProperty("combat_screen");
        explorationScreen = (org.godot.node.Node) getProperty("exploration_screen");

        if (combatScreen != null) {
            combatScreen.connect("combat_finished", new org.godot.core.Callable(this, "on_combat_finished"), 0);
        }

        // Connect opponent dialogue_finished signals
        org.godot.node.Node grid = getNode("Exploration/Grid");
        if (grid != null) {
            Object children = grid.getChildren();
            if (children instanceof org.godot.Godot[]) {
                for (org.godot.Godot n : (org.godot.Godot[]) children) {
                    Object typeObj = n.getProperty("type");
                    int type = typeObj instanceof Number ? ((Number) typeObj).intValue() : 0;
                    if (type != RPPawn.CELL_TYPE_ACTOR) continue;
                    boolean hasDialogue = (boolean) ((org.godot.node.Node) n).hasNode("DialoguePlayer");
                    if (!hasDialogue) continue;
                    org.godot.node.Node dialoguePlayer = (org.godot.node.Node) ((org.godot.node.Node) n).getNode("DialoguePlayer");
                    if (dialoguePlayer != null) {
                        dialoguePlayer.connect("dialogue_finished", new org.godot.core.Callable(this, "on_opponent_dialogue_finished"), 0);
                        // Store opponent reference
                        dialoguePlayer.setProperty("_opponent_ref", n);
                    }
                }
            }
        }

        if (combatScreen != null) {
            removeChild(combatScreen);
        }
    }

    @GodotMethod
    public void onOpponentDialogueFinished() {
        // Find the opponent that triggered this
        org.godot.node.Node grid = getNode("Exploration/Grid");
        if (grid == null) return;

        Object children = grid.getChildren();
        if (!(children instanceof org.godot.Godot[])) return;

        for (org.godot.Godot n : (org.godot.Godot[]) children) {
            Object lostObj = n.getProperty("lost");
            boolean lost = lostObj instanceof Boolean && (Boolean) lostObj;
            if (lost) continue;

            Object typeObj = n.getProperty("type");
            int type = typeObj instanceof Number ? ((Number) typeObj).intValue() : 0;
            if (type != RPPawn.CELL_TYPE_ACTOR) continue;

            boolean hasDialogue = (boolean) ((org.godot.node.Node) n).hasNode("DialoguePlayer");
            if (!hasDialogue) continue;
            org.godot.node.Node dp = (org.godot.node.Node) ((org.godot.node.Node) n).getNode("DialoguePlayer");
            if (dp == null) continue;

            // Check if this was the one that finished (compare with signal source)
            // Simplified: trigger combat for any active opponent
            org.godot.node.Node player = getNode("Exploration/Grid/Player");
            if (player == null) continue;

            Object playerCombatActor = player.getProperty("combat_actor");
            Object opponentCombatActor = n.getProperty("combat_actor");

            startCombat(playerCombatActor, opponentCombatActor);
            break;
        }
    }

    private void startCombat(Object playerActor, Object opponentActor) {
        org.godot.node.AnimationPlayer animPlayer = (org.godot.node.AnimationPlayer) getNode("AnimationPlayer");
        if (animPlayer != null) {
            animPlayer.play("fade_to_black");
            animPlayer.connect("animation_finished", new org.godot.core.Callable(this, "on_fade_out_complete"), 0);
        }
        setProperty("_pending_combat_actors", new Object[]{playerActor, opponentActor});
    }

    @GodotMethod
    public void onFadeOutComplete(String animName) {
        org.godot.node.AnimationPlayer animPlayer = (org.godot.node.AnimationPlayer) getNode("AnimationPlayer");
        if (animPlayer != null) {
            animPlayer.disconnect("animation_finished", new org.godot.core.Callable(this, "on_fade_out_complete"));
        }

        org.godot.node.Node exploration = getNode("Exploration");
        if (exploration != null) removeChild(exploration);
        if (combatScreen != null) {
            addChild(combatScreen);
            combatScreen.call("show");
            Object actorsObj = getProperty("_pending_combat_actors");
            if (actorsObj instanceof Object[]) {
                combatScreen.initialize((Object[]) actorsObj);
            }
        }
        if (animPlayer != null) animPlayer.playBackwards("fade_to_black");
    }

    @GodotMethod
    public void onCombatFinished(org.godot.Godot winner, org.godot.Godot loser) {
        if (combatScreen != null) removeChild(combatScreen);

        org.godot.node.AnimationPlayer animPlayer = (org.godot.node.AnimationPlayer) getNode("AnimationPlayer");
        if (animPlayer != null) animPlayer.playBackwards("fade_to_black");

        if (explorationScreen != null) addChild((org.godot.node.Node) explorationScreen);

        // Create dialogue player for win/lose
        org.godot.node.PackedScene dialogueSceneObj = (org.godot.node.PackedScene) org.godot.singleton.ResourceLoader.singleton().load("res://dialogue/dialogue_player/dialogue_player.tscn");
        if (dialogueSceneObj == null) return;

        org.godot.Godot dialogue = dialogueSceneObj.instantiate();
        if (dialogue == null) return;

        Object winnerNameObj = winner.getProperty("name");
        String winnerName = winnerNameObj != null ? winnerNameObj.toString() : "";

        String dialogueFile = winnerName.equals("Player") ? PLAYER_WIN : PLAYER_LOSE;
        dialogue.setProperty("dialogue_file", dialogueFile);

        // Connect animation finished to show dialogue
        if (animPlayer != null) {
            setProperty("_pending_dialogue", dialogue);
            animPlayer.connect("animation_finished", new org.godot.core.Callable(this, "on_fade_in_for_dialogue"), 0);
        }
    }

    @GodotMethod
    public void onFadeInForDialogue(String animName) {
        org.godot.node.AnimationPlayer animPlayer = (org.godot.node.AnimationPlayer) getNode("AnimationPlayer");
        if (animPlayer != null) {
            animPlayer.disconnect("animation_finished", new org.godot.core.Callable(this, "on_fade_in_for_dialogue"));
        }

        Object dialogueObj = getProperty("_pending_dialogue");
        if (!(dialogueObj instanceof org.godot.Godot)) return;
        org.godot.Godot dialogue = (org.godot.Godot) dialogueObj;

        org.godot.node.Node player = getNode("Exploration/Grid/Player");
        RPDialogueUI dialogueUI = null;
        if (explorationScreen != null) {
            dialogueUI = explorationScreen.getNodeAs("DialogueCanvas/DialogueUI", RPDialogueUI.class);
        }

        if (dialogueUI != null && player != null) {
            dialogueUI.showDialogue(player, dialogue);
        }

        if (combatScreen != null) combatScreen.clearCombat();

        dialogue.connect("dialogue_finished", new org.godot.core.Callable(this, "on_result_dialogue_finished"), 0);
    }

    @GodotMethod
    public void onResultDialogueFinished() {
        Object dialogueObj = getProperty("_pending_dialogue");
        if (dialogueObj instanceof org.godot.Godot) {
            ((org.godot.Godot) dialogueObj).call("queue_free");
        }
        setProperty("_pending_dialogue", null);
    }
}
