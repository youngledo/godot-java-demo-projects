package demos.threed.navigation;

import org.godot.annotation.GodotClass;
import org.godot.annotation.Export;
import org.godot.annotation.GodotMethod;
import org.godot.math.Vector3;
import org.godot.node.Marker3D;

@GodotClass(name = "NavRobot", parent = "Marker3D")
public class NavRobot extends Marker3D {

	@Export
	public double characterSpeed = 10.0;
	@Export
	public boolean showPath = true;

	private org.godot.Godot navAgent;
	private org.godot.Godot navPathLine;
	private boolean initialized = false;

	@Override
	public void _ready() {
		if (initialized) return;
		initialized = true;

		navAgent = (org.godot.Godot) call("get_node", "NavigationAgent3D");

		// Create a Line3D equivalent - we'll use a simple approach
		// The original creates a Line3D (MeshInstance3D subclass) programmatically
	}

	@Override
	public void _physicsProcess(double delta) {
		if (navAgent == null) return;

		boolean finished = (boolean) navAgent.call("is_navigation_finished");
		if (finished) return;

		Vector3 nextPosition = (Vector3) navAgent.call("get_next_path_position");
		Vector3 globalPos = (Vector3) call("get_global_position");

		if (nextPosition == null || globalPos == null) return;

		Vector3 offset = new Vector3(
			nextPosition.getX() - globalPos.getX(),
			nextPosition.getY() - globalPos.getY(),
			nextPosition.getZ() - globalPos.getZ()
		);

		double dist = Math.sqrt(offset.getX() * offset.getX() + offset.getY() * offset.getY() + offset.getZ() * offset.getZ());
		double speed = delta * characterSpeed;

		if (dist <= speed) {
			call("set_global_position", nextPosition);
		} else {
			double ratio = speed / dist;
			call("set_global_position", new Vector3(
				globalPos.getX() + offset.getX() * ratio,
				globalPos.getY() + offset.getY() * ratio,
				globalPos.getZ() + offset.getZ() * ratio
			));
		}

		// Look at direction (clamp Y to 0)
		Vector3 flatOffset = new Vector3(offset.getX(), 0, offset.getZ());
		double flatLen = Math.sqrt(flatOffset.getX() * flatOffset.getX() + flatOffset.getZ() * flatOffset.getZ());
		if (flatLen > 0.001) {
			Vector3 globalPos2 = (Vector3) call("get_global_position");
			call("look_at", new Vector3(globalPos2.getX() + flatOffset.getX(), globalPos2.getY(), globalPos2.getZ() + flatOffset.getZ()), new Vector3(0, 1, 0));
		}
	}

	@GodotMethod
	public void setTargetPosition(Vector3 targetPosition) {
		if (navAgent == null) return;
		navAgent.call("set_target_position", targetPosition);
	}
}
