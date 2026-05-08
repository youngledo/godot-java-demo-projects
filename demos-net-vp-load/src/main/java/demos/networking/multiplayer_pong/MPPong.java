package demos.networking.multiplayer_pong;

import org.godot.Godot;
import org.godot.annotation.GodotClass;
import org.godot.annotation.GodotMethod;
import org.godot.node.Button;
import org.godot.node.Label;
import org.godot.node.MultiplayerAPI;
import org.godot.node.Node;
import org.godot.node.Node2D;

@GodotClass(name = "MPPong", parent = "Node2D")
public class MPPong extends Node2D {

    private static final int SCORE_TO_WIN = 10;

    private int scoreLeft = 0;
    private int scoreRight = 0;

    private Node player2;
    private Label scoreLeftNode;
    private Label scoreRightNode;
    private Node winnerLeft;
    private Node winnerRight;

    @Override
    public void _ready() {
        player2 = getNode("Player2");
        scoreLeftNode = getNodeAs("ScoreLeft", Label.class);
        scoreRightNode = getNodeAs("ScoreRight", Label.class);
        winnerLeft = getNode("WinnerLeft");
        winnerRight = getNode("WinnerRight");

        MultiplayerAPI mp = getMultiplayer();
        if (mp.isServer()) {
            int[] peers = mp.getPeers();
            if (peers.length > 0) {
                player2.setMultiplayerAuthority(peers[0]);
            }
        } else {
            int uniqueId = mp.getUniqueId();
            player2.setMultiplayerAuthority(uniqueId);
        }

        System.out.println("Unique id: " + mp.getUniqueId());
    }

    @GodotMethod
    public void updateScore(boolean addToLeft) {
        if (addToLeft) {
            scoreLeft += 1;
            scoreLeftNode.setText(String.valueOf(scoreLeft));
        } else {
            scoreRight += 1;
            scoreRightNode.setText(String.valueOf(scoreRight));
        }

        boolean gameEnded = false;
        if (scoreLeft == SCORE_TO_WIN) {
            winnerLeft.setProperty("visible", true);
            gameEnded = true;
        } else if (scoreRight == SCORE_TO_WIN) {
            winnerRight.setProperty("visible", true);
            gameEnded = true;
        }

        if (gameEnded) {
            getNode("ExitGame").setProperty("visible", true);
            Godot ball = (Godot) getNode("Ball");
            ball.call("rpc", "stop");
        }
    }

    @GodotMethod
    public void OnExitGamePressed() {
        emitSignal("game_finished");
    }
}
