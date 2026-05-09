package demos.misc.joypads;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import org.godot.annotation.GodotClass;
import org.godot.annotation.GodotMethod;
import org.godot.node.BaseButton;
import org.godot.node.CanvasItem;
import org.godot.node.InputEventJoypadButton;
import org.godot.node.InputEventJoypadMotion;
import org.godot.node.Label;
import org.godot.node.Node;
import org.godot.node.TextEdit;
import org.godot.node.Window;
import org.godot.singleton.Input;
import org.godot.singleton.JavaScriptBridge;
import org.godot.singleton.OS;

@GodotClass(name = "RemapWizard", parent = "Node")
public class RemapWizard extends Node {

    private static final double DEADZONE = 0.3;

    private int joyIndex = -1;
    private String joyGuid = "";
    private String joyName = "";
    private int curStep = -1;
    private Map<String, Object> curMapping = new LinkedHashMap<>();
    private String lastMapping = "";

    private Node joyButtons;
    private Node joyAxes;
    private Label joyMappingText;
    private BaseButton joyMappingFullAxis;
    private BaseButton joyMappingAxisInvert;

    private boolean initialized = false;

    @Override
    public void _ready() {
        if (initialized) return;
        initialized = true;

        joyButtons = getNode("Mapping/Margin/VBox/SubViewportContainer/SubViewport/JoypadDiagram/Buttons");
        joyAxes = getNode("Mapping/Margin/VBox/SubViewportContainer/SubViewport/JoypadDiagram/Axes");
        joyMappingText = getNodeAs("Mapping/Margin/VBox/Info/Text/Value", Label.class);
        joyMappingFullAxis = getNodeAs("Mapping/Margin/VBox/Info/Extra/FullAxis", BaseButton.class);
        joyMappingAxisInvert = getNodeAs("Mapping/Margin/VBox/Info/Extra/InvertAxis", BaseButton.class);
    }

