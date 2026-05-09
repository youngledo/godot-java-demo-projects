package demos.misc.multiple_windows;

import java.util.ArrayList;
import java.util.List;
import org.godot.annotation.Export;
import org.godot.annotation.GodotClass;
import org.godot.annotation.GodotMethod;
import org.godot.math.Rect2i;
import org.godot.math.Vector2;
import org.godot.math.Vector2i;
import org.godot.node.BitMap;
import org.godot.node.Image;
import org.godot.node.Node;
import org.godot.node.Sprite2D;
import org.godot.node.Texture2D;
import org.godot.node.Window;

@GodotClass(name = "MWSpritePolygonPassthrough", parent = "Node")
public class SpritePolygonPassthrough extends Node {

    @Export
    public Sprite2D sprite;

    @GodotMethod
    public void generatePolygon() {
        if (sprite == null) return;

        Texture2D texture = sprite.getTexture();
        if (texture == null) return;

        Image image = texture.getImage();
        if (image == null) return;

        BitMap bitmap = BitMap.create();
        bitmap.createFromImageAlpha(image);

        Vector2i bitmapSize = bitmap.getSize();
        long hframes = sprite.getHframes();
        long vframes = sprite.getVframes();
        if (hframes == 0) hframes = 1;
        if (vframes == 0) vframes = 1;

        double cellSizeX = bitmapSize.x / (double) hframes;
        double cellSizeY = bitmapSize.y / (double) vframes;

        Vector2i frameCoords = sprite.getFrameCoords();
        int frameX = frameCoords != null ? frameCoords.x : 0;
        int frameY = frameCoords != null ? frameCoords.y : 0;
        Rect2i cellRect = new Rect2i(
            (int) Math.round(cellSizeX * frameX),
            (int) Math.round(cellSizeY * frameY),
            (int) Math.round(cellSizeX),
            (int) Math.round(cellSizeY));

        bitmap.growMask(1, cellRect);
        Object[] polygonsArray = bitmap.opaqueToPolygons(cellRect, 1.0);

        Vector2 spritePos = sprite.getPosition();
        Vector2 spriteOffset = sprite.getOffset();
        Vector2 offset = new Vector2(
            (spritePos != null ? spritePos.x : 0) + (spriteOffset != null ? spriteOffset.x : 0),
            (spritePos != null ? spritePos.y : 0) + (spriteOffset != null ? spriteOffset.y : 0)
        );

        if (sprite.isCentered()) {
            offset = new Vector2(offset.x - cellSizeX / 2.0, offset.y - cellSizeY / 2.0);
        }

        List<double[]> polygon = new ArrayList<>();
        double[] firstPoint = null;

        if (polygonsArray != null) {
            for (Object polyObj : polygonsArray) {
                if (polyObj instanceof Vector2[] polyArr) {
                    for (Vector2 point : polyArr) {
                        double[] polygonPoint = new double[] {point.x + offset.x, point.y + offset.y};
                        polygon.add(polygonPoint);
                        if (firstPoint == null) firstPoint = polygonPoint;
                    }
                    if (firstPoint != null) {
                        polygon.add(firstPoint);
                        polygon.add(firstPoint);
                    }
                }
            }
        }

        Window window = getWindow();
        if (window != null) {
            window.setMousePassthroughPolygon(polygon.toArray(new double[0][]));
        }
    }
}
