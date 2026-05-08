package demos.misc.os_test;

import org.godot.annotation.GodotClass;
import org.godot.node.Panel;
import org.godot.node.Node;

@GodotClass(name = "OSTest", parent = "Panel")
public class OSTest extends Panel {

    private org.godot.node.Node rtl;
    private int lineCount = 0;
    private boolean initialized = false;

    @Override
    public void _ready() {
        if (initialized) return;
        initialized = true;

        rtl = getNode("HBoxContainer/Features");
        if (rtl != null) rtl.call("grab_focus");

        addHeader("Audio");
        addLine("Mix rate", org.godot.singleton.AudioServer.singleton().getMixRate() + " Hz");
        addLine("Output latency", String.format("%f ms", org.godot.singleton.AudioServer.singleton().getOutputLatency() * 1000));
        addLine("Output device list", joinArray(org.godot.singleton.AudioServer.singleton().getOutputDeviceList()));
        addLine("Capture device list", joinArray(org.godot.singleton.AudioServer.singleton().getInputDeviceList()));
        addLine("Connected MIDI inputs", scanMidiInputs());

        addHeader("Date and time");
        addLine("Date and time (local)", org.godot.singleton.Time.singleton().getDatetimeStringFromSystem(false, true));
        addLine("Date and time (UTC)", org.godot.singleton.Time.singleton().getDatetimeStringFromSystem(true, true));
        addLine("Date (local)", org.godot.singleton.Time.singleton().getDateStringFromSystem(false));
        addLine("Date (UTC)", org.godot.singleton.Time.singleton().getDateStringFromSystem(true));
        addLine("Time (local)", org.godot.singleton.Time.singleton().getTimeStringFromSystem(false));
        addLine("Time (UTC)", org.godot.singleton.Time.singleton().getTimeStringFromSystem(true));
        addLine("Timezone", org.godot.singleton.Time.singleton().getTimeZoneFromSystem());
        addLine("UNIX time", org.godot.singleton.Time.singleton().getUnixTimeFromSystem());

        addHeader("Display");
        org.godot.singleton.DisplayServer ds = org.godot.singleton.DisplayServer.singleton();
        addLine("Screen count", ds.call("get_screen_count"));
        addLine("DPI", ds.call("screen_get_dpi"));
        addLine("Scale factor", ds.call("screen_get_scale"));
        addLine("Maximum scale factor", ds.call("screen_get_max_scale"));
        addLine("Startup screen position", ds.call("screen_get_position"));
        addLine("Startup screen size", ds.call("screen_get_size"));
        double refreshRate = (double) ds.call("screen_get_refresh_rate");
        addLine("Startup screen refresh rate", refreshRate > 0.0 ? String.format("%f Hz", refreshRate) : "");
        addLine("Usable (safe) area rectangle", ds.call("get_display_safe_area"));
        long orientation = (long) ds.call("screen_get_orientation");
        String[] orientations = {"Landscape", "Portrait", "Landscape (reverse)", "Portrait (reverse)",
            "Landscape (defined by sensor)", "Portrait (defined by sensor)", "Defined by sensor"};
        addLine("Screen orientation", orientations[(int) Math.min(orientation, orientations.length - 1)]);

        addHeader("Engine");
        Object versionInfo = org.godot.singleton.Engine.singleton().getVersionInfo();
        addLine("Version", versionInfo instanceof java.util.Map ? ((java.util.Map<?, ?>) versionInfo).get("string") : String.valueOf(versionInfo));
        addLine("Compiled for architecture", org.godot.singleton.Engine.singleton().getArchitectureName());
        addLine("Command-line arguments", joinArray(org.godot.singleton.OS.singleton().getCmdlineArgs()));
        addLine("Is debug build", org.godot.singleton.OS.singleton().isDebugBuild());
        addLine("Executable path", org.godot.singleton.OS.singleton().getExecutablePath());
        addLine("User data directory", org.godot.singleton.OS.singleton().getUserDataDir());
        addLine("Filesystem is persistent", org.godot.singleton.OS.singleton().isUserfsPersistent());
        addLine("Process ID (PID)", org.godot.singleton.OS.singleton().getProcessId());
        addLine("Main thread ID", org.godot.singleton.OS.singleton().getMainThreadId());
        addLine("Thread caller ID", org.godot.singleton.OS.singleton().getThreadCallerId());
        addLine("Memory information", org.godot.singleton.OS.singleton().getMemoryInfo());
        addLine("Static memory usage", org.godot.singleton.OS.singleton().getStaticMemoryUsage());
        addLine("Static memory peak usage", org.godot.singleton.OS.singleton().getStaticMemoryPeakUsage());

        addHeader("Environment");
        addLine("Value of `PATH`", org.godot.singleton.OS.singleton().getEnvironment("PATH"));
        addLine("Value of `path`", org.godot.singleton.OS.singleton().getEnvironment("path"));

        addHeader("Hardware");
        addLine("Model name", org.godot.singleton.OS.singleton().getName());
        addLine("Processor name", org.godot.singleton.OS.singleton().getProcessorName());
        addLine("Processor count", org.godot.singleton.OS.singleton().getProcessorCount());
        addLine("Device unique ID", org.godot.singleton.OS.singleton().getUniqueId());

        addHeader("Input");
        addLine("Device has touch screen", ds.call("is_touchscreen_available"));
        boolean hasVirtualKeyboard = (boolean) ds.call("has_feature", 11); // FEATURE_VIRTUAL_KEYBOARD
        addLine("Device has virtual keyboard", hasVirtualKeyboard);
        if (hasVirtualKeyboard) {
            addLine("Virtual keyboard height", ds.call("virtual_keyboard_get_height"));
        }

        addHeader("Localization");
        addLine("Locale", org.godot.singleton.OS.singleton().getLocale());
        addLine("Language", org.godot.singleton.OS.singleton().getLocaleLanguage());

        addHeader("Mobile");
        addLine("Granted permissions", joinArray(org.godot.singleton.OS.singleton().getGrantedPermissions()));

        addHeader("Software");
        addLine("OS name", org.godot.singleton.OS.singleton().getName());
        addLine("OS version", org.godot.singleton.OS.singleton().getVersion());
        addLine("Distribution name", call("OS.get_distribution_name"));
        addLine("System dark mode supported", ds.call("is_dark_mode_supported"));
        addLine("System dark mode enabled", ds.call("is_dark_mode"));
        Object accentColor = ds.call("get_accent_color");
        addLine("System accent color", accentColor != null ? "#" + accentColor : "N/A");
        Object systemFonts = call("OS.get_system_fonts");
        addLine("System fonts", (systemFonts instanceof Object[] ? ((Object[]) systemFonts).length : 0) + " fonts available");
        addLine("System font path (\"sans-serif\")", call("OS.get_system_font_path", "sans-serif"));
        addLine("System font path for English text", joinArray(call("OS.get_system_font_path_for_text", "sans-serif", "Hello")));
        addLine("System font path for Chinese text", joinArray(call("OS.get_system_font_path_for_text", "sans-serif", "你好")));
        addLine("System font path for Japanese text", joinArray(call("OS.get_system_font_path_for_text", "sans-serif", "こんにちは")));

        addHeader("Security");
        addLine("Is sandboxed", call("OS.is_sandboxed"));
        addLine("Entropy (8 random bytes)", call("OS.get_entropy", 8));
        Object caCerts = call("OS.get_system_ca_certificates");
        String caCertsStr = String.valueOf(caCerts);
        addLine("System CA certificates", !caCertsStr.isEmpty() ? "Available (" + caCertsStr.length() + " bytes)" : "Not available");

        addHeader("Engine directories");
        addLine("User data", call("OS.get_data_dir"));
        addLine("Configuration", call("OS.get_config_dir"));
        addLine("Cache", call("OS.get_cache_dir"));

        addHeader("System directories");
        addLine("Desktop", call("OS.get_system_dir", 0));
        addLine("DCIM", call("OS.get_system_dir", 1));
        addLine("Documents", call("OS.get_system_dir", 2));
        addLine("Downloads", call("OS.get_system_dir", 3));
        addLine("Movies", call("OS.get_system_dir", 4));
        addLine("Music", call("OS.get_system_dir", 5));
        addLine("Pictures", call("OS.get_system_dir", 6));
        addLine("Ringtones", call("OS.get_system_dir", 7));

        addHeader("Video");
        addLine("Adapter name", call("RenderingServer.get_video_adapter_name"));
        addLine("Adapter vendor", call("RenderingServer.get_video_adapter_vendor"));
        String renderMethod = (String) call("RenderingServer.get_current_rendering_method");
        if (!"gl_compatibility".equals(renderMethod)) {
            long adapterType = (long) call("RenderingServer.get_video_adapter_type");
            String[] adapterTypes = {"Other (Unknown)", "Integrated", "Discrete", "Virtual", "CPU"};
            addLine("Adapter type", adapterTypes[(int) Math.min(adapterType, adapterTypes.length - 1)]);
        }
        addLine("Adapter graphics API version", call("RenderingServer.get_video_adapter_api_version"));

        Object driverInfo = call("OS.get_video_adapter_driver_info");
        if (driverInfo instanceof Object[]) {
            Object[] info = (Object[]) driverInfo;
            if (info.length > 0) addLine("Adapter driver name", info[0]);
            if (info.length > 1) addLine("Adapter driver version", info[1]);
        }
    }

