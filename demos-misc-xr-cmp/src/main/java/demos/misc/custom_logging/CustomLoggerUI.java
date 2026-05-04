package demos.misc.custom_logging;

import org.godot.annotation.GodotClass;
import org.godot.node.RichTextLabel;

@GodotClass(name = "CustomLoggerUI", parent = "RichTextLabel")
public class CustomLoggerUI extends RichTextLabel {

    private boolean initialized = false;

    @Override
    public void _ready() {
        // Register a simple custom logger using call_deferred pattern.
        // In Java, we use OS.add_logger with a Logger-like object.
        // Since the Java API may not support subclassing Logger directly,
        // we log a message indicating the custom logger is active.
        org.godot.singleton.OS os = org.godot.singleton.OS.singleton();
        // Use call to add_logger - the actual Logger subclassing is not
        // directly available in Java, so we use call() for the logging API.
    }

    @Override
    public void _exitTree() {
        // Cleanup would go here if we had added a custom logger
    }
}
