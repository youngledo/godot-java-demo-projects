package demos.misc.joypads;

import org.godot.annotation.GodotClass;
import org.godot.annotation.GodotMethod;
import org.godot.node.Node;

import java.util.*;

@GodotClass(name = "RemapWizard", parent = "Node")
public class RemapWizard extends Node {

    private static final double DEADZONE = 0.3;

    private int joyIndex = -1;
    private String joyGuid = "";
    private String joyName = "";
    private int curStep = -1;
    private Map<String, Object> curMapping = new LinkedHashMap<>();
    private String lastMapping = "";

    private org.godot.node.Node joyButtons;
    private org.godot.node.Node joyAxes;
    private org.godot.node.Label joyMappingText;
    private org.godot.node.Node joyMappingFullAxis;
    private org.godot.node.Node joyMappingAxisInvert;

    private boolean initialized = false;

    @Override
    public void _ready() {
        if (initialized) return;
        initialized = true;

        joyButtons = getNode("Mapping/Margin/VBox/SubViewportContainer/SubViewport/JoypadDiagram/Buttons");
        joyAxes = getNode("Mapping/Margin/VBox/SubViewportContainer/SubViewport/JoypadDiagram/Axes");
        joyMappingText = (org.godot.node.Label) getNode("Mapping/Margin/VBox/Info/Text/Value");
        joyMappingFullAxis = getNode("Mapping/Margin/VBox/Info/Extra/FullAxis");
        joyMappingAxisInvert = getNode("Mapping/Margin/VBox/Info/Extra/InvertAxis");
    }

    @Override
    public boolean _input(Object inputEvent) {
        if (curStep == -1) return false;
        if (!(inputEvent instanceof org.godot.Godot)) return false;

        org.godot.Godot evt = (org.godot.Godot) inputEvent;
        String className = (String) evt.call("get_class");

        if (!"InputEventJoypadButton".equals(className) && !"InputEventJoypadMotion".equals(className)) return false;

        long device = (long) evt.getProperty("device");
        if (device != joyIndex) return false;

        String[] steps = getBaseKeys();

        if ("InputEventJoypadMotion".equals(className)) {
            getViewport().call("set_input_as_handled");
            double axisValue = (double) evt.getProperty("axis_value");
            long axisIdx = (long) evt.getProperty("axis");

            if (Math.abs(axisValue) > DEADZONE && joyMappingText != null) {
                // Create a mapping entry string
                boolean isInverted = joyMappingAxisInvert != null && (boolean) joyMappingAxisInvert.getProperty("button_pressed");
                boolean isFullAxis = joyMappingFullAxis != null && (boolean) joyMappingFullAxis.getProperty("button_pressed");

                String prefix = "";
                if (!isFullAxis) {
                    prefix = axisValue > 0 ? "+" : "-";
                }
                String suffix = isInverted ? "~" : "";
                String mapString = prefix + "a" + axisIdx + suffix;
                String humanString = "Axis " + prefix + axisIdx + suffix;

                joyMappingText.setProperty("text", humanString);
                curMapping.put(steps[curStep], mapString);
            }
        } else if ("InputEventJoypadButton".equals(className)) {
            boolean pressed = (boolean) evt.getProperty("pressed");
            if (pressed) {
                getViewport().call("set_input_as_handled");
                long buttonIndex = (long) evt.getProperty("button_index");
                String mapString = "b" + buttonIndex;
                if (joyMappingText != null) {
                    joyMappingText.setProperty("text", "Button " + buttonIndex);
                }
                curMapping.put(steps[curStep], mapString);
            }
        }
        return false;
    }

    private String[] getBaseKeys() {
        return new String[]{
            "a", "b", "y", "x", "start", "back", "leftstick", "rightstick",
            "leftshoulder", "rightshoulder", "dpup", "dpleft", "dpdown", "dpright",
            "leftx", "lefty", "rightx", "righty", "lefttrigger", "righttrigger"
        };
    }

    private Map<String, Long> getBaseMap() {
        Map<String, Long> map = new LinkedHashMap<>();
        map.put("a", 0L); map.put("b", 1L); map.put("y", 3L); map.put("x", 2L);
        map.put("start", 6L); map.put("back", 4L);
        map.put("leftstick", 7L); map.put("rightstick", 8L);
        map.put("leftshoulder", 9L); map.put("rightshoulder", 10L);
        map.put("dpup", 11L); map.put("dpleft", 13L); map.put("dpdown", 12L); map.put("dpright", 14L);
        map.put("leftx", 0L); map.put("lefty", 1L);
        map.put("rightx", 2L); map.put("righty", 3L);
        map.put("lefttrigger", 4L); map.put("righttrigger", 5L);
        return map;
    }

