package demos.twod.finite_state_machine;

import org.godot.annotation.GodotClass;
import org.godot.annotation.GodotMethod;
import org.godot.annotation.Signal;
import org.godot.node.Area2D;
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
    public void attack_finished() {}

    @Override
    public void _ready() {
        if (initialized) return;
        initialized = true;

        org.godot.Godot animPlayer = (org.godot.Godot) call("get_node", "AnimationPlayer");
        if (animPlayer != null) {
            animPlayer.call("connect", "animation_finished", new org.godot.core.Callable(this, "on_animation_finished"));
        }
        call("connect", "body_entered", new org.godot.core.Callable(this, "on_body_entered"));

        // Connect to StateMachine's state_changed signal via owner (Player node)
        org.godot.Godot owner = (org.godot.Godot) getProperty("owner");
        if (owner != null) {
            org.godot.Godot stateMachine = (org.godot.Godot) owner.call("get_node", "StateMachine");
            if (stateMachine != null) {
                stateMachine.call("connect", "state_changed", new org.godot.core.Callable(this, "on_StateMachine_state_changed"));
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
            org.godot.Godot animPlayer = (org.godot.Godot) call("get_node", "AnimationPlayer");
            if (animPlayer != null) animPlayer.call("stop");
            setProperty("visible", false);
            setProperty("monitoring", false);
        } else if (newState == 1) { // ATTACK
            currentDamage = comboDamages[comboCount - 1];
            currentAnimation = comboAnimations[comboCount - 1];
            org.godot.Godot animPlayer = (org.godot.Godot) call("get_node", "AnimationPlayer");
            if (animPlayer != null) animPlayer.call("play", currentAnimation);
            setProperty("visible", true);
            setProperty("monitoring", true);
        }

        state = newState;
    }

    @Override
    public boolean _unhandledInput(Object inputEvent) {
        if (state != 1) return false;
        if (attackInputState != 1) return false;

        org.godot.Godot event = (org.godot.Godot) inputEvent;
        if (event != null) {
            Object pressed = event.call("is_action_pressed", "attack");
            if (pressed instanceof Boolean && (Boolean) pressed) {
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

    public void set_attack_input_listening() {
        attackInputState = 1;
    }

    public void set_ready_for_next_attack() {
        readyForNextAttack = true;
    }

    @GodotMethod
    public void on_body_entered(org.godot.Godot body) {
        boolean hasHealth = (boolean) body.call("has_node", "Health");
        if (!hasHealth) return;

        Object ridObj = body.call("get_rid");
        long ridId = System.identityHashCode(body);
        if (ridObj instanceof org.godot.Godot) {
            try {
                Object idObj = ((org.godot.Godot) ridObj).getProperty("id");
                if (idObj instanceof Number) ridId = ((Number) idObj).longValue();
            } catch (Exception ignored) {}
        }
        if (hitObjects.contains(ridId)) return;
        hitObjects.add(ridId);

        body.call("take_damage", this, currentDamage, null);
    }

    @GodotMethod
    public void on_animation_finished(String name) {
        if (currentAnimation.isEmpty()) return;
        if (attackInputState == 2 && comboCount < MAX_COMBO_COUNT) {
            doAttack();
        } else {
            changeState(0);
            call("emit_signal", "attack_finished");
        }
    }

    @GodotMethod
    public void on_StateMachine_state_changed(org.godot.Godot currentState) {
        Object nameObj = currentState.getProperty("name");
        String name = nameObj != null ? nameObj.toString() : "";
        if (name.equals("Attack")) {
            doAttack();
        }
    }
}
