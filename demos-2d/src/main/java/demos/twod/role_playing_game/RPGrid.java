package demos.twod.role_playing_game;

import org.godot.annotation.GodotClass;
import org.godot.collection.GodotArray;
import org.godot.math.Vector2;
import org.godot.math.Vector2i;
import org.godot.node.Node;
import org.godot.node.TileMapLayer;

@GodotClass(name = "RPGrid", parent = "TileMapLayer")
public class RPGrid extends TileMapLayer {

    private RPDialogueUI dialogueUI;
    private boolean initialized = false;

    @Override
    public void _ready() {
        if (initialized) return;
        initialized = true;

        dialogueUI = (RPDialogueUI) getProperty("dialogue_ui");

        GodotArray<Node> children = getChildren();
        if (children != null) {
            for (int ci = 0; ci < children.size(); ci++) {
                Node child = children.get(ci);
                if (child instanceof RPPawn pawn) {
                    setCell(localToMap(pawn.getPosition()), pawn.getType(), new Vector2i(0, 0));
                }
            }
        }
    }

    public Vector2 requestMove(RPPawn pawn, Vector2i direction) {
        Vector2i startCell = localToMap(pawn.getPosition());
        Vector2i targetCell = new Vector2i(startCell.x + direction.x, startCell.y + direction.y);
        int cellTileId = getCellSourceId(targetCell);

        if (cellTileId == -1) {
            setCell(targetCell, RPPawn.CELL_TYPE_ACTOR, new Vector2i(0, 0));
            setCell(startCell, -1, new Vector2i(0, 0));
            return mapToLocal(targetCell);
        }

        if (cellTileId == RPPawn.CELL_TYPE_OBJECT || cellTileId == RPPawn.CELL_TYPE_ACTOR) {
            RPPawn targetPawn = getCellPawn(targetCell, cellTileId);
            if (targetPawn != null) {
                if (!targetPawn.hasNode("DialoguePlayer")) return Vector2.ZERO;

                Node dialoguePlayer = targetPawn.getNode("DialoguePlayer");
                if (dialogueUI != null && dialoguePlayer != null) {
                    dialogueUI.showDialogue(pawn, dialoguePlayer);
                }
            }
        }

        return Vector2.ZERO;
    }

    private RPPawn getCellPawn(Vector2i cell, int type) {
        GodotArray<Node> children = getChildren();
        if (children == null) return null;

        for (int ci = 0; ci < children.size(); ci++) {
            Node node = children.get(ci);
            if (!(node instanceof RPPawn pawn)) continue;
            if (pawn.getType() != type) continue;

            Vector2i nodeMapPos = localToMap(pawn.getPosition());
            if (nodeMapPos.x == cell.x && nodeMapPos.y == cell.y) return pawn;
        }
        return null;
    }
}
