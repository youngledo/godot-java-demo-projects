package demos.misc.custom_logging;

import org.godot.annotation.GodotClass;
import org.godot.annotation.GodotMethod;
import org.godot.node.Control;
import org.godot.node.Label;
import org.godot.singleton.OS;
import org.godot.singleton.ProjectSettings;

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
        System.err.println("Error 1.");
        System.err.println("Warning 1.");
        System.err.println("Error 2.");
        System.err.println("Warning 2.");
        System.out.println("Normal message 2.");
        System.err.println("Normal message 1 (stderr).");
        System.err.println("Normal message 2 (stderr).");
        System.out.print("Normal message 1 (raw). ");
        System.out.println("Normal message 2 (raw).\n--------");

        Label flushLabel = getNodeAs("FlushStdoutOnPrint", Label.class);
        if (flushLabel != null) {
            Object flushSetting = ProjectSettings.singleton().getSettingWithOverride("application/run/flush_stdout_on_print");
            boolean flushOn = flushSetting instanceof Boolean value && value;
            flushLabel.setText(flushOn ? "Flush stdout on print: Yes (?)" : "Flush stdout on print: No (?)");
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
        System.err.println("Printing warning #" + warningCounter + ".");
    }

    @GodotMethod
    public void OnPrintErrorPressed() {
        errorCounter++;
        System.err.println("Printing error #" + errorCounter + ".");
    }

    @GodotMethod
    public void OnOpenLogsFolderPressed() {
        Object logPath = ProjectSettings.singleton().getSettingWithOverride("debug/file_logging/log_path");
        String pathStr = String.valueOf(logPath);
        String baseDir = pathStr.substring(0, Math.max(0, pathStr.lastIndexOf('/')));
        OS.singleton().shellOpen(ProjectSettings.singleton().globalizePath(baseDir));
    }

    @GodotMethod
    public void OnCrashEnginePressed() {
        OS.singleton().crash("Crashing the engine on user request (the Crash Engine button was pressed). Do not report this as a bug.");
    }
}
