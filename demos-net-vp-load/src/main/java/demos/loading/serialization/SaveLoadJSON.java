package demos.loading.serialization;

import org.godot.annotation.GodotClass;
import org.godot.Godot;
import org.godot.node.Button;
import org.godot.node.FileAccess;

/**
 * Saves and loads game data using the JSON file format.
 * Since JSON doesn't support Godot types like Vector2 natively,
 * var_to_str/str_to_var are used for conversion.
 */
@GodotClass(name = "SaveLoadJSON", parent = "Button")
public class SaveLoadJSON extends Button {

    private static final String SAVE_PATH = "user://save_json.json";

    public void saveGame() {
        // Open file for writing (FileAccess.WRITE = 2).
        FileAccess fileObj = FileAccess.open(SAVE_PATH, 2);
        if (fileObj == null) return;
        FileAccess file = fileObj;

        String playerPath = (String) getProperty("player_node");
        SerPlayer player = (SerPlayer) getNode(playerPath);
        Godot sprite = player.getSprite();

        // Build save dictionary. JSON doesn't support Vector2, so use var_to_str.
        Godot saveDict = (Godot) call("Dictionary.new");

        Godot playerDict = (Godot) call("Dictionary.new");
        playerDict.setProperty("position", call("var_to_str", player.getProperty("position")));
        playerDict.setProperty("health", call("var_to_str", player.getHealth()));
        if (sprite != null) {
            playerDict.setProperty("rotation", call("var_to_str", sprite.getProperty("rotation")));
        }
        saveDict.setProperty("player", playerDict);

        Godot enemiesArray = (Godot) call("Array.new");
        saveDict.setProperty("enemies", enemiesArray);

        Godot tree = (Godot) getTree();
        Object[] enemies = (Object[]) tree.call("get_nodes_in_group", "enemy");
        for (Object enemyObj : enemies) {
            if (enemyObj instanceof Godot) {
                Godot enemy = (Godot) enemyObj;
                Godot dict = (Godot) call("Dictionary.new");
                dict.setProperty("position", call("var_to_str", enemy.getProperty("position")));
                enemiesArray.call("push_back", dict);
            }
        }

        // Write JSON to file.
        String jsonString = (String) call("JSON.stringify", saveDict);
        file.storeLine(jsonString);

        // Enable the load button.
        Godot loadBtn = (Godot) getNode("../LoadJSON");
        if (loadBtn != null) loadBtn.setProperty("disabled", false);
    }

    public void loadGame() {
        // Open file for reading (FileAccess.READ = 1).
        FileAccess fileObj = FileAccess.open(SAVE_PATH, 1);
        if (fileObj == null) return;
        FileAccess file = fileObj;

        Godot json = (Godot) call("JSON.new");
        String line = file.getLine();
        json.call("parse", line);
        Godot saveDict = (Godot) json.call("get_data");

        String playerPath = (String) getProperty("player_node");
        SerPlayer player = (SerPlayer) getNode(playerPath);
        Godot sprite = player.getSprite();

        // Restore player data using str_to_var for type conversion.
        Godot playerDict = (Godot) saveDict.getProperty("player");
        Object posObj = call("str_to_var", playerDict.getProperty("position"));
        player.setProperty("position", posObj);
        Object healthObj = call("str_to_var", playerDict.getProperty("health"));
        if (healthObj instanceof Number) {
            player.setHealth(((Number) healthObj).doubleValue());
        }
        if (sprite != null) {
            Object rotObj = call("str_to_var", playerDict.getProperty("rotation"));
            sprite.setProperty("rotation", rotObj);
        }

        // Remove existing enemies.
        Godot tree = (Godot) getTree();
        tree.call("call_group", "enemy", "queue_free");

        // Load enemies.
        String gamePath = (String) getProperty("game_node");
        Godot game = (Godot) getNode(gamePath);
        Godot enemiesArray = (Godot) saveDict.getProperty("enemies");

        if (enemiesArray != null && game != null) {
            int count = (int) enemiesArray.call("size");
            for (int i = 0; i < count; i++) {
                Godot enemyConfig = (Godot) enemiesArray.call("get", i);
                org.godot.node.PackedScene enemyScene = (org.godot.node.PackedScene) org.godot.singleton.ResourceLoader.singleton().load("res://enemy.tscn");
                if (enemyScene != null) {
                    Godot enemy = (Godot) ((Godot) enemyScene).call("instantiate");
                    Object enemyPos = call("str_to_var", enemyConfig.getProperty("position"));
                    enemy.setProperty("position", enemyPos);
                    game.call("add_child", enemy);
                }
            }
        }
    }
}
