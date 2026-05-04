package demos.loading.serialization;

import org.godot.annotation.GodotClass;
import org.godot.Godot;
import org.godot.node.Button;

/**
 * Saves and loads game data using Godot's custom ConfigFile format.
 * ConfigFile can store any Variant type.
 */
@GodotClass(name = "SaveLoadConfigFile", parent = "Button")
public class SaveLoadConfigFile extends Button {

    private static final String SAVE_PATH = "user://save_config_file.ini";

    // These are set via @export in GDScript - accessible as properties on the node.
    // game_node and player_node are NodePath properties set in the scene.

    public void saveGame() {
        Godot config = (Godot) call("ConfigFile.new");

        // Get the player node.
        String playerPath = (String) getProperty("player_node");
        Godot player = (Godot) call("get_node", playerPath);

        // Save player data.
        config.call("set_value", "player", "position", player.getProperty("position"));
        config.call("set_value", "player", "health", player.call("getHealth"));
        Godot sprite = (Godot) player.call("getSprite");
        if (sprite != null) {
            config.call("set_value", "player", "rotation", sprite.getProperty("rotation"));
        }

        // Save enemies.
        Godot tree = (Godot) call("get_tree");
        Object[] enemies = (Object[]) tree.call("get_nodes_in_group", "enemy");

        Godot enemyArray = (Godot) call("Array.new");
        for (Object enemyObj : enemies) {
            if (enemyObj instanceof Godot) {
                Godot enemy = (Godot) enemyObj;
                Godot dict = (Godot) call("Dictionary.new");
                dict.call("set", "position", enemy.getProperty("position"));
                enemyArray.call("push_back", dict);
            }
        }
        config.call("set_value", "enemies", "enemies", enemyArray);

        config.call("save", SAVE_PATH);

        // Enable the load button.
        Godot loadBtn = (Godot) call("get_node", "../LoadConfigFile");
        if (loadBtn != null) loadBtn.setProperty("disabled", false);
    }

    public void loadGame() {
        Godot config = (Godot) call("ConfigFile.new");
        config.call("load", SAVE_PATH);

        String playerPath = (String) getProperty("player_node");
        Godot player = (Godot) call("get_node", playerPath);

        // Restore player data.
        player.setProperty("position", config.call("get_value", "player", "position"));
        player.call("setHealth", config.call("get_value", "player", "health"));
        Godot sprite = (Godot) player.call("getSprite");
        if (sprite != null) {
            sprite.setProperty("rotation", config.call("get_value", "player", "rotation"));
        }

        // Remove existing enemies.
        Godot tree = (Godot) call("get_tree");
        tree.call("call_group", "enemy", "queue_free");

        // Load enemies.
        Object enemiesObj = config.call("get_value", "enemies", "enemies");
        String gamePath = (String) getProperty("game_node");
        Godot game = (Godot) call("get_node", gamePath);

        if (enemiesObj instanceof Godot && game != null) {
            Godot enemies = (Godot) enemiesObj;
            int count = (int) enemies.call("size");
            for (int i = 0; i < count; i++) {
                Godot enemyConfig = (Godot) enemies.call("get", i);
                Object enemyScene = call("load", "res://enemy.tscn");
                if (enemyScene != null) {
                    Godot enemy = (Godot) ((Godot) enemyScene).call("instantiate");
                    enemy.setProperty("position", enemyConfig.call("get", "position"));
                    game.call("add_child", enemy);
                }
            }
        }
    }
}
