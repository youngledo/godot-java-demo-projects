package demos.misc.custom_logging;

import org.godot.annotation.GodotClass;
import org.godot.annotation.GodotMethod;
import org.godot.node.Control;
import org.godot.node.Node;

@GodotClass(name = "CustomLoggingMain", parent = "Control")
public class CLMain extends Control {

    private int messageCounter = 0;
    private int messageRawCounter = 0;
    private int messageStderrCounter = 0;
    private int warningCounter = 0;
    private int errorCounter = 0;
    private boolean initialized = false;

    @Override
    public void _ready() {
        if (initialized) return;
        initialized = true;

        System.out.println("Normal message 1.");
        call("push_error", "Error 1.");
        call("push_warning", "Warning 1.");
        call("push_error", "Error 2.");
        call("push_warning", "Warning 2.");
        System.out.println("Normal message 2.");
        System.err.println("Normal message 1 (stderr).");
        System.err.println("Normal message 2 (stderr).");
        System.out.print("Normal message 1 (raw). ");
        System.out.println("Normal message 2 (raw).\n--------");

        org.godot.node.Node flushLabel = getNode("FlushStdoutOnPrint");
        if (flushLabel != null) {
            org.godot.Godot projectSettings = (org.godot.Godot) call("ProjectSettings");
            // Check flush_stdout_on_print setting
            Object flushSetting = call("ProjectSettings.get_setting_with_override",
                "application/run/flush_stdout_on_print");
            boolean flushOn = false;
            if (flushSetting instanceof Boolean) {
                flushOn = (Boolean) flushSetting;
            }
            flushLabel.setProperty("text",
                flushOn ? "Flush stdout on print: Yes (?)" : "Flush stdout on print: No (?)");
        }
    }

    @GodotMethod
    public void OnPrintMessagePressed() {
        messageCounter++;
        System.out.println("Printing message #" + messageCounter + ".");
    }

    @GodotMethod
    public void OnPrintMessageRawPressed() {
        messageRawCounter++;
        System.out.print("Printing message #" + messageRawCounter + " (raw). ");
    }

    @GodotMethod
    public void OnPrintMessageStderrPressed() {
        messageStderrCounter++;
        System.err.println("Printing message #" + messageStderrCounter + " (stderr).");
    }

    @GodotMethod
    public void OnPrintWarningPressed() {
        warningCounter++;
        call("push_warning", "Printing warning #" + warningCounter + ".");
    }

    @GodotMethod
    public void OnPrintErrorPressed() {
        errorCounter++;
        call("push_error", "Printing error #" + errorCounter + ".");
    }

    @GodotMethod
    public void OnOpenLogsFolderPressed() {
        org.godot.singleton.OS os = org.godot.singleton.OS.singleton();
        Object logPath = call("ProjectSettings.get_setting_with_override",
            "debug/file_logging/log_path");
        String pathStr = String.valueOf(logPath);
        // Get base directory
        String baseDir = pathStr.substring(0, Math.max(0, pathStr.lastIndexOf('/')));
        os.call("shell_open", call("ProjectSettings.globalize_path", baseDir));
    }

    @GodotMethod
    public void OnCrashEnginePressed() {
        org.godot.singleton.OS.singleton().call("crash",
            "Crashing the engine on user request (the Crash Engine button was pressed). Do not report this as a bug.");
    }
}
