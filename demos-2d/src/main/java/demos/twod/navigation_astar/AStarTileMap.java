package demos.twod.navigation_astar;

import org.godot.annotation.GodotClass;
import org.godot.annotation.GodotMethod;
import org.godot.math.Vector2;
import org.godot.math.Vector2i;
import org.godot.node.TileMapLayer;

@GodotClass(name = "AStarTileMap", parent = "TileMapLayer")
public class AStarTileMap extends TileMapLayer {

	private org.godot.node.AStarGrid2D astar;
	private boolean initialized = false;

	@Override
	public void _ready() {
		if (initialized) return;
		initialized = true;

		astar = org.godot.node.AStarGrid2D.create();
		if (astar != null) {
			astar.setCellSize(new Vector2(64, 64));
			astar.setOffset(new Vector2(32, 32));
			astar.setDefaultComputeHeuristic(0);
			astar.setDefaultEstimateHeuristic(0);
			astar.setDiagonalMode(1);
			astar.update();

			Vector2i[] usedCells = getUsedCells();
			if (usedCells != null) {
				for (Vector2i cell : usedCells) {
					astar.setPointSolid(cell);
				}
			}
		}
	}

	@GodotMethod
	public Vector2 roundLocalPosition(Vector2 localPosition) {
		Vector2i mapPos = localToMap(localPosition);
		return mapToLocal(mapPos);
	}

	@GodotMethod
	public boolean isPointWalkable(Vector2 localPosition) {
		if (astar == null) return false;
		Vector2i mapPos = localToMap(localPosition);
		if (!astar.isInBoundsv(mapPos)) return false;
		return !astar.isPointSolid(mapPos);
	}

	@GodotMethod
	public void clearPath() {
		queueRedraw();
	}

	@GodotMethod
	public Vector2[] findPath(Vector2 localStart, Vector2 localEnd) {
		if (astar == null) return new Vector2[0];
		Vector2i startMap = localToMap(localStart);
		Vector2i endMap = localToMap(localEnd);
		return toVector2Array(astar.getPointPath(startMap, endMap));
	}

	private static Vector2[] toVector2Array(double[][] path) {
		if (path == null) return new Vector2[0];
		Vector2[] result = new Vector2[path.length];
		for (int i = 0; i < path.length; i++) {
			double[] point = path[i];
			result[i] = point != null && point.length >= 2 ? new Vector2(point[0], point[1]) : Vector2.ZERO;
		}
		return result;
	}
}
