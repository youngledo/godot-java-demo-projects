package demos.threed.squash_the_creeps;

import org.godot.annotation.GodotClass;
import org.godot.annotation.GodotMethod;
import org.godot.core.Callable;
import org.godot.math.Vector3;
import org.godot.node.Node;

@GodotClass(name = "STCMain", parent = "Node")
public class STCMain extends Node {

	private org.godot.Godot mobTimer;
	private org.godot.Godot retry;
	private org.godot.Godot scoreLabel;
	private org.godot.Godot player;
	private boolean initialized = false;

	@Override
	public void _ready() {
		if (initialized) return;
		initialized = true;

		mobTimer = (org.godot.Godot) call("get_node", "MobTimer");
		retry = (org.godot.Godot) call("get_node", "UserInterface/Retry");
		scoreLabel = (org.godot.Godot) call("get_node", "UserInterface/ScoreLabel");
		player = (org.godot.Godot) call("get_node", "Player");

		if (retry != null) retry.call("hide");

		// Connect mob timer
		if (mobTimer != null) {
			mobTimer.connect("timeout", new Callable(this, "_on_mob_timer_timeout"), 0);
		}

		// Connect player hit signal
		if (player != null) {
			player.call("add_user_signal", "hit");
			player.connect("hit", new Callable(this, "_on_player_hit"), 0);
		}
	}

	@Override
	public void _exitTree() {
		if (mobTimer != null) mobTimer.call("stop");
		mobTimer = null;
		retry = null;
		scoreLabel = null;
		player = null;
	}

	@Override
	public boolean _unhandledInput(Object inputEvent) {
		org.godot.Godot ev = (org.godot.Godot) inputEvent;
		if ((boolean) ev.call("is_action_pressed", "ui_accept")) {
			if (retry != null && (boolean) retry.getProperty("visible")) {
				org.godot.Godot tree = (org.godot.Godot) call("get_tree");
				if (tree != null) tree.call("reload_current_scene");
				return true;
			}
		}
		return false;
	}

	@GodotMethod
	public void _on_mob_timer_timeout() {
		// Create mob from scene
		org.godot.Godot loader = (org.godot.Godot) call("get_node", "/root/ResourceLoader");
		// Use load to get mob scene
		Object mobSceneObj = call("load", "res://Mob.tscn");
		if (mobSceneObj == null) return;

		org.godot.Godot mobScene = (org.godot.Godot) mobSceneObj;
		org.godot.Godot mob = (org.godot.Godot) mobScene.call("instantiate");

		// Choose random location on SpawnPath
		org.godot.Godot spawnLocation = (org.godot.Godot) call("get_node", "SpawnPath/SpawnLocation");
		if (spawnLocation != null) {
			spawnLocation.setProperty("progress_ratio", Math.random());
		}

		// Initialize mob with spawn position and player position
		Vector3 spawnPos = spawnLocation != null ? (Vector3) spawnLocation.getProperty("position") : new Vector3(0, 0, 0);
		Vector3 playerPos = player != null ? (Vector3) player.getProperty("position") : new Vector3(0, 0, 0);
		mob.call("initialize", spawnPos, playerPos);

		call("add_child", mob);

		// Connect mob's squashed signal to score label
		mob.call("add_user_signal", "squashed");
		mob.connect("squashed", new Callable(this, "_on_mob_squashed"), 0);
	}

	@GodotMethod
	public void _on_player_hit() {
		if (mobTimer != null) mobTimer.call("stop");
		if (retry != null) retry.call("show");
	}

	@GodotMethod
	public void _on_mob_squashed() {
		if (scoreLabel != null) {
			long score = (long) scoreLabel.getProperty("text");
			scoreLabel.setProperty("text", String.valueOf(score + 1));
		}
	}
}
