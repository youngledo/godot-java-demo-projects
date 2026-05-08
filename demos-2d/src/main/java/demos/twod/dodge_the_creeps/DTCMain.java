package demos.twod.dodge_the_creeps;

import org.godot.annotation.GodotClass;
import org.godot.annotation.GodotMethod;
import org.godot.core.Callable;
import org.godot.math.Vector2;
import org.godot.node.Node;
import org.godot.node.SceneTree;

@GodotClass(name = "DTCMain", parent = "Node")
public class DTCMain extends Node {

	private int score = 0;
	private demos.twod.dodge_the_creeps.DTCPlayer player;
	private org.godot.node.Timer scoreTimer;
	private org.godot.node.Timer mobTimer;
	private org.godot.node.Timer startTimer;
	private demos.twod.dodge_the_creeps.DTCHUD hud;
	private org.godot.node.AudioStreamPlayer music;
	private org.godot.node.AudioStreamPlayer deathSound;
	private boolean initialized = false;

	@Override
	public void _ready() {
		if (initialized) return;
		initialized = true;

		player = (demos.twod.dodge_the_creeps.DTCPlayer) getNode("Player");
		scoreTimer = (org.godot.node.Timer) getNode("ScoreTimer");
		mobTimer = (org.godot.node.Timer) getNode("MobTimer");
		startTimer = (org.godot.node.Timer) getNode("StartTimer");
		hud = (demos.twod.dodge_the_creeps.DTCHUD) getNode("HUD");
		music = (org.godot.node.AudioStreamPlayer) getNode("Music");
		deathSound = (org.godot.node.AudioStreamPlayer) getNode("DeathSound");

		// Connect signals programmatically
		if (mobTimer != null) mobTimer.connect("timeout", new Callable(this, "_on_mob_timer_timeout"), 0);
		if (scoreTimer != null) scoreTimer.connect("timeout", new Callable(this, "_on_score_timer_timeout"), 0);
		if (startTimer != null) startTimer.connect("timeout", new Callable(this, "_on_start_timer_timeout"), 0);
		if (player != null) {
			player.call("add_user_signal", "hit");
			player.connect("hit", new Callable(this, "_on_player_hit"), 0);
		}
		if (hud != null) {
			hud.call("add_user_signal", "start_game");
			hud.connect("start_game", new Callable(this, "new_game"), 0);
		}
	}

	@GodotMethod
	public void newGame() {
		org.godot.node.SceneTree tree = getTree();
		if (tree != null) tree.callGroup("mobs", "queue_free");
		score = 0;

		org.godot.node.Node startPos = getNode("StartPosition");
		if (player != null && startPos != null) {
			Vector2 pos = (Vector2) startPos.getProperty("position");
			player.start(pos);
		}

		if (startTimer != null) startTimer.start();
		if (hud != null) {
			hud.updateScore(score);
			hud.showMessage("Get Ready");
		}
		if (music != null) music.play();
	}

	@GodotMethod
	public void gameOver() {
		if (scoreTimer != null) scoreTimer.stop();
		if (mobTimer != null) mobTimer.stop();
		if (hud != null) hud.showGameOver();
		if (music != null) music.stop();
		if (deathSound != null) deathSound.play();
	}

	@GodotMethod
	public void OnMobTimerTimeout() {
		org.godot.node.PackedScene mobSceneObj = (org.godot.node.PackedScene) org.godot.singleton.ResourceLoader.singleton().load("res://Mob.tscn");
		if (mobSceneObj == null) return;
		org.godot.Godot mob = mobSceneObj.instantiate();

		org.godot.node.Node spawnLocation = getNode("MobPath/MobSpawnLocation");
		if (spawnLocation != null) {
			spawnLocation.setProperty("progress_ratio", Math.random());
			Vector2 pos = (Vector2) spawnLocation.getProperty("position");
			mob.setProperty("position", pos);

			double direction = (double) spawnLocation.getProperty("rotation") + Math.PI / 2;
			direction += (Math.random() - 0.5) * Math.PI / 2;
			mob.setProperty("rotation", direction);

			double speed = 150.0 + Math.random() * 100.0;
			Vector2 vel = new Vector2(speed, 0);
			// Rotate velocity
			double cosD = Math.cos(direction);
			double sinD = Math.sin(direction);
			mob.setProperty("linear_velocity", new Vector2(vel.getX() * cosD - vel.getY() * sinD, vel.getX() * sinD + vel.getY() * cosD));
		}

		addChild((org.godot.node.Node) mob);
	}

	@GodotMethod
	public void OnScoreTimerTimeout() {
		score++;
		if (hud != null) hud.updateScore(score);
	}

	@GodotMethod
	public void OnStartTimerTimeout() {
		if (mobTimer != null) mobTimer.start();
		if (scoreTimer != null) scoreTimer.start();
	}

	@GodotMethod
	public void OnPlayerHit() {
		gameOver();
	}
}
