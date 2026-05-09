package demos.loading.load_threaded;

import org.godot.annotation.GodotClass;
import org.godot.annotation.GodotMethod;
import org.godot.node.Button;
import org.godot.node.HBoxContainer;
import org.godot.node.Node;
import org.godot.node.Resource;
import org.godot.node.Texture2D;
import org.godot.node.TextureRect;
import org.godot.node.VBoxContainer;
import org.godot.singleton.ResourceLoader;

/**
 * Demonstrates how to use ResourceLoader for background (threaded) loading.
 * Starts loading 6 painting textures in the background, then retrieves them
 * on demand when each button is pressed.
 */
@GodotClass(name = "LoadThreaded", parent = "VBoxContainer")
public class LoadThreaded extends VBoxContainer {

    private static final String[] PAINTING_PATHS = {
        "res://paintings/painting_babel.jpg",
        "res://paintings/painting_las_meninas.png",
        "res://paintings/painting_mona_lisa.jpg",
        "res://paintings/painting_old_guitarist.jpg",
        "res://paintings/painting_parasol.jpg",
        "res://paintings/painting_the_swing.jpg"
    };

    @GodotMethod
    public void _onStartLoadingPressed() {
        ResourceLoader resourceLoader = ResourceLoader.singleton();
        for (String path : PAINTING_PATHS) {
            resourceLoader.loadThreadedRequest(path);
        }

        HBoxContainer getLoaded = getNodeAs("GetLoaded", HBoxContainer.class);
        if (getLoaded != null) {
            int childCount = getLoaded.getChildCount();
            for (int i = 0; i < childCount; i++) {
                Node child = getLoaded.getChild(i);
                if (child instanceof Button btn) {
                    btn.setDisabled(false);
                }
            }
        }
    }

    @GodotMethod
    public void _onBabelPressed() {
        setPaintingTexture("Paintings/Babel", "res://paintings/painting_babel.jpg", "GetLoaded/Babel");
    }

    @GodotMethod
    public void _onLasMeninasPressed() {
        setPaintingTexture("Paintings/LasMeninas", "res://paintings/painting_las_meninas.png", "GetLoaded/LasMeninas");
    }

    @GodotMethod
    public void _onMonaLisapressed() {
        setPaintingTexture("Paintings/MonaLisa", "res://paintings/painting_mona_lisa.jpg", "GetLoaded/MonaLisa");
    }

    @GodotMethod
    public void _onOldGuitaristPressed() {
        setPaintingTexture("Paintings/OldGuitarist", "res://paintings/painting_old_guitarist.jpg", "GetLoaded/OldGuitarist");
    }

    @GodotMethod
    public void _onParasolPressed() {
        setPaintingTexture("Paintings/Parasol", "res://paintings/painting_parasol.jpg", "GetLoaded/Parasol");
    }

    @GodotMethod
    public void _onSwingPressed() {
        setPaintingTexture("Paintings/Swing", "res://paintings/painting_the_swing.jpg", "GetLoaded/Swing");
    }

    private void setPaintingTexture(String paintingNodePath, String resourcePath, String buttonNodePath) {
        Resource resource = ResourceLoader.singleton().loadThreadedGet(resourcePath);
        TextureRect painting = getNodeAs(paintingNodePath, TextureRect.class);
        if (painting != null && resource instanceof Texture2D texture) {
            painting.setTexture(texture);
        }

        Button btn = getNodeAs(buttonNodePath, Button.class);
        if (btn != null) {
            btn.setDisabled(true);
        }
    }
}
