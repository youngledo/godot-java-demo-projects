package demos.twod.role_playing_game;

import org.godot.annotation.GodotClass;
import org.godot.node.TileMapLayer;
import org.godot.math.Vector2i;

@GodotClass(name = "RPGrid", parent = "TileMapLayer")
public class RPGrid extends TileMapLayer {

    private RPDialogueUI dialogueUI;
    private boolean initialized = false;

    @Override
    public void _ready() {
        if (initialized) return;
        initialized = true;

        dialogueUI = (RPDialogueUI) getProperty("dialogue_ui");

        Object children = getChildren();
        if (children instanceof org.godot.Godot[]) {
            for (org.godot.Godot child : (org.godot.Godot[]) children) {
                Object typeObj = child.getProperty("type");
                int type = typeObj instanceof Number ? ((Number) typeObj).intValue() : 0;
                Object posObj = child.getProperty("position");
                if (posObj instanceof org.godot.math.Vector2) {
                    Object mapPos = call("local_to_map", posObj);
                    setCell((org.godot.math.Vector2i) mapPos, type, new Vector2i(0, 0));
                }
            }
        }
    }

    public org.godot.math.Vector2 requestMove(org.godot.Godot pawn, org.godot.math.Vector2i direction) {
        Object pawnPosObj = pawn.getProperty("position");
        if (!(pawnPosObj instanceof org.godot.math.Vector2)) return org.godot.math.Vector2.ZERO;
        org.godot.math.Vector2 pawnPos = (org.godot.math.Vector2) pawnPosObj;

        Object cellStart = call("local_to_map", pawnPos);
        Object cellTargetObj = call("(Vector2i)" + cellStart + " + " + direction);
        // Use call for vector math
        org.godot.math.Vector2i startCell = (org.godot.math.Vector2i) cellStart;
        org.godot.math.Vector2i targetCell = new org.godot.math.Vector2i(
                startCell.x + direction.x, startCell.y + direction.y);

        Object cellTileIdObj = call("get_cell_source_id", targetCell);
        int cellTileId = cellTileIdObj instanceof Number ? ((Number) cellTileIdObj).intValue() : -1;

        if (cellTileId == -1) {
            setCell(targetCell, RPPawn.CELL_TYPE_ACTOR, new Vector2i(0, 0));
            setCell(startCell, -1, new Vector2i(0, 0));
            Object result = call("map_to_local", targetCell);
            return result instanceof org.godot.math.Vector2 ? (org.godot.math.Vector2) result : org.godot.math.Vector2.ZERO;
        }

        if (cellTileId == RPPawn.CELL_TYPE_OBJECT || cellTileId == RPPawn.CELL_TYPE_ACTOR) {
            org.godot.Godot targetPawn = getCellPawn(targetCell, cellTileId);
            if (targetPawn != null) {
                boolean hasDialogue = (boolean) ((org.godot.node.Node) targetPawn).hasNode("DialoguePlayer");
                if (!hasDialogue) return org.godot.math.Vector2.ZERO;

                org.godot.Godot dialoguePlayer = (org.godot.Godot) ((org.godot.node.Node) targetPawn).getNode("DialoguePlayer");
                if (dialogueUI != null && dialoguePlayer != null) {
                    dialogueUI.showDialogue(pawn, dialoguePlayer);
                }
            }
        }

        return org.godot.math.Vector2.ZERO;
    }

    private org.godot.Godot getCellPawn(Object cell, int type) {
        Object children = getChildren();
        if (!(children instanceof org.godot.Godot[])) return null;

        for (org.godot.Godot node : (org.godot.Godot[]) children) {
            Object nodeTypeObj = node.getProperty("type");
            int nodeType = nodeTypeObj instanceof Number ? ((Number) nodeTypeObj).intValue() : 0;
            if (nodeType != type) continue;

            Object nodePosObj = node.getProperty("position");
            if (nodePosObj instanceof org.godot.math.Vector2) {
                Object nodeMapPos = call("local_to_map", nodePosObj);
                if (nodeMapPos instanceof org.godot.math.Vector2i && cell instanceof org.godot.math.Vector2i) {
                    org.godot.math.Vector2i nmp = (org.godot.math.Vector2i) nodeMapPos;
                    org.godot.math.Vector2i cp = (org.godot.math.Vector2i) cell;
                    if (nmp.x == cp.x && nmp.y == cp.y) return node;
                }
            }
        }
        return null;
    }
}
