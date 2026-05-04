package demos.misc.multiple_windows;

import org.godot.annotation.GodotClass;
import org.godot.annotation.GodotMethod;
import org.godot.node.RichTextLabel;

@GodotClass(name = "MWUrlOpener", parent = "RichTextLabel")
public class UrlOpener extends RichTextLabel {

    private boolean initialized = false;

    @Override
    public void _ready() {
        if (initialized) return;
        initialized = true;
        connect("meta_clicked",
            new org.godot.core.Callable(this, "_on_meta_clicked"), 0);
    }

    @GodotMethod
    public void _on_meta_clicked(Object meta) {
        org.godot.singleton.OS.singleton().call("shell_open", String.valueOf(meta));
    }
}
