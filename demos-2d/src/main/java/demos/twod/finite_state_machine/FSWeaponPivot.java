package demos.twod.finite_state_machine;

import org.godot.annotation.GodotClass;
import org.godot.annotation.GodotMethod;
import org.godot.node.Marker2D;
import org.godot.math.Vector2;

@GodotClass(name = "FSWeaponPivot", parent = "Marker2D")
public class FSWeaponPivot extends Marker2D {

    private int zIndexStart = 0;
    private boolean initialized = false;

    @Override
    public void _ready() {
        if (initialized) return;
        initialized = true;

        org.godot.Godot owner = (org.godot.Godot) getProperty("owner");
        if (owner != null) {
            owner.connect("direction_changed", new org.godot.core.Callable(this, "on_direction_changed"), 0);
        }
        Object zObj = getProperty("z_index");
        zIndexStart = zObj instanceof Number ? ((Number) zObj).intValue() : 0;
    }

    @GodotMethod
    public void onDirectionChanged(Vector2 direction) {
        setProperty("rotation", direction.angle());
        if (direction.y == -1 && direction.x == 0) {
            setProperty("z_index", zIndexStart - 1);
        } else {
            setProperty("z_index", zIndexStart);
        }
    }
}
