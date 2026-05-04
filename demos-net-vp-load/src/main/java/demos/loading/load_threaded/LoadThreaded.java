package demos.loading.load_threaded;

import org.godot.annotation.GodotClass;
import org.godot.annotation.GodotMethod;
import org.godot.node.VBoxContainer;
import org.godot.Godot;

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
        // Request threaded loading for each painting.
        for (String path : PAINTING_PATHS) {
            call("ResourceLoader.load_threaded_request", path);
        }

        // Enable all buttons in the GetLoaded container.
        Godot getLoaded = (Godot) call("get_node", "GetLoaded");
        if (getLoaded != null) {
            int childCount = (int) getLoaded.call("get_child_count");
            for (int i = 0; i < childCount; i++) {
                Godot btn = (Godot) getLoaded.call("get_child", i);
                if (btn != null) {
                    btn.setProperty("disabled", false);
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
        // Get the loaded texture from the threaded loader.
        Object tex = call("ResourceLoader.load_threaded_get", resourcePath);
        Godot painting = (Godot) call("get_node", paintingNodePath);
        if (painting != null && tex != null) {
            painting.setProperty("texture", tex);
        }
        // Disable the button after loading.
        Godot btn = (Godot) call("get_node", buttonNodePath);
        if (btn != null) {
            btn.setProperty("disabled", true);
        }
    }
}
