package demos.twod.navigation_astar;

import org.godot.annotation.GodotClass;
import org.godot.annotation.Export;
import org.godot.math.Vector2;
import org.godot.node.Node2D;

@GodotClass(name = "AStarCharacter", parent = "Node2D")
public class AStarCharacter extends Node2D {

	private static final double MASS = 10.0;
	private static final double ARRIVE_DISTANCE = 10.0;

	@Export
	public double speed = 200.0;

	private int state = 0; // 0=IDLE, 1=FOLLOW
	private Vector2 velocity = new Vector2(0, 0);
	private Vector2 clickPosition = new Vector2(0, 0);
	private org.godot.Godot tileMap;
	private java.util.List<Vector2> path = new java.util.ArrayList<>();
	private Vector2 nextPoint = new Vector2(0, 0);
	private boolean initialized = false;

	@Override
	public void _ready() {
		if (initialized) return;
		initialized = true;

		tileMap = (org.godot.Godot) call("get_node", "../TileMapLayer");
		changeState(0);
	}

	@Override
	public void _physicsProcess(double delta) {
		if (state != 1) return;

		boolean arrived = moveTo(nextPoint);
		if (arrived && !path.isEmpty()) {
			path.remove(0);
			if (path.isEmpty()) {
				changeState(0);
				return;
			}
			nextPoint = path.get(0);
		}
	}

	@Override
	public boolean _unhandledInput(Object inputEvent) {
		org.godot.Godot ev = (org.godot.Godot) inputEvent;

		org.godot.Godot viewport = (org.godot.Godot) call("get_viewport");
		if (viewport == null) return false;
		clickPosition = (Vector2) viewport.call("get_mouse_position");

		if (tileMap != null && (boolean) tileMap.call("is_point_walkable", clickPosition)) {
			if ((boolean) ev.call("is_action_pressed", "teleport_to")) {
				changeState(0);
				Vector2 rounded = (Vector2) tileMap.call("round_local_position", clickPosition);
				if (rounded != null) {
					setProperty("global_position", rounded);
					call("reset_physics_interpolation");
				}
				return true;
			} else if ((boolean) ev.call("is_action_pressed", "move_to")) {
				changeState(1);
				return true;
			}
		}
		return false;
	}

	private boolean moveTo(Vector2 localPosition) {
		Vector2 pos = (Vector2) getProperty("position");
		if (pos == null) return true;

		Vector2 desired = new Vector2(
			(localPosition.getX() - pos.getX()),
			(localPosition.getY() - pos.getY())
		);
		double desiredLen = Math.sqrt(desired.getX() * desired.getX() + desired.getY() * desired.getY());
		if (desiredLen > 0) {
			desired = new Vector2(desired.getX() / desiredLen * speed, desired.getY() / desiredLen * speed);
		}

		Vector2 steering = new Vector2(desired.getX() - velocity.getX(), desired.getY() - velocity.getY());
		velocity = new Vector2(velocity.getX() + steering.getX() / MASS, velocity.getY() + steering.getY() / MASS);

		setProperty("position", new Vector2(pos.getX() + velocity.getX(), pos.getY() + velocity.getY()));
		setProperty("rotation", Math.atan2(velocity.getY(), velocity.getX()));

		double dist = Math.sqrt(
			(localPosition.getX() - ((Vector2) getProperty("position")).getX()) *
			(localPosition.getX() - ((Vector2) getProperty("position")).getX()) +
			(localPosition.getY() - ((Vector2) getProperty("position")).getY()) *
			(localPosition.getY() - ((Vector2) getProperty("position")).getY())
		);
		return dist < ARRIVE_DISTANCE;
	}

	private void changeState(int newState) {
		if (newState == 0 && tileMap != null) {
			tileMap.call("clear_path");
		} else if (newState == 1 && tileMap != null) {
			Vector2 pos = (Vector2) getProperty("position");
			Object pathResult = tileMap.call("find_path", pos, clickPosition);
			if (pathResult instanceof Vector2[]) {
				Vector2[] arr = (Vector2[]) pathResult;
				path.clear();
				for (Vector2 v : arr) path.add(v);
			} else if (pathResult instanceof java.util.List) {
				path = (java.util.List<Vector2>) pathResult;
			}
			if (path.size() < 2) {
				changeState(0);
				return;
			}
			nextPoint = path.get(1);
		}
		state = newState;
	}
}
