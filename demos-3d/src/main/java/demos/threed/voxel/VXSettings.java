package demos.threed.voxel;

import org.godot.annotation.GodotClass;
import org.godot.node.Node;

@GodotClass(name = "VXSettings", parent = "Node")
public class VXSettings extends Node {

    public int renderDistance = 7;
    public boolean fogEnabled = true;
    public double fogDistance = 32.0;
    public int worldType = 0;

    private String savePath = "user://settings.json";
    private boolean initialized = false;

    @Override
    public void _enterTree() {
        if (initialized) return;
        initialized = true;

        Object fileExists = call("FileAccess.file_exists", savePath);
        boolean exists = fileExists instanceof Boolean && (Boolean) fileExists;

        if (exists) {
            Object file = call("FileAccess.open", savePath, 1); // READ
            if (file instanceof org.godot.Godot) {
                org.godot.Godot f = (org.godot.node.FileAccess) file;
                Object textObj = f.call("get_as_text");
                f.call("close");

                if (textObj != null) {
                    Object json = call("JSON.new");
                    if (json instanceof org.godot.Godot) {
                        org.godot.Godot j = (org.godot.node.JSON) json;
                        j.call("parse", textObj.toString());
                        Object dataObj = j.getProperty("data");
                        if (dataObj instanceof org.godot.Godot) {
                            org.godot.Godot data = (org.godot.Godot) dataObj;
                            Object rd = data.call("get", "render_distance");
                            Object fe = data.call("get", "fog_enabled");
                            if (rd instanceof Number) renderDistance = ((Number) rd).intValue();
                            if (fe instanceof Boolean) fogEnabled = (Boolean) fe;
                        }
                    }
                }
            }
        } else {
            saveSettings();
        }
    }

    public void saveSettings() {
        Object file = call("FileAccess.open", savePath, 2); // WRITE
        if (!(file instanceof org.godot.Godot)) return;

        org.godot.Godot f = (org.godot.node.FileAccess) file;
        String jsonStr = "{\"render_distance\":" + renderDistance + ",\"fog_enabled\":" + fogEnabled + "}";
        f.call("store_line", jsonStr);
        f.call("close");
    }
}
