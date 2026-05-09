package demos.threed.squash_the_creeps;

import org.godot.annotation.GodotClass;
import org.godot.annotation.GodotMethod;
import org.godot.core.Callable;
import org.godot.math.Vector3;
import org.godot.node.Node;
import org.godot.node.SceneTree;

@GodotClass(name = "STCMain", parent = "Node")
public class STCMain extends Node {

	private org.godot.node.Timer mobTimer;
	private org.godot.node.Control retry;
	private org.godot.node.Label scoreLabel;
	private STCPlayer player;
	private boolean initialized = false;

	@Override
	public void _ready() {
		if (initialized) return;
		initialized = true;

		mobTimer = (org.godot.node.Timer) getNode("MobTimer");
		retry = (org.godot.node.Control) getNode("UserInterface/Retry");
		scoreLabel = (org.godot.node.Label) getNode("UserInterface/ScoreLabel");
		player = getNodeAs("Player", STCPlayer.class);

		if (retry != null) retry.hide();

		// Connect mob timer
		if (mobTimer != null) {
			mobTimer.connect("timeout", new Callable(this, "_on_mob_timer_timeout"), 0);
		}

		// Connect player hit signal
		if (player != null) {
			player.connect("hit", new Callable(this, "_on_player_hit"), 0);
		}
	}

	@Override
	public void _exitTree() {
		if (mobTimer != null) mobTimer.stop();
		mobTimer = null;
		retry = null;
		scoreLabel = null;
		player = null;
	}

	@Override
	public boolean _unhandledInput(Object inputEvent) {
		org.godot.node.InputEvent ev = (org.godot.node.InputEvent) inputEvent;
		if ((boolean) ev.isActionPressed("ui_accept")) {
			if (retry != null && (boolean) retry.getProperty("visible")) {
				org.godot.node.SceneTree tree = getTree();
				if (tree != null) tree.reloadCurrentScene();
				return true;
			}
		}
		return false;
	}

	@GodotMethod
	public void OnMobTimerTimeout() {
		// Create mob from scene
		org.godot.node.Node loader = getNode("/root/ResourceLoader");
		// Use load to get mob scene
		org.godot.node.PackedScene mobSceneObj = (org.godot.node.PackedScene) org.godot.singleton.ResourceLoader.singleton().load("res://Mob.tscn");
		if (mobSceneObj == null) return;

		org.godot.node.PackedScene mobScene = (org.godot.node.PackedScene) mobSceneObj;
		if (!(mobScene.instantiate() instanceof STCMob mob)) return;

		// Choose random location on SpawnPath
		org.godot.node.Node spawnLocation = getNode("SpawnPath/SpawnLocation");
		if (spawnLocation != null) {
			spawnLocation.setProperty("progress_ratio", Math.random());
		}

		// Initialize mob with spawn position and player position
		Vector3 spawnPos = spawnLocation != null ? (Vector3) spawnLocation.getProperty("position") : new Vector3(0, 0, 0);
		Vector3 playerPos = player != null ? player.getPosition() : new Vector3(0, 0, 0);
		mob.initialize(spawnPos, playerPos);

		addChild(mob);

		// Connect mob's squashed signal to score label
		mob.connect("squashed", new Callable(this, "_on_mob_squashed"), 0);
	}

	@GodotMethod
	public void OnPlayerHit() {
		if (mobTimer != null) mobTimer.stop();
		if (retry != null) retry.show();
	}

	@GodotMethod
	public void OnMobSquashed() {
		if (scoreLabel != null) {
			long score = (long) scoreLabel.getProperty("text");
			scoreLabel.setProperty("text", String.valueOf(score + 1));
		}
	}
}