    @Override
    public boolean _input(Object inputEvent) {
        if (curStep == -1) return false;

        String[] steps = getBaseKeys();

        if (inputEvent instanceof InputEventJoypadMotion event) {
            if (event.getDevice() != joyIndex) return false;
            getViewport().setInputAsHandled();
            double axisValue = event.getAxisValue();
            long axisIdx = event.getAxis();

            if (Math.abs(axisValue) > DEADZONE && joyMappingText != null) {
                boolean isInverted = joyMappingAxisInvert != null && joyMappingAxisInvert.isButtonPressed();
                boolean isFullAxis = joyMappingFullAxis != null && joyMappingFullAxis.isButtonPressed();

                String prefix = "";
                if (!isFullAxis) {
                    prefix = axisValue > 0 ? "+" : "-";
                }
                String suffix = isInverted ? "~" : "";
                String mapString = prefix + "a" + axisIdx + suffix;
                String humanString = "Axis " + prefix + axisIdx + suffix;

                joyMappingText.setText(humanString);
                curMapping.put(steps[curStep], mapString);
            }
        } else if (inputEvent instanceof InputEventJoypadButton event) {
            if (event.getDevice() != joyIndex) return false;
            if (event.isPressed()) {
                getViewport().setInputAsHandled();
                long buttonIndex = event.getButtonIndex();
                String mapString = "b" + buttonIndex;
                if (joyMappingText != null) {
                    joyMappingText.setText("Button " + buttonIndex);
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
        joyGuid = Input.singleton().getJoyGuid(idx);
        joyName = Input.singleton().getJoyName(idx);
        if (joyGuid == null || joyGuid.isEmpty()) {
            System.err.println("Unable to find controller");
            return;
        }
        if (OS.singleton().hasFeature("web")) {
            Window startWin = getNodeAs("Start", Window.class);
            if (startWin != null) {
                startWin.setTitle(joyGuid + " - " + joyName);
                startWin.popupCentered();
            }
        } else {
            OnWizardPressed();
        }
    }

    @GodotMethod
    public void OnWizardPressed() {
        Input.singleton().removeJoyMapping(joyGuid);
        Window startWin = getNodeAs("Start", Window.class);
        if (startWin != null) startWin.hide();
        Window mappingWin = getNodeAs("Mapping", Window.class);
        if (mappingWin != null) mappingWin.popupCentered();
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
        Window startWin = getNodeAs("Start", Window.class);
        if (startWin != null) startWin.hide();
    }

    @GodotMethod
    public void OnMappingCloseRequested() {
        Window mappingWin = getNodeAs("Mapping", Window.class);
        if (mappingWin != null) mappingWin.hide();
    }

    @GodotMethod
    public void OnMapWindowCloseRequested() {
        Window mapWin = getNodeAs("MapWindow", Window.class);
        if (mapWin != null) mapWin.hide();
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
        Window mappingWin = getNodeAs("Mapping", Window.class);
        String[] steps = getBaseKeys();
        if (mappingWin != null) {
            mappingWin.setTitle("Step: " + (curStep + 1) + "/" + steps.length);
        }
        if (joyMappingText != null) joyMappingText.setText("");

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

        if (joyButtons != null) {
            for (Node child : joyButtons.getChildren()) {
                if (child instanceof CanvasItem item) item.hide();
            }
        }
        if (joyAxes != null) {
            for (Node child : joyAxes.getChildren()) {
                if (child instanceof CanvasItem item) item.hide();
            }
        }

        if (key.equals("leftx") || key.equals("lefty") || key.equals("rightx") || key.equals("righty")) {
            if (joyAxes != null) {
                CanvasItem plus = joyAxes.getNodeAs(idx + "+", CanvasItem.class);
                CanvasItem minus = joyAxes.getNodeAs(idx + "-", CanvasItem.class);
                if (plus != null) plus.show();
                if (minus != null) minus.show();
            }
        } else if (key.equals("lefttrigger") || key.equals("righttrigger")) {
            if (joyAxes != null) {
                CanvasItem node = joyAxes.getNodeAs(String.valueOf(idx), CanvasItem.class);
                if (node != null) node.show();
            }
        } else {
            if (joyButtons != null) {
                CanvasItem node = joyButtons.getNodeAs(String.valueOf(idx), CanvasItem.class);
                if (node != null) node.show();
            }
        }

        if (joyMappingFullAxis != null) {
            joyMappingFullAxis.setButtonPressed(
                key.equals("leftx") || key.equals("lefty") || key.equals("rightx") || key.equals("righty") ||
                key.equals("righttrigger") || key.equals("lefttrigger"));
        }
        if (joyMappingAxisInvert != null) {
            joyMappingAxisInvert.setButtonPressed(false);
        }
    }

    private String createMappingString(Map<String, Object> mapping) {
        String result = joyGuid + "," + joyName + ",";
        for (Map.Entry<String, Object> entry : mapping.entrySet()) {
            String val = String.valueOf(entry.getValue());
            if (!val.isEmpty()) {
                result += entry.getKey() + ":" + val + ",";
            }
        }
        String platform = "Unknown";
        String osName = OS.singleton().getName();
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
        Input.singleton().addJoyMapping(lastMapping, true);
        reset();
        showMap();
    }

    private void reset() {
        Window startWin = getNodeAs("Start", Window.class);
        Window mappingWin = getNodeAs("Mapping", Window.class);
        if (startWin != null) startWin.hide();
        if (mappingWin != null) mappingWin.hide();
        joyGuid = "";
        joyName = "";
        curMapping.clear();
        curStep = -1;
    }

    @GodotMethod
    public void showMap() {
        if (OS.singleton().hasFeature("web")) {
            JavaScriptBridge.singleton().eval("window.prompt('This is the resulting remap string', '" + lastMapping + "')");
        } else {
            Window mapWin = getNodeAs("MapWindow", Window.class);
            if (mapWin != null) {
                TextEdit textEdit = mapWin.getNodeAs("Margin/VBoxContainer/TextEdit", TextEdit.class);
                if (textEdit != null) textEdit.setText(lastMapping);
                mapWin.popupCentered();
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
