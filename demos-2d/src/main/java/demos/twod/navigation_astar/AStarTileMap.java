package demos.twod.navigation_astar;

import org.godot.annotation.GodotClass;
import org.godot.annotation.GodotMethod;
import org.godot.math.Vector2;
import org.godot.node.TileMapLayer;

@GodotClass(name = "AStarTileMap", parent = "TileMapLayer")
public class AStarTileMap extends TileMapLayer {

	private org.godot.Godot astar;
	private boolean initialized = false;

	@Override
	public void _ready() {
		if (initialized) return;
		initialized = true;

		// Create AStarGrid2D
		Object astarObj = call("create_astar_grid");
		if (astarObj != null) {
			astar = (org.godot.Godot) astarObj;
		}
		if (astar != null) {
			astar.setProperty("cell_size", new Vector2(64, 64));
			astar.setProperty("offset", new Vector2(32, 32));
			astar.setProperty("default_compute_heuristic", 0); // MANHATTAN
			astar.setProperty("default_estimate_heuristic", 0);
			astar.setProperty("diagonal_mode", 1); // NEVER
			astar.call("update");

			// Mark used cells as solid
			Object[] usedCells = (Object[]) call("get_used_cells");
			if (usedCells != null) {
				for (Object cell : usedCells) {
					astar.call("set_point_solid", cell);
				}
			}
		}
	}

	@GodotMethod
	public Vector2 roundLocalPosition(Vector2 localPosition) {
		Object mapPos = call("local_to_map", localPosition);
		Object result = call("map_to_local", mapPos);
		return (Vector2) result;
	}

	@GodotMethod
	public boolean isPointWalkable(Vector2 localPosition) {
		if (astar == null) return false;
		Object mapPos = call("local_to_map", localPosition);
		boolean inBounds = (boolean) astar.call("is_in_boundsv", mapPos);
		if (!inBounds) return false;
		return !(boolean) astar.call("is_point_solid", mapPos);
	}

	@GodotMethod
	public void clearPath() {
		call("queue_redraw");
	}

	@GodotMethod
	public Vector2[] findPath(Vector2 localStart, Vector2 localEnd) {
		if (astar == null) return new Vector2[0];
		Object startMap = call("local_to_map", localStart);
		Object endMap = call("local_to_map", localEnd);
		Object pathObj = astar.call("get_point_path", startMap, endMap);
		if (pathObj instanceof Vector2[]) return (Vector2[]) pathObj;
		return new Vector2[0];
	}
}
