package demos.loading.serialization;

import org.godot.Godot;
import org.godot.annotation.GodotClass;
import org.godot.collection.GodotArray;
import org.godot.collection.GodotDictionary;
import org.godot.node.Button;
import org.godot.node.ConfigFile;
import org.godot.node.Node;
import org.godot.node.Node2D;
import org.godot.node.PackedScene;
import org.godot.node.SceneTree;
import org.godot.singleton.ResourceLoader;

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
        ConfigFile config = ConfigFile.create();

        String playerPath = (String) getProperty("player_node");
        SerPlayer player = (SerPlayer) getNode(playerPath);

        config.setValue("player", "position", player.getPosition());
        config.setValue("player", "health", player.getHealth());
        Godot sprite = player.getSprite();
        if (sprite != null) {
            config.setValue("player", "rotation", sprite.getProperty("rotation"));
        }

        SceneTree tree = getTree();
        Node[] enemies = tree.getNodesInGroup("enemy");

        GodotArray enemyArray = new GodotArray();
        for (Node enemyNode : enemies) {
            if (enemyNode instanceof Node2D enemy) {
                GodotDictionary dict = new GodotDictionary();
                dict.put("position", enemy.getPosition());
                enemyArray.add(dict);
            }
        }
        config.setValue("enemies", "enemies", enemyArray);

        config.save(SAVE_PATH);

        Button loadBtn = getNodeAs("../LoadConfigFile", Button.class);
        if (loadBtn != null) loadBtn.setDisabled(false);
    }

    public void loadGame() {
        ConfigFile config = ConfigFile.create();
        config.load(SAVE_PATH);

        String playerPath = (String) getProperty("player_node");
        SerPlayer player = (SerPlayer) getNode(playerPath);

        player.setPosition((org.godot.math.Vector2) config.getValue("player", "position"));
        player.setHealth(((Number) config.getValue("player", "health")).doubleValue());
        Godot sprite = player.getSprite();
        if (sprite != null) {
            sprite.setProperty("rotation", config.getValue("player", "rotation"));
        }

        SceneTree tree = getTree();
        tree.callGroup("enemy", "queue_free");

        Object enemiesObj = config.getValue("enemies", "enemies");
        String gamePath = (String) getProperty("game_node");
        Node game = getNode(gamePath);

        if (enemiesObj instanceof GodotArray enemies && game != null) {
            int count = enemies.size();
            for (int i = 0; i < count; i++) {
                GodotDictionary enemyConfig = (GodotDictionary) enemies.get(i);
                PackedScene enemyScene = (PackedScene) ResourceLoader.singleton().load("res://enemy.tscn");
                if (enemyScene != null) {
                    Node enemy = enemyScene.instantiate();
                    enemy.setProperty("position", enemyConfig.get("position"));
                    game.addChild(enemy);
                }
            }
        }
    }
}
