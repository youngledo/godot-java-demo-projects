package demos.misc.joypads;

import org.godot.annotation.GodotClass;
import org.godot.node.Node;

@GodotClass(name = "JoyMapping", parent = "RefCounted")
public class JoyMapping extends org.godot.node.RefCounted {

    public static final long TYPE_NONE = 0;
    public static final long TYPE_BTN = 1;
    public static final long TYPE_AXIS = 2;

    public static final long AXIS_FULL = 0;
    public static final long AXIS_HALF_PLUS = 1;
    public static final long AXIS_HALF_MINUS = 2;

    public long type = TYPE_NONE;
    public int idx = -1;
    public long axis = AXIS_FULL;
    public boolean inverted = false;

    // Constructor for creating from code
    public JoyMapping() {}

    public JoyMapping(long pType, int pIdx) {
        this.type = pType;
        this.idx = pIdx;
        this.axis = AXIS_FULL;
    }

    @Override
    public String toString() {
        if (type == TYPE_NONE) return "";

        String ts = type == TYPE_BTN ? "b" : "a";
        String prefix = "";
        String suffix = inverted ? "~" : "";

        if (axis == AXIS_HALF_PLUS) prefix = "+";
        else if (axis == AXIS_HALF_MINUS) prefix = "-";

        return prefix + ts + idx + suffix;
    }

    public String toHumanString() {
        if (type == TYPE_BTN) return "Button " + idx;
        if (type == TYPE_AXIS) {
            String prefix = "";
            if (axis == AXIS_HALF_PLUS) prefix = "(+) ";
            else if (axis == AXIS_HALF_MINUS) prefix = "(-) ";
            String suffix = inverted ? " (inverted)" : "";
            return "Axis " + prefix + idx + suffix;
        }
        return "";
    }
}
