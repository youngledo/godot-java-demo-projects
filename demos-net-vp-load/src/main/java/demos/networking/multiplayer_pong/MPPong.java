package demos.networking.multiplayer_pong;

import org.godot.Godot;
import org.godot.annotation.GodotClass;
import org.godot.annotation.GodotMethod;
import org.godot.node.Node2D;
import org.godot.node.Node;

@GodotClass(name = "MPPong", parent = "Node2D")
public class MPPong extends Node2D {

    private static final int SCORE_TO_WIN = 10;

    private int scoreLeft = 0;
    private int scoreRight = 0;

    private Godot player2;
    private Godot scoreLeftNode;
    private Godot scoreRightNode;
    private Godot winnerLeft;
    private Godot winnerRight;

    @Override
    public void _ready() {
        player2 = (Godot) getNode("Player2");
        scoreLeftNode = (Godot) getNode("ScoreLeft");
        scoreRightNode = (Godot) getNode("ScoreRight");
        winnerLeft = (Godot) getNode("WinnerLeft");
        winnerRight = (Godot) getNode("WinnerRight");

        Godot mp = (Godot) getMultiplayer();
        if ((boolean) mp.call("is_server")) {
            Object peers = mp.call("get_peers");
            // Get first peer - peers is an array/dictionary
            Object[] peerArray = (Object[]) peers;
            if (peerArray.length > 0) {
                long firstPeer = (long) peerArray[0];
                player2.call("set_multiplayer_authority", (int) firstPeer);
            }
        } else {
            long uniqueId = (long) mp.call("get_unique_id");
            player2.call("set_multiplayer_authority", (int) uniqueId);
        }

        System.out.println("Unique id: " + mp.call("get_unique_id"));
    }

    @GodotMethod
    public void updateScore(boolean addToLeft) {
        if (addToLeft) {
            scoreLeft += 1;
            scoreLeftNode.call("set_text", String.valueOf(scoreLeft));
        } else {
            scoreRight += 1;
            scoreRightNode.call("set_text", String.valueOf(scoreRight));
        }

        boolean gameEnded = false;
        if (scoreLeft == SCORE_TO_WIN) {
            winnerLeft.call("show");
            gameEnded = true;
        } else if (scoreRight == SCORE_TO_WIN) {
            winnerRight.call("show");
            gameEnded = true;
        }

        if (gameEnded) {
            ((Godot) getNode("ExitGame")).call("show");
            Godot ball = (Godot) getNode("Ball");
            ball.call("rpc", "stop");
        }
    }

    @GodotMethod
    public void OnExitGamePressed() {
        emitSignal("game_finished");
    }
}
