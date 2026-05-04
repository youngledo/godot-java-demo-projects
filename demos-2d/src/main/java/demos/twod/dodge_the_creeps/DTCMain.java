package demos.twod.dodge_the_creeps;

import org.godot.annotation.GodotClass;
import org.godot.annotation.GodotMethod;
import org.godot.core.Callable;
import org.godot.math.Vector2;
import org.godot.node.Node;

@GodotClass(name = "DTCMain", parent = "Node")
public class DTCMain extends Node {

	private int score = 0;
	private org.godot.Godot player;
	private org.godot.Godot scoreTimer;
	private org.godot.Godot mobTimer;
	private org.godot.Godot startTimer;
	private org.godot.Godot hud;
	private org.godot.Godot music;
	private org.godot.Godot deathSound;
	private boolean initialized = false;

	@Override
	public void _ready() {
		if (initialized) return;
		initialized = true;

		player = (org.godot.Godot) call("get_node", "Player");
		scoreTimer = (org.godot.Godot) call("get_node", "ScoreTimer");
		mobTimer = (org.godot.Godot) call("get_node", "MobTimer");
		startTimer = (org.godot.Godot) call("get_node", "StartTimer");
		hud = (org.godot.Godot) call("get_node", "HUD");
		music = (org.godot.Godot) call("get_node", "Music");
		deathSound = (org.godot.Godot) call("get_node", "DeathSound");

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
		org.godot.Godot tree = (org.godot.Godot) call("get_tree");
		if (tree != null) tree.call("call_group", "mobs", "queue_free");
		score = 0;

		org.godot.Godot startPos = (org.godot.Godot) call("get_node", "StartPosition");
		if (player != null && startPos != null) {
			Vector2 pos = (Vector2) startPos.getProperty("position");
			player.call("start", pos);
		}

		if (startTimer != null) startTimer.call("start");
		if (hud != null) {
			hud.call("update_score", score);
			hud.call("show_message", "Get Ready");
		}
		if (music != null) music.call("play");
	}

	@GodotMethod
	public void gameOver() {
		if (scoreTimer != null) scoreTimer.call("stop");
		if (mobTimer != null) mobTimer.call("stop");
		if (hud != null) hud.call("show_game_over");
		if (music != null) music.call("stop");
		if (deathSound != null) deathSound.call("play");
	}

	@GodotMethod
	public void _on_mob_timer_timeout() {
		Object mobSceneObj = call("load", "res://Mob.tscn");
		if (mobSceneObj == null) return;
		org.godot.Godot mob = (org.godot.Godot) ((org.godot.Godot) mobSceneObj).call("instantiate");

		org.godot.Godot spawnLocation = (org.godot.Godot) call("get_node", "MobPath/MobSpawnLocation");
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

		call("add_child", mob);
	}

	@GodotMethod
	public void _on_score_timer_timeout() {
		score++;
		if (hud != null) hud.call("update_score", score);
	}

	@GodotMethod
	public void _on_start_timer_timeout() {
		if (mobTimer != null) mobTimer.call("start");
		if (scoreTimer != null) scoreTimer.call("start");
	}

	@GodotMethod
	public void _on_player_hit() {
		gameOver();
	}
}
