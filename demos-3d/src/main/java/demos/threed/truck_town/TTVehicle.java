package demos.threed.truck_town;

import org.godot.annotation.Export;
import org.godot.annotation.GodotClass;
import org.godot.annotation.GodotMethod;
import org.godot.math.Vector3;
import org.godot.node.VehicleBody3D;
import org.godot.node.Node;
import org.godot.node.SceneTree;
import org.godot.singleton.Input;

@GodotClass(name = "TTVehicle", parent = "VehicleBody3D")
public class TTVehicle extends VehicleBody3D {

	private static final double STEER_SPEED = 1.5;
	private static final double STEER_LIMIT = 0.4;

	@Export
	public double engineForceValue = 40.0;

	private double previousSpeed = 0.0;
	private boolean turboActive = false;
	private boolean headlightsActive = false;
	private double steerTarget = 0.0;
	private org.godot.node.Node engineSound;
	private org.godot.node.Node impactSound;
	private org.godot.node.Node honkSound;
	private org.godot.Godot turbometer;
	private org.godot.node.AnimationPlayer turboAnimator;
	private boolean initialized = false;

	@Override
	public void _ready() {
		if (initialized) return;
		initialized = true;

		engineSound = getNode("EngineSound");
		impactSound = getNode("ImpactSound");
		honkSound = getNode("HonkSound");
	}

	@Override
	public void _physicsProcess(double delta) {
		org.godot.singleton.Input input = org.godot.singleton.Input.singleton();

		steerTarget = 0.0;
		if ((boolean) input.isActionPressed("turn_left")) steerTarget += 1;
		if ((boolean) input.isActionPressed("turn_right")) steerTarget -= 1;
		steerTarget *= STEER_LIMIT;

		// Engine sound
		Vector3 linVel = (Vector3) getProperty("linear_velocity");
		double speed = linVel != null ? Math.sqrt(linVel.getX()*linVel.getX() + linVel.getY()*linVel.getY() + linVel.getZ()*linVel.getZ()) : 0;
		double desiredPitch = 0.05 + speed / (engineForceValue * 0.5);
		if (engineSound != null) {
			double currentPitch = (double) engineSound.getProperty("pitch_scale");
			engineSound.setProperty("pitch_scale", currentPitch + (desiredPitch - currentPitch) * 0.2);
		}

		// Impact detection
		if (Math.abs(speed - previousSpeed) > 1.0) {
			if (impactSound != null) impactSound.call("play");
		}

		// Acceleration / reverse
		if ((boolean) input.isActionPressed("accelerate")) {
			double ef = engineForceValue;
			if (speed < 5.0 && speed > 0.001) {
				ef = Math.min(100.0, engineForceValue * 5.0 / speed);
			}
			setProperty("engine_force", ef);
		} else if ((boolean) input.isActionPressed("reverse")) {
			double ef = -engineForceValue;
			if (speed < 5.0 && speed > 0.001) {
				ef = -Math.min(100.0, engineForceValue * 5.0 / speed);
			}
			setProperty("engine_force", ef);
		} else {
			setProperty("engine_force", 0.0);
		}

		// Steering
		double currentSteer = (double) getProperty("steering");
		double newSteer = currentSteer;
		if (currentSteer < steerTarget) {
			newSteer = Math.min(currentSteer + STEER_SPEED * delta, steerTarget);
		} else if (currentSteer > steerTarget) {
			newSteer = Math.max(currentSteer - STEER_SPEED * delta, steerTarget);
		}
		setProperty("steering", newSteer);

		// Boost
		boolean boostPressed = (boolean) input.isActionPressed("boost");
		boolean newTurbo = boostPressed && turbometer != null && (double) turbometer.getProperty("value") > 0;
		if (newTurbo != turboActive && turboAnimator != null) {
			turboAnimator.play(newTurbo ? "TURBO" : "Idle");
		}
		turboActive = newTurbo;
		if (turboActive && turbometer != null) {
			double tv = (double) turbometer.getProperty("value") - delta * 3.0;
			turbometer.setProperty("value", tv);
		} else if (!boostPressed && turbometer != null) {
			double tv = (double) turbometer.getProperty("value") + delta;
			turbometer.setProperty("value", Math.min(1.0, tv));
		}

		if (turboActive) {
			// Apply boost force in forward direction
			setProperty("constant_force", new Vector3(0, 0, 400.0));
		} else {
			setProperty("constant_force", new Vector3(0, 0, 0));
		}

		previousSpeed = speed;
	}

	@Override
	public boolean _input(Object inputEvent) {
		org.godot.node.InputEvent ev = (org.godot.node.InputEvent) inputEvent;
		if ((boolean) ev.isActionPressed("honk")) {
			if (honkSound != null) honkSound.call("play");
		}
		return false;
	}

	@GodotMethod
	public void toggleHeadlights() {
		headlightsActive = !headlightsActive;
		org.godot.node.SceneTree tree = getTree();
		if (tree != null) {
			tree.call("call_group", "headlight", "set_visible", headlightsActive);
		}
	}
}
