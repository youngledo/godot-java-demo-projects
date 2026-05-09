package demos.loading.serialization;

import org.godot.annotation.GodotClass;
import org.godot.annotation.GodotMethod;
import org.godot.node.Button;
import org.godot.node.FileAccess;
import org.godot.node.VBoxContainer;
import org.godot.singleton.OS;
import org.godot.singleton.ProjectSettings;

/**
 * GUI container that initializes save/load button states and provides
 * the "Open User Data Folder" functionality.
 */
@GodotClass(name = "Gui", parent = "VBoxContainer")
public class Gui extends VBoxContainer {

    @Override
    public void _ready() {
        Button loadConfigBtn = getNodeAs("SaveLoad/LoadConfigFile", Button.class);
        if (loadConfigBtn != null) {
            boolean exists = FileAccess.fileExists("user://save_config_file.ini");
            loadConfigBtn.setDisabled(!exists);
        }

        Button loadJsonBtn = getNodeAs("SaveLoad/LoadJSON", Button.class);
        if (loadJsonBtn != null) {
            boolean exists = FileAccess.fileExists("user://save_json.json");
            loadJsonBtn.setDisabled(!exists);
        }
    }

    @GodotMethod
    public void _onOpenUserDataFolderPressed() {
        String globalPath = ProjectSettings.singleton().globalizePath("user://");
        OS.singleton().shellOpen(globalPath);
    }
}
