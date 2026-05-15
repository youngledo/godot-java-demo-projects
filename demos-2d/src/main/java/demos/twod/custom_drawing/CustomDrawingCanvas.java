package demos.twod.custom_drawing;

import org.godot.annotation.GodotClass;
import org.godot.math.Color;
import org.godot.math.Rect2;
import org.godot.math.Vector2;
import org.godot.node.Control;
import org.godot.node.Font;
import org.godot.node.Node;

@GodotClass(name = "CustomDrawingCanvas", parent = "Control")
public class CustomDrawingCanvas extends Control {
    private double elapsed;

    @Override
    public void _ready() {
        setProcess(true);
    }

    @Override
    public void _process(double delta) {
        elapsed += delta;
        if ("Animation".equals(getSectionName())) {
            queueRedraw();
        }
    }

    @Override
    public void _draw() {
        String section = getSectionName();
        switch (section) {
            case "Lines" -> drawLines();
            case "Rectangles" -> drawRectangles();
            case "Polygons" -> drawPolygons();
            case "Text" -> drawTextSamples();
            case "Animation" -> drawAnimationSamples();
            default -> drawPlaceholder(section);
        }
    }

    private String getSectionName() {
        Node parent = getParent();
        return parent != null ? parent.getName() : "";
    }

    private void drawLines() {
        Color green = new Color(0.35, 1.0, 0.35);
        Color blue = new Color(0.25, 0.55, 1.0);
        Color orange = new Color(1.0, 0.6, 0.2);

        drawLine(new Vector2(260, 70), new Vector2(520, 70), green, 8.0, true);
        drawDashedLine(new Vector2(260, 115), new Vector2(520, 115), blue, 6.0, 14.0, true, true);
        drawCircle(new Vector2(390, 190), 42.0, orange, false, 6.0, true);
        drawArc(new Vector2(390, 320), 56.0, 0.0, Math.PI * 1.5, 48, green, 6.0, true);
    }

    private void drawRectangles() {
        drawRect(new Rect2(260, 70, 140, 95), new Color(0.2, 0.55, 1.0, 0.35));
        drawRect(new Rect2(430, 70, 140, 95), new Color(0.2, 0.55, 1.0), false, 5.0, true);
        drawRect(new Rect2(260, 215, 310, 95), new Color(1.0, 0.55, 0.25, 0.45));
        drawLine(new Vector2(260, 215), new Vector2(570, 310), new Color(1.0, 0.85, 0.25), 4.0, true);
        drawLine(new Vector2(570, 215), new Vector2(260, 310), new Color(1.0, 0.85, 0.25), 4.0, true);
    }

    private void drawPolygons() {
        double[][] triangle = {{320, 90}, {250, 210}, {390, 210}};
        double[][] triangleColors = {{1, 0.3, 0.2, 1}, {0.3, 1, 0.4, 1}, {0.2, 0.5, 1, 1}};
        drawPolygon(triangle, triangleColors);

        double[][] pentagon = {{520, 90}, {590, 145}, {560, 230}, {480, 230}, {450, 145}};
        drawColoredPolygon(pentagon, new Color(0.8, 0.45, 1.0, 0.65));

        double[][] polyline = {{260, 320}, {330, 285}, {400, 345}, {470, 300}, {540, 350}};
        drawPolyline(polyline, new Color(0.35, 1.0, 0.35), 6.0, true);
    }

    private void drawTextSamples() {
        Font font = getThemeDefaultFont();
        drawString(font, new Vector2(260, 100), "draw_string() from Java", 0, -1.0, 32, new Color(0.95, 0.95, 0.95));
        drawString(font, new Vector2(260, 160), "typed CanvasItem API", 0, -1.0, 24, new Color(0.35, 1.0, 0.35));
        drawString(font, new Vector2(260, 230), "你好，Godot Java", 0, -1.0, 28, new Color(1.0, 0.75, 0.25));
    }

    private void drawAnimationSamples() {
        double angle = elapsed * 2.2;
        double endAngle = angle + Math.PI * 1.4;
        Color color = Color.fromHsv((elapsed * 0.1) % 1.0, 0.8, 1.0);

        drawArc(new Vector2(360, 165), 72.0, angle, endAngle, 64, color, 10.0, true);
        drawCircle(new Vector2(360 + Math.cos(angle) * 100.0, 165 + Math.sin(angle) * 60.0), 18.0, color);
        drawAnimationSlice(elapsed % 2.0, 2.0, 0.35, 1.35);
        drawRect(new Rect2(260, 310, 220, 56), new Color(0.2, 0.55, 1.0), false, 4.0, true);
    }

    private void drawPlaceholder(String section) {
        Font font = getThemeDefaultFont();
        drawRect(new Rect2(260, 90, 320, 180), new Color(0.2, 0.2, 0.25, 0.7));
        drawString(font, new Vector2(290, 175), section + " drawing API", 0, -1.0, 28, new Color(0.8, 0.8, 0.85));
    }
}
