package demos.twod.role_playing_game;

import org.godot.annotation.GodotClass;
import org.godot.math.Vector2;
import org.godot.singleton.Input;

@GodotClass(name = "RPPlayer", parent = "Node2D")
public class RPPlayer extends RPWalker {

    private org.godot.singleton.Input input;

    @Override
    public void _ready() {
        super._ready();
        input = org.godot.singleton.Input.singleton();
    }

    @Override
    public void _process(double delta) {
        if (input == null || isMoving()) return;

        Object moveVecObj = input.call("get_vector", "move_left", "move_right", "move_up", "move_down");
        if (!(moveVecObj instanceof Vector2)) return;
        Vector2 inputDir = (Vector2) moveVecObj;

        // Round to integer direction
        int ix = (int) Math.round(inputDir.x);
        int iy = (int) Math.round(inputDir.y);
        if (ix == 0 && iy == 0) return;

        updateLookDirection(new Vector2(ix, iy));

        org.godot.math.Vector2i dir = new org.godot.math.Vector2i(ix, iy);
        if (grid != null) {
            Object result = grid.call("request_move", this, dir);
            if (result instanceof Vector2) {
                Vector2 targetPos = (Vector2) result;
                if (targetPos.x != 0 || targetPos.y != 0) {
                    moveTo(targetPos);
                    return;
                }
            }
        }

        if (active) bump();
    }
}
