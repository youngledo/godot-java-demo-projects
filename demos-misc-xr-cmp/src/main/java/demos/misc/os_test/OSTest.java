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
        org.godot.Godot audioServer = (org.godot.Godot) call("AudioServer");
        addLine("Mix rate", call("AudioServer.get_mix_rate") + " Hz");
        addLine("Output latency", String.format("%f ms", ((double) call("AudioServer.get_output_latency")) * 1000));
        addLine("Output device list", joinArray(call("AudioServer.get_output_device_list")));
        addLine("Capture device list", joinArray(call("AudioServer.get_input_device_list")));
        addLine("Connected MIDI inputs", scanMidiInputs());

        addHeader("Date and time");
        addLine("Date and time (local)", call("Time.get_datetime_string_from_system", false, true));
        addLine("Date and time (UTC)", call("Time.get_datetime_string_from_system", true, true));
        addLine("Date (local)", call("Time.get_date_string_from_system", false));
        addLine("Date (UTC)", call("Time.get_date_string_from_system", true));
        addLine("Time (local)", call("Time.get_time_string_from_system", false));
        addLine("Time (UTC)", call("Time.get_time_string_from_system", true));
        addLine("Timezone", call("Time.get_time_zone_from_system"));
        addLine("UNIX time", call("Time.get_unix_time_from_system"));

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
        Object versionInfo = call("Engine.get_version_info");
        addLine("Version", versionInfo instanceof java.util.Map ? ((java.util.Map<?, ?>) versionInfo).get("string") : String.valueOf(versionInfo));
        addLine("Compiled for architecture", call("Engine.get_architecture_name"));
        addLine("Command-line arguments", call("OS.get_cmdline_args"));
        addLine("Is debug build", call("OS.is_debug_build"));
        addLine("Executable path", call("OS.get_executable_path"));
        addLine("User data directory", call("OS.get_user_data_dir"));
        addLine("Filesystem is persistent", call("OS.is_userfs_persistent"));
        addLine("Process ID (PID)", call("OS.get_process_id"));
        addLine("Main thread ID", call("OS.get_main_thread_id"));
        addLine("Thread caller ID", call("OS.get_thread_caller_id"));
        addLine("Memory information", call("OS.get_memory_info"));
        addLine("Static memory usage", call("OS.get_static_memory_usage"));
        addLine("Static memory peak usage", call("OS.get_static_memory_peak_usage"));

        addHeader("Environment");
        addLine("Value of `PATH`", org.godot.singleton.OS.singleton().getEnvironment("PATH"));
        addLine("Value of `path`", org.godot.singleton.OS.singleton().getEnvironment("path"));

        addHeader("Hardware");
        addLine("Model name", call("OS.get_model_name"));
        addLine("Processor name", call("OS.get_processor_name"));
        addLine("Processor count", call("OS.get_processor_count"));
        addLine("Device unique ID", call("OS.get_unique_id"));

        addHeader("Input");
        addLine("Device has touch screen", ds.call("is_touchscreen_available"));
        boolean hasVirtualKeyboard = (boolean) ds.call("has_feature", 11); // FEATURE_VIRTUAL_KEYBOARD
        addLine("Device has virtual keyboard", hasVirtualKeyboard);
        if (hasVirtualKeyboard) {
            addLine("Virtual keyboard height", ds.call("virtual_keyboard_get_height"));
        }

        addHeader("Localization");
        addLine("Locale", call("OS.get_locale"));
        addLine("Language", call("OS.get_locale_language"));

        addHeader("Mobile");
        addLine("Granted permissions", call("OS.get_granted_permissions"));

        addHeader("Software");
        addLine("OS name", call("OS.get_name"));
        addLine("OS version", call("OS.get_version"));
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
