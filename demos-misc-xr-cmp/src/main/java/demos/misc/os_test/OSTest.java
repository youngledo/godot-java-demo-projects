package demos.misc.os_test;

import java.util.Arrays;
import org.godot.annotation.GodotClass;
import org.godot.collection.GodotDictionary;
import org.godot.node.Panel;
import org.godot.node.RichTextLabel;
import org.godot.singleton.AudioServer;
import org.godot.singleton.DisplayServer;
import org.godot.singleton.Engine;
import org.godot.singleton.OS;
import org.godot.singleton.RenderingServer;
import org.godot.singleton.Time;

@GodotClass(name = "OSTest", parent = "Panel")
public class OSTest extends Panel {

    private RichTextLabel rtl;
    private int lineCount = 0;
    private boolean initialized = false;

    @Override
    public void _ready() {
        if (initialized) return;
        initialized = true;

        rtl = getNodeAs("HBoxContainer/Features", RichTextLabel.class);
        if (rtl != null) rtl.grabFocus();

        AudioServer audioServer = AudioServer.singleton();
        Time time = Time.singleton();
        DisplayServer displayServer = DisplayServer.singleton();
        Engine engine = Engine.singleton();
        OS os = OS.singleton();
        RenderingServer renderingServer = RenderingServer.singleton();

        addHeader("Audio");
        addLine("Mix rate", audioServer.getMixRate() + " Hz");
        addLine("Output latency", String.format("%f ms", audioServer.getOutputLatency() * 1000));
        addLine("Output device list", joinArray(audioServer.getOutputDeviceList()));
        addLine("Capture device list", joinArray(audioServer.getInputDeviceList()));
        addLine("Connected MIDI inputs", scanMidiInputs());

        addHeader("Date and time");
        addLine("Date and time (local)", time.getDatetimeStringFromSystem(false, true));
        addLine("Date and time (UTC)", time.getDatetimeStringFromSystem(true, true));
        addLine("Date (local)", time.getDateStringFromSystem(false));
        addLine("Date (UTC)", time.getDateStringFromSystem(true));
        addLine("Time (local)", time.getTimeStringFromSystem(false));
        addLine("Time (UTC)", time.getTimeStringFromSystem(true));
        addLine("Timezone", time.getTimeZoneFromSystem());
        addLine("UNIX time", time.getUnixTimeFromSystem());

        addHeader("Display");
        addLine("Screen count", displayServer.getScreenCount());
        addLine("DPI", displayServer.screenGetDpi());
        addLine("Scale factor", displayServer.screenGetScale());
        addLine("Maximum scale factor", displayServer.screenGetMaxScale());
        addLine("Startup screen position", displayServer.screenGetPosition());
        addLine("Startup screen size", displayServer.screenGetSize());
        double refreshRate = displayServer.screenGetRefreshRate();
        addLine("Startup screen refresh rate", refreshRate > 0.0 ? String.format("%f Hz", refreshRate) : "");
        addLine("Usable (safe) area rectangle", displayServer.getDisplaySafeArea());
        int orientation = displayServer.screenGetOrientation();
        String[] orientations = { "Landscape", "Portrait", "Landscape (reverse)", "Portrait (reverse)",
                "Landscape (defined by sensor)", "Portrait (defined by sensor)", "Defined by sensor" };
        addLine("Screen orientation", orientations[Math.min(orientation, orientations.length - 1)]);

        addHeader("Engine");
        Object versionInfo = engine.getVersionInfo();
        addLine("Version", versionString(versionInfo));
        addLine("Compiled for architecture", engine.getArchitectureName());
        addLine("Command-line arguments", joinArray(os.getCmdlineArgs()));
        addLine("Is debug build", os.isDebugBuild());
        addLine("Executable path", os.getExecutablePath());
        addLine("User data directory", os.getUserDataDir());
        addLine("Filesystem is persistent", os.isUserfsPersistent());
        addLine("Process ID (PID)", os.getProcessId());
        addLine("Main thread ID", os.getMainThreadId());
        addLine("Thread caller ID", os.getThreadCallerId());
        addLine("Memory information", os.getMemoryInfo());
        addLine("Static memory usage", os.getStaticMemoryUsage());
        addLine("Static memory peak usage", os.getStaticMemoryPeakUsage());

        addHeader("Environment");
        addLine("Value of `PATH`", os.getEnvironment("PATH"));
        addLine("Value of `path`", os.getEnvironment("path"));

        addHeader("Hardware");
        addLine("Model name", os.getName());
        addLine("Processor name", os.getProcessorName());
        addLine("Processor count", os.getProcessorCount());
        addLine("Device unique ID", os.getUniqueId());

        addHeader("Input");
        addLine("Device has touch screen", displayServer.isTouchscreenAvailable());
        boolean hasVirtualKeyboard = displayServer.hasFeature(11);
        addLine("Device has virtual keyboard", hasVirtualKeyboard);
        if (hasVirtualKeyboard) {
            addLine("Virtual keyboard height", displayServer.virtualKeyboardGetHeight());
        }

        addHeader("Localization");
        addLine("Locale", os.getLocale());
        addLine("Language", os.getLocaleLanguage());

        addHeader("Mobile");
        addLine("Granted permissions", joinArray(os.getGrantedPermissions()));

        addHeader("Software");
        addLine("OS name", os.getName());
        addLine("OS version", os.getVersion());
        addLine("Distribution name", os.getDistributionName());
        addLine("System dark mode supported", displayServer.isDarkModeSupported());
        addLine("System dark mode enabled", displayServer.isDarkMode());
        addLine("System accent color", displayServer.getAccentColor());
        String[] systemFonts = os.getSystemFonts();
        addLine("System fonts", systemFonts.length + " fonts available");
        addLine("System font path (\"sans-serif\")", os.getSystemFontPath("sans-serif"));
        addLine("System font path for English text", joinArray(os.getSystemFontPathForText("sans-serif", "Hello")));
        addLine("System font path for Chinese text", joinArray(os.getSystemFontPathForText("sans-serif", "你好")));
        addLine("System font path for Japanese text", joinArray(os.getSystemFontPathForText("sans-serif", "こんにちは")));

        addHeader("Security");
        addLine("Is sandboxed", os.isSandboxed());
        addLine("Entropy (8 random bytes)", joinArray(os.getEntropy(8)));
        String caCerts = os.getSystemCaCertificates();
        addLine("System CA certificates", !caCerts.isEmpty() ? "Available (" + caCerts.length() + " bytes)" : "Not available");

        addHeader("Engine directories");
        addLine("User data", os.getDataDir());
        addLine("Configuration", os.getConfigDir());
        addLine("Cache", os.getCacheDir());

        addHeader("System directories");
        addLine("Desktop", os.getSystemDir(0));
        addLine("DCIM", os.getSystemDir(1));
        addLine("Documents", os.getSystemDir(2));
        addLine("Downloads", os.getSystemDir(3));
        addLine("Movies", os.getSystemDir(4));
        addLine("Music", os.getSystemDir(5));
        addLine("Pictures", os.getSystemDir(6));
        addLine("Ringtones", os.getSystemDir(7));

        addHeader("Video");
        addLine("Adapter name", renderingServer.getVideoAdapterName());
        addLine("Adapter vendor", renderingServer.getVideoAdapterVendor());
        String renderMethod = renderingServer.getCurrentRenderingMethod();
        if (!"gl_compatibility".equals(renderMethod)) {
            int adapterType = renderingServer.getVideoAdapterType();
            String[] adapterTypes = { "Other (Unknown)", "Integrated", "Discrete", "Virtual", "CPU" };
            addLine("Adapter type", adapterTypes[Math.min(adapterType, adapterTypes.length - 1)]);
        }
        addLine("Adapter graphics API version", renderingServer.getVideoAdapterApiVersion());

        String[] driverInfo = os.getVideoAdapterDriverInfo();
        if (driverInfo.length > 0) addLine("Adapter driver name", driverInfo[0]);
        if (driverInfo.length > 1) addLine("Adapter driver version", driverInfo[1]);
    }

