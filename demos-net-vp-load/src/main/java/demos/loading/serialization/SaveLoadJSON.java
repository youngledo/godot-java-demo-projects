package demos.loading.serialization;

import org.godot.Godot;
import org.godot.annotation.GodotClass;
import org.godot.collection.GodotArray;
import org.godot.collection.GodotDictionary;
import org.godot.node.Button;
import org.godot.node.FileAccess;
import org.godot.node.JSON;
import org.godot.node.Node;
import org.godot.node.Node2D;
import org.godot.node.PackedScene;
import org.godot.node.SceneTree;
import org.godot.singleton.ResourceLoader;

/**
 * Saves and loads game data using the JSON file format.
 * Since JSON doesn't support Godot types like Vector2 natively,
 * var_to_str/str_to_var are used for conversion.
 */
@GodotClass(name = "SaveLoadJSON", parent = "Button")
public class SaveLoadJSON extends Button {

    private static final String SAVE_PATH = "user://save_json.json";

    public void saveGame() {
        FileAccess file = FileAccess.open(SAVE_PATH, 2);
        if (file == null) return;

        String playerPath = (String) getProperty("player_node");
        SerPlayer player = (SerPlayer) getNode(playerPath);
        Godot sprite = player.getSprite();

        GodotDictionary saveDict = new GodotDictionary();

        GodotDictionary playerDict = new GodotDictionary();
        playerDict.put("position", varToStr(player.getPosition()));
        playerDict.put("health", varToStr(player.getHealth()));
        if (sprite != null) {
            playerDict.put("rotation", varToStr(sprite.getProperty("rotation")));
        }
        saveDict.put("player", playerDict);

        GodotArray enemiesArray = new GodotArray();
        saveDict.put("enemies", enemiesArray);

        SceneTree tree = getTree();
        Node[] enemies = tree.getNodesInGroup("enemy");
        for (Node enemyNode : enemies) {
            if (enemyNode instanceof Node2D enemy) {
                GodotDictionary dict = new GodotDictionary();
                dict.put("position", varToStr(enemy.getPosition()));
                enemiesArray.add(dict);
            }
        }

        String jsonString = JSON.stringify(saveDict);
        file.storeLine(jsonString);

        Button loadBtn = getNodeAs("../LoadJSON", Button.class);
        if (loadBtn != null) loadBtn.setDisabled(false);
    }

    public void loadGame() {
        FileAccess file = FileAccess.open(SAVE_PATH, 1);
        if (file == null) return;

        JSON json = JSON.create();
        String line = file.getLine();
        json.parse(line);
        GodotDictionary saveDict = (GodotDictionary) json.getData();

        String playerPath = (String) getProperty("player_node");
        SerPlayer player = (SerPlayer) getNode(playerPath);
        Godot sprite = player.getSprite();

        GodotDictionary playerDict = (GodotDictionary) saveDict.get("player");
        Object posObj = strToVar((String) playerDict.get("position"));
        player.setPosition((org.godot.math.Vector2) posObj);
        Object healthObj = strToVar((String) playerDict.get("health"));
        if (healthObj instanceof Number) {
            player.setHealth(((Number) healthObj).doubleValue());
        }
        if (sprite != null) {
            Object rotObj = strToVar((String) playerDict.get("rotation"));
            sprite.setProperty("rotation", rotObj);
        }

        SceneTree tree = getTree();
        tree.callGroup("enemy", "queue_free");

        String gamePath = (String) getProperty("game_node");
        Node game = getNode(gamePath);
        GodotArray enemiesArray = (GodotArray) saveDict.get("enemies");

        if (enemiesArray != null && game != null) {
            int count = enemiesArray.size();
            for (int i = 0; i < count; i++) {
                GodotDictionary enemyConfig = (GodotDictionary) enemiesArray.get(i);
                PackedScene enemyScene = (PackedScene) ResourceLoader.singleton().load("res://enemy.tscn");
                if (enemyScene != null) {
                    Node enemy = enemyScene.instantiate();
                    Object enemyPos = strToVar((String) enemyConfig.get("position"));
                    enemy.setProperty("position", enemyPos);
                    game.addChild(enemy);
                }
            }
        }
    }
}