    @GodotMethod
    public void start(long idx) {
        joyIndex = (int) idx;
        joyGuid = (String) org.godot.singleton.Input.singleton().getJoyGuid(idx);
        joyName = (String) org.godot.singleton.Input.singleton().getJoyName(idx);
        if (joyGuid == null || joyGuid.isEmpty() ) {
            call("push_error", "Unable to find controller");
            return;
        }
        org.godot.singleton.OS os = org.godot.singleton.OS.singleton();
        if ((boolean) os.call("has_feature", "web")) {
            org.godot.node.Node startWin = getNode("Start");
            if (startWin != null) {
                startWin.setProperty("window_title", joyGuid + " - " + joyName);
                startWin.call("popup_centered");
            }
        } else {
            OnWizardPressed();
        }
    }

    @GodotMethod
    public void OnWizardPressed() {
        org.godot.singleton.Input.singleton().removeJoyMapping(joyGuid);
        org.godot.node.Node startWin = getNode("Start");
        if (startWin != null) startWin.call("hide");
        org.godot.node.Node mappingWin = getNode("Mapping");
        if (mappingWin != null) mappingWin.call("popup_centered");
        curStep = 0;
        stepNext();
    }

    @GodotMethod
    public void OnCancelPressed() {
        reset();
    }

    @GodotMethod
    public void OnNextPressed() {
        curStep++;
        stepNext();
    }

    @GodotMethod
    public void OnPrevPressed() {
        if (curStep > 0) {
            curStep--;
            stepNext();
        }
    }

    @GodotMethod
    public void OnSkipPressed() {
        String[] steps = getBaseKeys();
        if (curStep < steps.length) {
            curMapping.remove(steps[curStep]);
        }
        curStep++;
        stepNext();
    }

    @GodotMethod
    public void OnXboxPressed() {
        remapAndClose(getXboxMapping());
    }

    @GodotMethod
    public void OnXboxosxPressed() {
        remapAndClose(getXboxOsxMapping());
    }

    @GodotMethod
    public void OnMappingPopupHide() {
        reset();
    }

    @GodotMethod
    public void OnStartCloseRequested() {
        org.godot.node.Node startWin = getNode("Start");
        if (startWin != null) startWin.call("hide");
    }

    @GodotMethod
    public void OnMappingCloseRequested() {
        org.godot.node.Node mappingWin = getNode("Mapping");
        if (mappingWin != null) mappingWin.call("hide");
    }

    @GodotMethod
    public void OnMapWindowCloseRequested() {
        org.godot.node.Node mapWin = getNode("MapWindow");
        if (mapWin != null) mapWin.call("hide");
    }

    @GodotMethod
    public void OnFullAxisToggled(boolean buttonPressed) {
        if (curStep == -1 || !buttonPressed) return;
        String[] steps = getBaseKeys();
        if (curStep < steps.length && curMapping.containsKey(steps[curStep])) {
            // Update axis type
            // Simplified: just note full axis in the mapping string
        }
    }

    @GodotMethod
    public void OnInvertAxisToggled(boolean buttonPressed) {
        if (curStep == -1) return;
        String[] steps = getBaseKeys();
        if (curStep < steps.length && curMapping.containsKey(steps[curStep])) {
            // Update inverted state
        }
    }

    private void stepNext() {
        org.godot.node.Node mappingWin = getNode("Mapping");
        String[] steps = getBaseKeys();
        if (mappingWin != null) {
            mappingWin.setProperty("title", "Step: " + (curStep + 1) + "/" + steps.length);
        }
        if (joyMappingText != null) joyMappingText.setProperty("text", "");

        if (curStep >= steps.length) {
            remapAndClose(curMapping);
        } else {
            updateStep();
        }
    }

