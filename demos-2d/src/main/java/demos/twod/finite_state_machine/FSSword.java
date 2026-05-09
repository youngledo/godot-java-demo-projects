package demos.twod.finite_state_machine;

import org.godot.annotation.GodotClass;
import org.godot.annotation.GodotMethod;
import org.godot.annotation.Signal;
import org.godot.node.Area2D;
import org.godot.node.CollisionObject2D;
import org.godot.node.Node;
import java.util.ArrayList;

@GodotClass(name = "FSSword", parent = "Area2D")
public class FSSword extends Area2D {

    private static final int MAX_COMBO_COUNT = 3;

    private int state = 0; // 0=IDLE, 1=ATTACK
    private int attackInputState = 0; // 0=IDLE, 1=LISTENING, 2=REGISTERED
    private boolean readyForNextAttack = false;
    private int comboCount = 0;
    private double currentDamage = 1;
    private String currentAnimation = "";
    private ArrayList<Long> hitObjects = new ArrayList<>();

    private double[][] combo = {
            {1, 1, 3}, // damage values
    };
    private String[] comboAnimations = {"attack_fast", "attack_fast", "attack_medium"};
    private double[] comboDamages = {1, 1, 3};

    private boolean initialized = false;

    @Signal
    public void attackFinished() {}

    @Override
    public void _ready() {
        if (initialized) return;
        initialized = true;

        org.godot.node.AnimationPlayer animPlayer = (org.godot.node.AnimationPlayer) getNode("AnimationPlayer");
        if (animPlayer != null) {
            animPlayer.connect("animation_finished", new org.godot.core.Callable(this, "on_animation_finished"), 0);
        }
        connect("body_entered", new org.godot.core.Callable(this, "on_body_entered"));

        // Connect to StateMachine's state_changed signal via owner (Player node)
        org.godot.node.Node owner = (org.godot.node.Node) getProperty("owner");
        if (owner != null) {
            org.godot.Godot stateMachine = (org.godot.node.Node) owner.getNode("StateMachine");
            if (stateMachine != null) {
                stateMachine.connect("state_changed", new org.godot.core.Callable(this, "on_StateMachine_state_changed"), 0);
            }
        }

        changeState(0);
    }

    private void changeState(int newState) {
        if (state == 1) { // ATTACK
            hitObjects.clear();
            attackInputState = 1; // LISTENING
            readyForNextAttack = false;
        }

        if (newState == 0) { // IDLE
            comboCount = 0;
            org.godot.node.AnimationPlayer animPlayer = (org.godot.node.AnimationPlayer) getNode("AnimationPlayer");
            if (animPlayer != null) animPlayer.stop();
            setProperty("visible", false);
            setProperty("monitoring", false);
        } else if (newState == 1) { // ATTACK
            currentDamage = comboDamages[comboCount - 1];
            currentAnimation = comboAnimations[comboCount - 1];
            org.godot.node.AnimationPlayer animPlayer = (org.godot.node.AnimationPlayer) getNode("AnimationPlayer");
            if (animPlayer != null) animPlayer.play(currentAnimation);
            setProperty("visible", true);
            setProperty("monitoring", true);
        }

        state = newState;
    }

    @Override
    public boolean _unhandledInput(Object inputEvent) {
        if (state != 1) return false;
        if (attackInputState != 1) return false;

        org.godot.node.InputEvent event = inputEvent instanceof org.godot.node.InputEvent ? (org.godot.node.InputEvent) inputEvent : null;
        if (event != null) {
            if (event.isActionPressed("attack", false, false)) {
                attackInputState = 2; // REGISTERED
            }
        }
        return false;
    }

    @Override
    public void _physicsProcess(double delta) {
        if (attackInputState == 2 && readyForNextAttack) {
            doAttack();
        }
    }

    public void doAttack() {
        comboCount++;
        changeState(1);
    }

    public void setAttackInputListening() {
        attackInputState = 1;
    }

    public void setReadyForNextAttack() {
        readyForNextAttack = true;
    }

    @GodotMethod
    public void onBodyEntered(org.godot.Godot body) {
        if (!(body instanceof Node bodyNode)) return;
        if (!bodyNode.hasNode("Health")) return;

        long ridId = body instanceof CollisionObject2D collisionObject ? collisionObject.getRid() : System.identityHashCode(body);
        if (hitObjects.contains(ridId)) return;
        hitObjects.add(ridId);

        if (body instanceof FSPlayerController playerController) {
            playerController.takeDamage(this, currentDamage, null);
        }
    }

    @GodotMethod
    public void onAnimationFinished(String name) {
        if (currentAnimation.isEmpty()) return;
        if (attackInputState == 2 && comboCount < MAX_COMBO_COUNT) {
            doAttack();
        } else {
            changeState(0);
            emitSignal("attack_finished");
        }
    }

    @GodotMethod
    public void onStateMachineStateChanged(org.godot.Godot currentState) {
        Object nameObj = currentState.getProperty("name");
        String name = nameObj != null ? nameObj.toString() : "";
        if (name.equals("Attack")) {
            doAttack();
        }
    }
}
