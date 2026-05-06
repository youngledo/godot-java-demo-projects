package demos.gui.rich_text_bbcode;

import org.godot.annotation.GodotClass;
import org.godot.annotation.GodotMethod;
import org.godot.node.Control;
import org.godot.node.SceneTree;

@GodotClass(name = "RichTextBBCode", parent = "Control")
public class RichTextBBCode extends Control {

    @GodotMethod
    public void OnRichTextLabelMetaClicked(Object meta) {
        org.godot.singleton.OS os = org.godot.singleton.OS.singleton();
        long err = (long) os.call("shell_open", String.valueOf(meta));
        if (err == 0) {
            System.out.println("Opened link '" + meta + "' successfully!");
        } else {
            System.out.println("Failed opening the link '" + meta + "'!");
        }
    }

    @GodotMethod
    public void OnPauseToggled(boolean buttonPressed) {
        org.godot.node.SceneTree tree = getTree();
        if (tree != null) {
            tree.setProperty("paused", buttonPressed);
        }
    }
}
