package demos.loading.serialization;

import org.godot.annotation.GodotClass;
import org.godot.annotation.GodotMethod;
import org.godot.node.VBoxContainer;
import org.godot.Godot;

/**
 * GUI container that initializes save/load button states and provides
 * the "Open User Data Folder" functionality.
 */
@GodotClass(name = "Gui", parent = "VBoxContainer")
public class Gui extends VBoxContainer {

    @Override
    public void _ready() {
        // Don't allow loading files that don't exist yet.
        Godot loadConfigBtn = (Godot) call("get_node", "SaveLoad/LoadConfigFile");
        if (loadConfigBtn != null) {
            boolean exists = (boolean) call("FileAccess.file_exists", "user://save_config_file.ini");
            loadConfigBtn.setProperty("disabled", !exists);
        }

        Godot loadJsonBtn = (Godot) call("get_node", "SaveLoad/LoadJSON");
        if (loadJsonBtn != null) {
            boolean exists = (boolean) call("FileAccess.file_exists", "user://save_json.json");
            loadJsonBtn.setProperty("disabled", !exists);
        }
    }

    @GodotMethod
    public void _onOpenUserDataFolderPressed() {
        String globalPath = (String) call("ProjectSettings.globalize_path", "user://");
        call("OS.shell_open", globalPath);
    }
}
