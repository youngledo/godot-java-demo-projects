package demos.loading.runtime_save_load;

import org.godot.annotation.GodotClass;
import org.godot.annotation.GodotMethod;
import org.godot.node.Control;
import org.godot.Godot;

/**
 * Demonstrates how to load and save various file types at runtime without
 * going through Godot's resource importing system. Supports images, audio,
 * 3D scenes (glTF/FBX), fonts, ZIP archives, and plain text files.
 */
@GodotClass(name = "RuntimeSaveLoad", parent = "Control")
public class RuntimeSaveLoad extends Control {

    private Godot filePathEdit;
    private Godot fileDialog;
    private Godot plainTextViewer;
    private Godot plainTextViewerLabel;
    private Godot textureViewer;
    private Godot audioPlayer;
    private Godot audioPlayerInformation;
    private Godot audioStreamPlayer;
    private Godot sceneViewer;
    private Godot sceneViewerCamera;
    private Godot fontViewer;
    private Godot zipViewer;
    private Godot zipViewerFileList;
    private Godot zipViewerFilePreview;
    private Godot errorLabel;
    private Godot exportButton;
    private Godot exportFileDialog;

    private Godot zipReader;
    private Godot sceneViewerRootNode;

    @Override
    public void _ready() {
        filePathEdit = (Godot) call("get_node", "MarginContainer/VBoxContainer/HBoxContainer/FilePath");
        fileDialog = (Godot) call("get_node", "MarginContainer/VBoxContainer/HBoxContainer/FileDialog");
        plainTextViewer = (Godot) call("get_node", "MarginContainer/VBoxContainer/Result/PlainTextViewer");
        plainTextViewerLabel = (Godot) call("get_node", "MarginContainer/VBoxContainer/Result/PlainTextViewer/Label");
        textureViewer = (Godot) call("get_node", "MarginContainer/VBoxContainer/Result/TextureViewer");
        audioPlayer = (Godot) call("get_node", "MarginContainer/VBoxContainer/Result/AudioPlayer");
        audioPlayerInformation = (Godot) call("get_node", "MarginContainer/VBoxContainer/Result/AudioPlayer/Information");
        audioStreamPlayer = (Godot) call("get_node", "MarginContainer/VBoxContainer/Result/AudioPlayer/AudioStreamPlayer");
        sceneViewer = (Godot) call("get_node", "MarginContainer/VBoxContainer/Result/SceneViewer");
        sceneViewerCamera = (Godot) call("get_node", "MarginContainer/VBoxContainer/Result/SceneViewer/SubViewport/Camera3D");
        fontViewer = (Godot) call("get_node", "MarginContainer/VBoxContainer/Result/FontViewer");
        zipViewer = (Godot) call("get_node", "MarginContainer/VBoxContainer/Result/ZIPViewer");
        zipViewerFileList = (Godot) call("get_node", "MarginContainer/VBoxContainer/Result/ZIPViewer/FileList");
        zipViewerFilePreview = (Godot) call("get_node", "MarginContainer/VBoxContainer/Result/ZIPViewer/FilePreview");
        errorLabel = (Godot) call("get_node", "MarginContainer/VBoxContainer/Result/ErrorLabel");
        exportButton = (Godot) call("get_node", "MarginContainer/VBoxContainer/Export");
        exportFileDialog = (Godot) call("get_node", "MarginContainer/VBoxContainer/Export/FileDialog");

        // Create the ZIP reader instance.
        zipReader = (Godot) call("ZIPReader.new");
    }

