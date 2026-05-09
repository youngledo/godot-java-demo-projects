package demos.twod.navigation_astar;

import org.godot.annotation.GodotClass;
import org.godot.annotation.Export;
import org.godot.math.Vector2;
import org.godot.node.Node2D;
import org.godot.node.Node;
import org.godot.node.Viewport;

@GodotClass(name = "AStarCharacter", parent = "Node2D")
public class AStarCharacter extends Node2D {

	private static final double MASS = 10.0;
	private static final double ARRIVE_DISTANCE = 10.0;

	@Export
	public double speed = 200.0;

	private int state = 0; // 0=IDLE, 1=FOLLOW
	private Vector2 velocity = new Vector2(0, 0);
	private Vector2 clickPosition = new Vector2(0, 0);
	private AStarTileMap tileMap;
	private java.util.List<Vector2> path = new java.util.ArrayList<>();
	private Vector2 nextPoint = new Vector2(0, 0);
	private boolean initialized = false;

	@Override
	public void _ready() {
		if (initialized) return;
		initialized = true;

		tileMap = getNodeAs("../TileMapLayer", AStarTileMap.class);
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
		org.godot.node.InputEvent ev = (org.godot.node.InputEvent) inputEvent;

		org.godot.node.Viewport viewport = getViewport();
		if (viewport == null) return false;
		clickPosition = (Vector2) viewport.getMousePosition();

		if (tileMap != null && tileMap.isPointWalkable(clickPosition)) {
			if ((boolean) ev.isActionPressed("teleport_to")) {
				changeState(0);
				Vector2 rounded = tileMap.roundLocalPosition(clickPosition);
				if (rounded != null) {
					setGlobalPosition(rounded);
					resetPhysicsInterpolation();
				}
				return true;
			} else if ((boolean) ev.isActionPressed("move_to")) {
				changeState(1);
				return true;
			}
		}
		return false;
	}

	private boolean moveTo(Vector2 localPosition) {
		Vector2 pos = getPosition();
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

		Vector2 newPos = new Vector2(pos.getX() + velocity.getX(), pos.getY() + velocity.getY());
		setPosition(newPos);
		setRotation(Math.atan2(velocity.getY(), velocity.getX()));

		double dist = Math.sqrt(
			(localPosition.getX() - newPos.getX()) *
			(localPosition.getX() - newPos.getX()) +
			(localPosition.getY() - newPos.getY()) *
			(localPosition.getY() - newPos.getY())
		);
		return dist < ARRIVE_DISTANCE;
	}

	private void changeState(int newState) {
		if (newState == 0 && tileMap != null) {
			tileMap.clearPath();
		} else if (newState == 1 && tileMap != null) {
			Vector2[] pathResult = tileMap.findPath(getPosition(), clickPosition);
			path.clear();
			for (Vector2 v : pathResult) path.add(v);
			if (path.size() < 2) {
				changeState(0);
				return;
			}
			nextPoint = path.get(1);
		}
		state = newState;
	}
}
