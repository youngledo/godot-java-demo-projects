package demos.twod.role_playing_game;

import org.godot.annotation.GodotClass;
import org.godot.node.Node2D;
import org.godot.node.Node;

@GodotClass(name = "RPPawn", parent = "Node2D")
public class RPPawn extends Node2D {

    public static final int CELL_TYPE_ACTOR = 0;
    public static final int CELL_TYPE_OBSTACLE = 1;
    public static final int CELL_TYPE_OBJECT = 2;

    protected int type = CELL_TYPE_ACTOR;
    protected boolean active = true;

    private boolean initialized = false;

    @Override
    public void _ready() {
        if (initialized) return;
        initialized = true;

        Object typeObj = getProperty("type");
        if (typeObj instanceof Number) type = ((Number) typeObj).intValue();
    }

    public void setActive(boolean value) {
        active = value;
        setProcess(value);
        setProcessInput(value);
    }

    public boolean isActive() { return active; }
    public int getType() { return type; }
}
