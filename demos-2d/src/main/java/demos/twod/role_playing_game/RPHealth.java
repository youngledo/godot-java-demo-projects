package demos.twod.role_playing_game;

import org.godot.annotation.GodotClass;
import org.godot.annotation.GodotMethod;
import org.godot.annotation.Signal;
import org.godot.node.Node;

@GodotClass(name = "RPHealth", parent = "Node")
public class RPHealth extends Node {

    private int life = 0;
    private int maxLife = 10;
    private int baseArmor = 0;
    private int armor = 0;
    private boolean initialized = false;

    @Signal
    public void dead() {}

    @Signal
    public void health_changed() {}

    @Override
    public void _ready() {
        if (initialized) return;
        initialized = true;

        Object lifeObj = getProperty("life");
        if (lifeObj instanceof Number) life = ((Number) lifeObj).intValue();
        Object maxObj = getProperty("max_life");
        if (maxObj instanceof Number) maxLife = ((Number) maxObj).intValue();
        Object armorObj = getProperty("base_armor");
        if (armorObj instanceof Number) baseArmor = ((Number) armorObj).intValue();
        armor = baseArmor;
    }

    public void takeDamage(int damage) {
        life = life - damage + armor;
        if (life <= 0) {
            call("emit_signal", "dead");
        } else {
            call("emit_signal", "health_changed", (double) life);
        }
    }

    public void heal(int amount) {
        life += amount;
        life = Math.max(life, Math.min(life, maxLife));
        call("emit_signal", "health_changed", (double) life);
    }

    @GodotMethod
    public double get_health_ratio() {
        return (double) life / maxLife;
    }

    public int getLife() { return life; }
    public int getMaxLife() { return maxLife; }
    public int getBaseArmor() { return baseArmor; }
    public int getArmor() { return armor; }
    public void setArmor(int value) { armor = value; }
}
