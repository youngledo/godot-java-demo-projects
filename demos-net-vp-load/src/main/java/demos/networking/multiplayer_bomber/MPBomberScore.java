package demos.networking.multiplayer_bomber;

import org.godot.Godot;
import org.godot.annotation.GodotClass;
import org.godot.annotation.GodotMethod;
import org.godot.node.HBoxContainer;

import java.util.HashMap;
import java.util.Map;

@GodotClass(name = "MPBomberScore", parent = "HBoxContainer")
public class MPBomberScore extends HBoxContainer {

    private Map<Integer, PlayerLabel> playerLabels = new HashMap<>();

    private static class PlayerLabel {
        String name;
        Godot label;
        int score;
    }

    @Override
    public void _ready() {
        Godot winner = (Godot) call("get_node", "../Winner");
        winner.call("hide");

        // Connect ExitGame button signal (moved from .tscn [connection] line)
        Godot exitBtn = (Godot) call("get_node", "../Winner/ExitGame");
        if (exitBtn != null) {
            exitBtn.call("connect", "pressed", new org.godot.core.Callable(this, "_on_exit_game_pressed"));
        }
    }

    @Override
    public void _process(double delta) {
        Godot rocks = (Godot) call("get_node", "../Rocks");
        long rocksLeft = (long) rocks.call("get_child_count");
        if (rocksLeft == 0) {
            String winnerName = "";
            int winnerScore = 0;
            for (Map.Entry<Integer, PlayerLabel> entry : playerLabels.entrySet()) {
                if (entry.getValue().score > winnerScore) {
                    winnerScore = entry.getValue().score;
                    winnerName = entry.getValue().name;
                }
            }
            Godot winner = (Godot) call("get_node", "../Winner");
            winner.call("set_text", "THE WINNER IS:\n" + winnerName);
            winner.call("show");
        }
    }

    @GodotMethod
    public void increase_score(int forWho) {
        assert playerLabels.containsKey(forWho);

        PlayerLabel pl = playerLabels.get(forWho);
        pl.score += 1;
        pl.label.call("set_text", pl.name + "\n" + pl.score);
    }

    @GodotMethod
    public void add_player(int id, String newPlayerName) {
        Godot label = (Godot) call("Label.new");
        label.setProperty("horizontal_alignment", 1);
        label.setProperty("text", newPlayerName + "\n0");

        Godot gamestate = (Godot) call("get_node", "/root/gamestate");
        Godot color = (Godot) gamestate.call("get_player_color", newPlayerName);
        label.setProperty("modulate", color);
        label.setProperty("size_flags_horizontal", 3);

        Godot font = (Godot) call("preload", "res://montserrat.otf");
        label.call("add_theme_font_override", "font", font);
        label.call("add_theme_color_override", "font_outline_color", call("Color", 0, 0, 0));
        label.call("add_theme_constant_override", "outline_size", 9);
        label.call("add_theme_font_size_override", "font_size", 18);

        call("add_child", label, false, 0);

        PlayerLabel pl = new PlayerLabel();
        pl.name = newPlayerName;
        pl.label = label;
        pl.score = 0;
        playerLabels.put(id, pl);
    }

    @GodotMethod
    public void _on_exit_game_pressed() {
        Godot gamestate = (Godot) call("get_node", "/root/gamestate");
        gamestate.call("end_game");
    }
}