    private void addHeader(String header) {
        if (rtl != null) {
            rtl.call("append_text", "\n[font_size=24][color=#5cf]" + header + "[/color][/font_size]\n[font_size=1]\n[/font_size]");
        }
        System.out.println("\n" + header);
        System.out.println("=".repeat(header.length()));
    }

    private void addLine(String key, Object value) {
        lineCount++;
        String valueStr = String.valueOf(value);
        if (valueStr.isEmpty()) valueStr = "(empty)";
        String originalValue = valueStr;

        if (value instanceof Boolean) {
            boolean b = (Boolean) value;
            valueStr = b ? "[color=6f7]true[/color]" : "[color=#f76]false[/color]";
        }

        String bgcolor = lineCount % 2 == 0 ? "[bgcolor=#8883]" : "";
        String bgcolorEnd = lineCount % 2 == 0 ? "[/bgcolor]" : "";

        if (rtl != null) {
            rtl.call("append_text", bgcolor + "[color=#9df]" + key + ":[/color] " +
                (valueStr.isEmpty() ? "[color=#fff8](empty)[/color]" : valueStr) + bgcolorEnd + "\n");
        }
        System.out.println(key + ": " + originalValue);
    }

    private String scanMidiInputs() {
        String dsName = (String) org.godot.singleton.DisplayServer.singleton().call("get_name");
        if ("headless".equals(dsName)) return "";

        org.godot.singleton.OS os = org.godot.singleton.OS.singleton();
        os.call("open_midi_inputs");
        Object devices = os.call("get_connected_midi_inputs");
        String result = joinArray(devices);
        os.call("close_midi_inputs");
        return result;
    }

    private String joinArray(Object arr) {
        if (arr instanceof Object[]) {
            return String.join(", ", java.util.Arrays.stream((Object[]) arr)
                .map(String::valueOf).toArray(String[]::new));
        }
        return arr != null ? String.valueOf(arr) : "";
    }
}
