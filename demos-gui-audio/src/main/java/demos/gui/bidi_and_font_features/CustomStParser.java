package demos.gui.bidi_and_font_features;

import org.godot.annotation.GodotClass;
import org.godot.node.LineEdit;

@GodotClass(name = "CustomStParser", parent = "LineEdit")
public class CustomStParser extends LineEdit {

    // Note: _structured_text_parser is a virtual method in GDScript that may not
    // be directly available as an override in godot-java. This port provides the
    // logic but the structured text parser override may need to be connected
    // differently depending on godot-java's support for this virtual method.

    public Object[] structuredTextParser(Object args, String text) {
        String[] tags = text.split(":");
        int prev = 0;
        java.util.List<double[]> output = new java.util.ArrayList<>();

        for (int i = 0; i < tags.length; i++) {
            double[] range1 = new double[]{prev, prev + tags[i].length(), 0}; // DIRECTION_AUTO = 0
            double[] range2 = new double[]{prev + tags[i].length(), prev + tags[i].length() + 1, 0};
            output.add(0, range1);
            output.add(0, range2);
            prev = prev + tags[i].length() + 1;
        }

        return output.toArray();
    }
}