    private void resetVisibility() {
        if (plainTextViewer != null) plainTextViewer.setProperty("visible", false);
        if (textureViewer != null) textureViewer.setProperty("visible", false);
        if (audioPlayer != null) audioPlayer.setProperty("visible", false);

        if (sceneViewer != null) {
            sceneViewer.setProperty("visible", false);
            int childCount = (int) sceneViewer.call("get_child_count");
            if (childCount > 0) {
                Godot lastChild = (Godot) sceneViewer.call("get_child", childCount - 1);
                if (lastChild != null) {
                    Object isNode3D = lastChild.call("is_class", "Node3D");
                    if (Boolean.TRUE.equals(isNode3D)) {
                        sceneViewer.call("remove_child", lastChild);
                        lastChild.call("queue_free");
                    }
                }
            }
        }

        if (fontViewer != null) fontViewer.setProperty("visible", false);
        if (zipViewer != null) {
            zipViewer.setProperty("visible", false);
            if (zipViewerFileList != null) zipViewerFileList.call("clear");
        }
        if (errorLabel != null) errorLabel.setProperty("visible", false);
        if (exportButton != null) exportButton.setProperty("disabled", false);
    }

    @GodotMethod
    public void _onBrowsePressed() {
        if (fileDialog != null) fileDialog.call("popup_centered_ratio");
    }

    @GodotMethod
    public void _onFilePathTextSubmitted(String newText) {
        openFile(newText);
        // Put the caret at the end of the submitted text.
        if (filePathEdit != null) {
            int textLength = ((String) filePathEdit.getProperty("text")).length();
            filePathEdit.setProperty("caret_column", textLength);
        }
    }

    @GodotMethod
    public void _onFileDialogFileSelected(String path) {
        openFile(path);
    }

    @GodotMethod
    public void _onAudioPlayerPressed() {
        if (audioStreamPlayer != null) audioStreamPlayer.call("play");
    }

    @GodotMethod
    public void _onSceneViewerZoomValueChanged(double value) {
        // Slider uses negative value so that it can be inverted easily.
        if (sceneViewerCamera != null) {
            sceneViewerCamera.setProperty("size", Math.abs(value));
        }
    }

    @GodotMethod
    public void _onZipViewerItemSelected(int index) {
        if (zipViewerFileList != null && zipReader != null && zipViewerFilePreview != null) {
            String itemText = (String) zipViewerFileList.call("get_item_text", index);
            Object data = zipReader.call("read_file", itemText);
            if (data != null) {
                String text = (String) call("get_string_from_utf8", new Object[]{data});
                zipViewerFilePreview.setProperty("text", text);
            }
        }
    }

    // --- File exporting ---

    @GodotMethod
    public void _onExportPressed() {
        if (exportFileDialog != null) exportFileDialog.call("popup_centered_ratio");
    }

    @GodotMethod
    public void _onExportFileDialogFileSelected(String path) {
        if (Boolean.TRUE.equals(plainTextViewer.getProperty("visible"))) {
            Object fa = call("FileAccess.open", path, 2); // FileAccess.WRITE = 2
            if (fa != null) {
                ((Godot) fa).call("store_string", plainTextViewerLabel.getProperty("text"));
                ((Godot) fa).call("close");
            }
        } else if (Boolean.TRUE.equals(textureViewer.getProperty("visible"))) {
            Object image = textureViewer.call("get_image");
            if (image != null) {
                String pathLower = path.toLowerCase();
                if (pathLower.endsWith(".png")) {
                    ((Godot) image).call("save_png", path);
                } else if (pathLower.endsWith(".jpg") || pathLower.endsWith(".jpeg")) {
                    ((Godot) image).call("save_jpg", path, 0.9);
                } else if (pathLower.endsWith(".webp")) {
                    ((Godot) image).call("save_webp", path);
                }
            }
        }
        // Audio: Ogg Vorbis and MP3 can't be exported at runtime to standard format.
        // Font: Can't be exported at runtime to standard format.
        else if (Boolean.TRUE.equals(sceneViewer.getProperty("visible"))) {
            if (sceneViewerRootNode != null) {
                Godot gltfDocument = (Godot) call("GLTFDocument.new");
                Godot gltfState = (Godot) call("GLTFState.new");
                gltfDocument.call("append_from_scene", sceneViewerRootNode, gltfState);
                gltfDocument.call("write_to_filesystem", gltfState, path);
            }
        } else if (Boolean.TRUE.equals(zipViewer.getProperty("visible"))) {
            Godot zipPacker = (Godot) call("ZIPPacker.new");
            Object error = zipPacker.call("open", path);
            if (error != null && !error.toString().equals("0")) {
                return;
            }

            Object filesObj = zipReader.call("get_files");
            if (filesObj instanceof Godot) {
                Godot files = (Godot) filesObj;
                int fileCount = (int) files.call("size");
                for (int i = 0; i < fileCount; i++) {
                    String file = (String) files.call("get", i);
                    zipPacker.call("start_file", file);
                    Object fileData = zipReader.call("read_file", file);
                    zipPacker.call("write_file", fileData);
                    zipPacker.call("close_file");
                }
            }
            zipPacker.call("close");
        }
    }

