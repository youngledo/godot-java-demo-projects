package demos.threed.voxel;

import org.godot.annotation.GodotClass;
import org.godot.collection.GodotDictionary;
import org.godot.node.FileAccess;
import org.godot.node.JSON;
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

        if (FileAccess.fileExists(savePath)) {
            FileAccess file = FileAccess.open(savePath, 1);
            if (file == null) return;

            String text = file.getAsText();
            file.close();

            JSON json = JSON.create();
            json.parse(text);
            if (json.getData() instanceof GodotDictionary data) {
                Object rd = data.get("render_distance");
                Object fe = data.get("fog_enabled");
                if (rd instanceof Number number) renderDistance = number.intValue();
                if (fe instanceof Boolean value) fogEnabled = value;
            }
        } else {
            saveSettings();
        }
    }

    public void saveSettings() {
        FileAccess file = FileAccess.open(savePath, 2);
        if (file == null) return;

        String jsonStr = "{\"render_distance\":" + renderDistance + ",\"fog_enabled\":" + fogEnabled + "}";
        file.storeLine(jsonStr);
        file.close();
    }
}
