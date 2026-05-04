package demos.misc.multiple_windows;

import org.godot.annotation.Export;
import org.godot.annotation.GodotClass;
import org.godot.annotation.GodotMethod;
import org.godot.node.Node;

@GodotClass(name = "MWSpritePolygonPassthrough", parent = "Node")
public class SpritePolygonPassthrough extends Node {

    @Export
    public org.godot.Godot sprite;

    @GodotMethod
    public void generate_polygon() {
        if (sprite == null) return;

        Object textureObj = sprite.getProperty("texture");
        if (textureObj == null) return;
        org.godot.Godot texture = (org.godot.Godot) textureObj;

        Object imageObj = texture.call("get_image");
        if (imageObj == null) return;
        org.godot.Godot image = (org.godot.Godot) imageObj;

        // Create bitmap from image alpha
        org.godot.Godot bitmap = (org.godot.Godot) call("BitMap.new");
        bitmap.call("create_from_image_alpha", image);

        org.godot.math.Vector2 bitmapSize = (org.godot.math.Vector2) bitmap.call("get_size");
        long hframes = (long) sprite.getProperty("hframes");
        long vframes = (long) sprite.getProperty("vframes");
        if (hframes == 0) hframes = 1;
        if (vframes == 0) vframes = 1;

        double cellSizeX = bitmapSize.getX() / (double) hframes;
        double cellSizeY = bitmapSize.getY() / (double) vframes;

        org.godot.math.Vector2 frameCoords = (org.godot.math.Vector2) sprite.getProperty("frame_coords");
        double frameX = frameCoords != null ? frameCoords.getX() : 0;
        double frameY = frameCoords != null ? frameCoords.getY() : 0;
        org.godot.math.Rect2 cellRect = new org.godot.math.Rect2(
            cellSizeX * frameX, cellSizeY * frameY, cellSizeX, cellSizeY);

        bitmap.call("grow_mask", 1, cellRect);
        Object polygonsObj = bitmap.call("opaque_to_polygons", cellRect, 1.0);

        org.godot.math.Vector2 spritePos = (org.godot.math.Vector2) sprite.getProperty("position");
        org.godot.math.Vector2 spriteOffset = (org.godot.math.Vector2) sprite.getProperty("offset");
        org.godot.math.Vector2 offset = new org.godot.math.Vector2(
            (spritePos != null ? spritePos.getX() : 0) + (spriteOffset != null ? spriteOffset.getX() : 0),
            (spritePos != null ? spritePos.getY() : 0) + (spriteOffset != null ? spriteOffset.getY() : 0)
        );

        boolean centered = (boolean) sprite.getProperty("centered");
        if (centered) {
            offset = new org.godot.math.Vector2(
                offset.getX() - cellSizeX / 2.0,
                offset.getY() - cellSizeY / 2.0
            );
        }

        // Build polygon from bitmap polygons
        java.util.List<org.godot.math.Vector2> polygon = new java.util.ArrayList<>();
        org.godot.math.Vector2 firstPoint = null;

        if (polygonsObj instanceof Object[]) {
            Object[] polygonsArray = (Object[]) polygonsObj;
            for (Object polyObj : polygonsArray) {
                if (polyObj instanceof org.godot.math.Vector2[]) {
                    org.godot.math.Vector2[] polyArr = (org.godot.math.Vector2[]) polyObj;
                    for (int i = 0; i < polyArr.length; i++) {
                        org.godot.math.Vector2 point = polyArr[i];
                        polygon.add(new org.godot.math.Vector2(
                            point.getX() + offset.getX(),
                            point.getY() + offset.getY()
                        ));
                        if (firstPoint == null) firstPoint = polygon.get(0);
                    }
                    if (firstPoint != null) {
                        polygon.add(firstPoint);
                        polygon.add(firstPoint);
                    }
                }
            }
        }

        org.godot.Godot win = (org.godot.Godot) call("get_window");
        if (win != null) {
            win.setProperty("mouse_passthrough_polygon", polygon.toArray(new org.godot.math.Vector2[0]));
        }
    }
}