    private void showError(String message) {
        resetVisibility();
        if (errorLabel != null) {
            errorLabel.setProperty("text", "ERROR: " + message);
            errorLabel.setProperty("visible", true);
        }
    }

    public void openFile(String path) {
        System.out.println("Opening: " + path);
        if (filePathEdit != null) filePathEdit.setProperty("text", path);
        String pathLower = path.toLowerCase();

        // Images.
        if (pathLower.endsWith(".jpg") || pathLower.endsWith(".jpeg") ||
            pathLower.endsWith(".png") || pathLower.endsWith(".webp") ||
            pathLower.endsWith(".svg") || pathLower.endsWith(".tga") ||
            pathLower.endsWith(".bmp")) {

            Object image = call("Image.load_from_file", path);
            resetVisibility();
            if (exportFileDialog != null) {
                exportFileDialog.setProperty("filters", new String[]{"*.png ; PNG Image", "*.jpg, *.jpeg ; JPEG Image", "*.webp ; WebP Image"});
            }
            if (textureViewer != null) {
                textureViewer.setProperty("visible", true);
                Object tex = call("ImageTexture.create_from_image", image);
                textureViewer.setProperty("texture", tex);
            }
        }
        // Audio.
        else if (pathLower.endsWith(".ogg") || pathLower.endsWith(".mp3") || pathLower.endsWith(".wav")) {
            Object stream = null;
            if (pathLower.endsWith(".ogg")) {
                stream = call("AudioStreamOggVorbis.load_from_file", path);
            } else if (pathLower.endsWith(".mp3")) {
                stream = call("AudioStreamMP3.load_from_file", path);
            } else if (pathLower.endsWith(".wav")) {
                stream = call("AudioStreamWAV.load_from_file", path);
            }
            if (audioStreamPlayer != null) audioStreamPlayer.setProperty("stream", stream);
            resetVisibility();
            if (exportButton != null) exportButton.setProperty("disabled", true);
            if (audioPlayer != null) audioPlayer.setProperty("visible", true);

            if (stream != null && audioPlayerInformation != null) {
                double duration = ((Number) ((Godot) stream).getProperty("length")).doubleValue();
                long roundedDuration = Math.round(duration);
                long minutes = roundedDuration / 60;
                long seconds = roundedDuration % 60;
                audioPlayerInformation.setProperty("text",
                    String.format("Duration: %02d:%02d", minutes, seconds));
            }
        }
        // 3D scenes.
        else if (pathLower.endsWith(".gltf") || pathLower.endsWith(".glb")) {
            Godot gltfDocument = (Godot) call("GLTFDocument.new");
            Godot gltfState = (Godot) call("GLTFState.new");
            Object error = gltfDocument.call("append_from_file", path, gltfState);
            if (error != null && error.toString().equals("0")) {
                sceneViewerRootNode = (Godot) gltfDocument.call("generate_scene", gltfState);
                resetVisibility();
                if (sceneViewer != null) {
                    sceneViewer.call("add_child", sceneViewerRootNode);
                    sceneViewer.setProperty("visible", true);
                }
                if (exportFileDialog != null) {
                    exportFileDialog.setProperty("filters", new String[]{"*.gltf ; glTF Text Scene", "*.glb ; glTF Binary Scene"});
                }
            } else {
                showError("Couldn't load the file as a glTF scene.");
            }
        } else if (pathLower.endsWith(".fbx")) {
            Godot fbxDocument = (Godot) call("FBXDocument.new");
            Godot fbxState = (Godot) call("FBXState.new");
            Object error = fbxDocument.call("append_from_file", path, fbxState);
            if (error != null && error.toString().equals("0")) {
                sceneViewerRootNode = (Godot) fbxDocument.call("generate_scene", fbxState);
                resetVisibility();
                if (sceneViewer != null) {
                    sceneViewer.call("add_child", sceneViewerRootNode);
                    sceneViewer.setProperty("visible", true);
                }
                if (exportFileDialog != null) {
                    exportFileDialog.setProperty("filters", new String[]{"*.fbx ; FBX Scene"});
                }
            } else {
                showError("Couldn't load the file as a FBX scene.");
            }
        }
        // Fonts.
        else if (pathLower.endsWith(".ttf") || pathLower.endsWith(".otf") ||
                 pathLower.endsWith(".woff") || pathLower.endsWith(".woff2") ||
                 pathLower.endsWith(".pfb") || pathLower.endsWith(".pfm") ||
                 pathLower.endsWith(".fnt") || pathLower.endsWith(".font")) {
            Godot fontFile = (Godot) call("FontFile.new");
            if (pathLower.endsWith(".fnt") || pathLower.endsWith(".font")) {
                fontFile.call("load_bitmap_font", path);
            } else {
                fontFile.call("load_dynamic_font", path);
            }

            Object data = fontFile.getProperty("data");
            boolean hasData = false;
            if (data instanceof byte[]) {
                hasData = ((byte[]) data).length > 0;
            } else if (data instanceof Godot) {
                hasData = true; // assume non-null Godot object means data exists
            }

            if (hasData && fontViewer != null) {
                fontViewer.call("add_theme_font_override", "font", fontFile);
                resetVisibility();
                fontViewer.setProperty("visible", true);
                if (exportButton != null) exportButton.setProperty("disabled", true);
            } else {
                showError("Couldn't load the file as a font.");
            }
        }
        // ZIP archives.
        else if (pathLower.endsWith(".zip")) {
            if (zipReader != null) {
                zipReader.call("open", path);
                Object filesObj = zipReader.call("get_files");
                if (exportFileDialog != null) {
                    exportFileDialog.setProperty("filters", new String[]{"*.zip ; ZIP Archive"});
                }
                resetVisibility();
                if (filesObj instanceof Godot && zipViewerFileList != null) {
                    Godot files = (Godot) filesObj;
                    int fileCount = (int) files.call("size");
                    for (int i = 0; i < fileCount; i++) {
                        String file = (String) files.call("get", i);
                        zipViewerFileList.call("add_item", file, null);
                        // Make folders disabled in the list.
                        zipViewerFileList.call("set_item_disabled", -1, file.endsWith("/"));
                    }
                }
                if (zipViewer != null) zipViewer.setProperty("visible", true);
            }
        }
        // Fallback - plain text.
        else {
            Object fileContents = call("FileAccess.get_file_as_string", path);
            if (fileContents == null || fileContents.toString().isEmpty()) {
                showError("File is empty or is a binary file.");
            } else {
                if (plainTextViewerLabel != null) {
                    plainTextViewerLabel.setProperty("text", fileContents);
                }
                resetVisibility();
                if (plainTextViewer != null) plainTextViewer.setProperty("visible", true);
            }
        }
    }
}
