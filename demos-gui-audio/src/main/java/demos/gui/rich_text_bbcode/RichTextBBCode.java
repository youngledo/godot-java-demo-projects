package demos.gui.rich_text_bbcode;

import org.godot.annotation.GodotClass;
import org.godot.annotation.GodotMethod;
import org.godot.node.Control;

@GodotClass(name = "RichTextBBCode", parent = "Control")
public class RichTextBBCode extends Control {

    @GodotMethod
    public void _on_RichTextLabel_meta_clicked(Object meta) {
        org.godot.singleton.OS os = org.godot.singleton.OS.singleton();
        long err = (long) os.call("shell_open", String.valueOf(meta));
        if (err == 0) {
            System.out.println("Opened link '" + meta + "' successfully!");
        } else {
            System.out.println("Failed opening the link '" + meta + "'!");
        }
    }

    @GodotMethod
    public void _on_pause_toggled(boolean buttonPressed) {
        org.godot.Godot tree = (org.godot.Godot) call("get_tree");
        if (tree != null) {
            tree.setProperty("paused", buttonPressed);
        }
    }
}