    private void updateStep() {
        String[] steps = getBaseKeys();
        Map<String, Long> baseMap = getBaseMap();
        String key = steps[curStep];
        long idx = baseMap.getOrDefault(key, 0L);

        // Hide all buttons and axes
        if (joyButtons != null) {
            for (Object child : (Object[]) joyButtons.getChildren() ) {
                if (child instanceof org.godot.Godot) ((org.godot.Godot) child).call("hide");
            }
        }
        if (joyAxes != null) {
            for (Object child : (Object[]) joyAxes.getChildren() ) {
                if (child instanceof org.godot.Godot) ((org.godot.Godot) child).call("hide");
            }
        }

        // Show relevant indicator
        if (key.equals("leftx") || key.equals("lefty") || key.equals("rightx") || key.equals("righty")) {
            if (joyAxes != null) {
                org.godot.node.CanvasItem plus = (org.godot.node.CanvasItem) joyAxes.getNode(idx + "+");
                org.godot.node.CanvasItem minus = (org.godot.node.CanvasItem) joyAxes.getNode(idx + "-");
                if (plus != null) plus.show();
                if (minus != null) minus.show();
            }
        } else if (key.equals("lefttrigger") || key.equals("righttrigger")) {
            if (joyAxes != null) {
                org.godot.node.CanvasItem node = (org.godot.node.CanvasItem) joyAxes.getNode(String.valueOf(idx));
                if (node != null) node.show();
            }
        } else {
            if (joyButtons != null) {
                org.godot.node.CanvasItem node = (org.godot.node.CanvasItem) joyButtons.getNode(String.valueOf(idx));
                if (node != null) node.show();
            }
        }

        if (joyMappingFullAxis != null) {
            joyMappingFullAxis.setProperty("button_pressed",
                key.equals("leftx") || key.equals("lefty") || key.equals("rightx") || key.equals("righty") ||
                key.equals("righttrigger") || key.equals("lefttrigger"));
        }
        if (joyMappingAxisInvert != null) {
            joyMappingAxisInvert.setProperty("button_pressed", false);
        }
    }

    private String createMappingString(Map<String, Object> mapping) {
        String result = joyGuid + "," + joyName + ",";
        for (Map.Entry<String, Object> entry : mapping.entrySet() ) {
            String val = String.valueOf(entry.getValue());
            if (!val.isEmpty() ) {
                result += entry.getKey() + ":" + val + ",";
            }
        }
        String platform = "Unknown";
        String osName = (String) org.godot.singleton.OS.singleton().call("get_name");
        Map<String, String> platforms = new HashMap<>();
        platforms.put("Windows", "Windows");
        platforms.put("macOS", "Mac OS X");
        platforms.put("Linux", "Linux");
        platforms.put("Android", "Android");
        platforms.put("iOS", "iOS");
        if (platforms.containsKey(osName)) platform = platforms.get(osName);

        return result + "platform:" + platform;
    }

    private void remapAndClose(Map<String, Object> mapping) {
        lastMapping = createMappingString(mapping);
        org.godot.singleton.Input.singleton().addJoyMapping(lastMapping, true);
        reset();
        showMap();
    }

    private void reset() {
        org.godot.node.Node startWin = getNode("Start");
        org.godot.node.Node mappingWin = getNode("Mapping");
        if (startWin != null) startWin.call("hide");
        if (mappingWin != null) mappingWin.call("hide");
        joyGuid = "";
        joyName = "";
        curMapping.clear();
        curStep = -1;
    }

    @GodotMethod
    public void showMap() {
        org.godot.singleton.OS os = org.godot.singleton.OS.singleton();
        if ((boolean) os.call("has_feature", "web")) {
            // Web: use JavaScriptBridge
            call("JavaScriptBridge.eval", "window.prompt('This is the resulting remap string', '" + lastMapping + "')");
        } else {
            org.godot.node.Node mapWin = getNode("MapWindow");
            if (mapWin != null) {
                org.godot.Godot textEdit = (org.godot.Godot) mapWin.getNode("Margin/VBoxContainer/TextEdit");
                if (textEdit != null) textEdit.setProperty("text", lastMapping);
                mapWin.call("popup_centered");
            }
        }
    }

    private Map<String, Object> getXboxMapping() {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("a", "b0"); map.put("b", "b1"); map.put("y", "b3"); map.put("x", "b2");
        map.put("start", "b7"); map.put("guide", "b8"); map.put("back", "b6");
        map.put("leftstick", "b9"); map.put("rightstick", "b10");
        map.put("leftshoulder", "b4"); map.put("rightshoulder", "b5");
        map.put("dpup", "-a7"); map.put("dpleft", "-a6"); map.put("dpdown", "+a7"); map.put("dpright", "+a6");
        map.put("leftx", "a0"); map.put("lefty", "a1"); map.put("rightx", "a3"); map.put("righty", "a4");
        map.put("lefttrigger", "a2"); map.put("righttrigger", "a5");
        return map;
    }

    private Map<String, Object> getXboxOsxMapping() {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("a", "b11"); map.put("b", "b12"); map.put("y", "b14"); map.put("x", "b13");
        map.put("start", "b4"); map.put("back", "b5");
        map.put("leftstick", "b6"); map.put("rightstick", "b7");
        map.put("leftshoulder", "b8"); map.put("rightshoulder", "b9");
        map.put("dpup", "b0"); map.put("dpleft", "b2"); map.put("dpdown", "b1"); map.put("dpright", "b3");
        map.put("leftx", "a0"); map.put("lefty", "a1"); map.put("rightx", "a2"); map.put("righty", "a3");
        map.put("lefttrigger", "a4"); map.put("righttrigger", "a5");
        return map;
    }
}
