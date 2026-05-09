package demos.networking.multiplayer_bomber;

import org.godot.Godot;
import org.godot.annotation.GodotClass;
import org.godot.annotation.GodotMethod;
import org.godot.node.Button;
import org.godot.node.HBoxContainer;
import org.godot.node.Label;
import org.godot.node.Font;
import org.godot.node.Node;
import org.godot.singleton.ResourceLoader;

import java.util.HashMap;
import java.util.Map;

@GodotClass(name = "MPBomberScore", parent = "HBoxContainer")
public class MPBomberScore extends HBoxContainer {

    private Map<Integer, PlayerLabel> playerLabels = new HashMap<>();

    private static class PlayerLabel {
        String name;
        Label label;
        int score;
    }

    @Override
    public void _ready() {
        Label winner = getNodeAs("../Winner", Label.class);
        winner.setVisible(false);

        Button exitBtn = getNodeAs("../Winner/ExitGame", Button.class);
        if (exitBtn != null) {
            exitBtn.connect("pressed", new org.godot.core.Callable(this, "_on_exit_game_pressed"), 0);
        }
    }

    @Override
    public void _process(double delta) {
        Node rocks = getNodeAs("../Rocks", Node.class);
        long rocksLeft = rocks.getChildCount();
        if (rocksLeft == 0) {
            String winnerName = "";
            int winnerScore = 0;
            for (Map.Entry<Integer, PlayerLabel> entry : playerLabels.entrySet() ) {
                if (entry.getValue().score > winnerScore) {
                    winnerScore = entry.getValue().score;
                    winnerName = entry.getValue().name;
                }
            }
            Label winner = getNodeAs("../Winner", Label.class);
            winner.setText("THE WINNER IS:\n" + winnerName);
            winner.setVisible(true);
        }
    }

    @GodotMethod
    public void increaseScore(int forWho) {
        assert playerLabels.containsKey(forWho);

        PlayerLabel pl = playerLabels.get(forWho);
        pl.score += 1;
        pl.label.setText(pl.name + "\n" + pl.score);
    }

    @GodotMethod
    public void addPlayer(int id, String newPlayerName) {
        Label label = new Label();
        label.setHorizontalAlignment(1);
        label.setText(newPlayerName + "\n0");

        MPBomberGameState gamestate = getNodeAs("/root/gamestate", MPBomberGameState.class);
        Godot color = (Godot) gamestate.getPlayerColor(newPlayerName);
        label.setProperty("modulate", color);
        label.setProperty("size_flags_horizontal", 3);

        Font font = (Font) ResourceLoader.singleton().load("res://montserrat.otf");
        label.addThemeFontOverride("font", font);
        label.addThemeColorOverride("font_outline_color", new org.godot.math.Color(0, 0, 0));
        label.addThemeConstantOverride("outline_size", 9);
        label.addThemeFontSizeOverride("font_size", 18);

        addChild(label);

        PlayerLabel pl = new PlayerLabel();
        pl.name = newPlayerName;
        pl.label = label;
        pl.score = 0;
        playerLabels.put(id, pl);
    }

    @GodotMethod
    public void OnExitGamePressed() {
        MPBomberGameState gamestate = getNodeAs("/root/gamestate", MPBomberGameState.class);
        gamestate.endGame();
    }
}