    private void addHeader(String header) {
        if (rtl != null) {
            rtl.appendText("\n[font_size=24][color=#5cf]" + header + "[/color][/font_size]\n[font_size=1]\n[/font_size]");
        }
        System.out.println("\n" + header);
        System.out.println("=".repeat(header.length()));
    }

    private void addLine(String key, Object value) {
        lineCount++;
        String valueStr = String.valueOf(value);
        if (valueStr.isEmpty()) valueStr = "(empty)";
        String originalValue = valueStr;

        if (value instanceof Boolean b) {
            valueStr = b ? "[color=6f7]true[/color]" : "[color=#f76]false[/color]";
        }

        String bgcolor = lineCount % 2 == 0 ? "[bgcolor=#8883]" : "";
        String bgcolorEnd = lineCount % 2 == 0 ? "[/bgcolor]" : "";

        if (rtl != null) {
            rtl.appendText(bgcolor + "[color=#9df]" + key + ":[/color] "
                    + (valueStr.isEmpty() ? "[color=#fff8](empty)[/color]" : valueStr) + bgcolorEnd + "\n");
        }
        System.out.println(key + ": " + originalValue);
    }

    private String scanMidiInputs() {
        if ("headless".equals(DisplayServer.singleton().getName())) return "";

        OS os = OS.singleton();
        os.openMidiInputs();
        String result = joinArray(os.getConnectedMidiInputs());
        os.closeMidiInputs();
        return result;
    }

    private String joinArray(Object arr) {
        if (arr instanceof Object[] values) {
            return String.join(", ", Arrays.stream(values).map(String::valueOf).toArray(String[]::new));
        }
        if (arr instanceof byte[] values) {
            return Arrays.toString(values);
        }
        return arr != null ? String.valueOf(arr) : "";
    }

    private String versionString(Object versionInfo) {
        if (versionInfo instanceof GodotDictionary dictionary) {
            Object value = dictionary.get("string");
            return String.valueOf(value);
        }
        if (versionInfo instanceof java.util.Map<?, ?> map) {
            return String.valueOf(map.get("string"));
        }
        return String.valueOf(versionInfo);
    }
}
