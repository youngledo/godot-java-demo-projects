package demos.loading.runtime_save_load;

import org.godot.annotation.GodotClass;
import org.godot.annotation.GodotMethod;
import org.godot.node.AudioStream;
import org.godot.node.AudioStreamMP3;
import org.godot.node.AudioStreamOggVorbis;
import org.godot.node.AudioStreamPlayer;
import org.godot.node.AudioStreamWAV;
import org.godot.node.Button;
import org.godot.node.Camera3D;
import org.godot.node.Control;
import org.godot.node.FBXDocument;
import org.godot.node.FBXState;
import org.godot.node.FileAccess;
import org.godot.node.FileDialog;
import org.godot.node.FontFile;
import org.godot.node.GLTFDocument;
import org.godot.node.GLTFState;
import org.godot.node.HSplitContainer;
import org.godot.node.Image;
import org.godot.node.ImageTexture;
import org.godot.node.ItemList;
import org.godot.node.Label;
import org.godot.node.LineEdit;
import org.godot.node.Node;
import org.godot.node.Node3D;
import org.godot.node.SubViewportContainer;
import org.godot.node.Texture2D;
import org.godot.node.TextureRect;
import org.godot.node.ZIPPacker;
import org.godot.node.ZIPReader;

/**
 * Demonstrates how to load and save various file types at runtime without
 * going through Godot's resource importing system. Supports images, audio,
 * 3D scenes (glTF/FBX), fonts, ZIP archives, and plain text files.
 */
@GodotClass(name = "RuntimeSaveLoad", parent = "Control")
public class RuntimeSaveLoad extends Control {

    private LineEdit filePathEdit;
    private FileDialog fileDialog;
    private Control plainTextViewer;
    private Label plainTextViewerLabel;
    private TextureRect textureViewer;
    private Button audioPlayer;
    private Label audioPlayerInformation;
    private AudioStreamPlayer audioStreamPlayer;
    private SubViewportContainer sceneViewer;
    private Camera3D sceneViewerCamera;
    private Label fontViewer;
    private HSplitContainer zipViewer;
    private ItemList zipViewerFileList;
    private Label zipViewerFilePreview;
    private Label errorLabel;
    private Button exportButton;
    private FileDialog exportFileDialog;

    private ZIPReader zipReader;
    private Node sceneViewerRootNode;

    @Override
    public void _ready() {
        filePathEdit = getNodeAs("MarginContainer/VBoxContainer/HBoxContainer/FilePath", LineEdit.class);
        fileDialog = getNodeAs("MarginContainer/VBoxContainer/HBoxContainer/FileDialog", FileDialog.class);
        plainTextViewer = getNodeAs("MarginContainer/VBoxContainer/Result/PlainTextViewer", Control.class);
        plainTextViewerLabel = getNodeAs("MarginContainer/VBoxContainer/Result/PlainTextViewer/Label", Label.class);
        textureViewer = getNodeAs("MarginContainer/VBoxContainer/Result/TextureViewer", TextureRect.class);
        audioPlayer = getNodeAs("MarginContainer/VBoxContainer/Result/AudioPlayer", Button.class);
        audioPlayerInformation = getNodeAs("MarginContainer/VBoxContainer/Result/AudioPlayer/Information", Label.class);
        audioStreamPlayer = getNodeAs("MarginContainer/VBoxContainer/Result/AudioPlayer/AudioStreamPlayer",
                AudioStreamPlayer.class);
        sceneViewer = getNodeAs("MarginContainer/VBoxContainer/Result/SceneViewer", SubViewportContainer.class);
        sceneViewerCamera = getNodeAs("MarginContainer/VBoxContainer/Result/SceneViewer/SubViewport/Camera3D", Camera3D.class);
        fontViewer = getNodeAs("MarginContainer/VBoxContainer/Result/FontViewer", Label.class);
        zipViewer = getNodeAs("MarginContainer/VBoxContainer/Result/ZIPViewer", HSplitContainer.class);
        zipViewerFileList = getNodeAs("MarginContainer/VBoxContainer/Result/ZIPViewer/FileList", ItemList.class);
        zipViewerFilePreview = getNodeAs("MarginContainer/VBoxContainer/Result/ZIPViewer/FilePreview", Label.class);
        errorLabel = getNodeAs("MarginContainer/VBoxContainer/Result/ErrorLabel", Label.class);
        exportButton = getNodeAs("MarginContainer/VBoxContainer/Export", Button.class);
        exportFileDialog = getNodeAs("MarginContainer/VBoxContainer/Export/FileDialog", FileDialog.class);

        zipReader = ZIPReader.create();
    }

    private void resetVisibility() {
        if (plainTextViewer != null) plainTextViewer.setVisible(false);
        if (textureViewer != null) textureViewer.setVisible(false);
        if (audioPlayer != null) audioPlayer.setVisible(false);

        if (sceneViewer != null) {
            sceneViewer.setVisible(false);
            int childCount = sceneViewer.getChildCount();
            if (childCount > 0) {
                Node lastChild = sceneViewer.getChild(childCount - 1);
                if (lastChild instanceof Node3D) {
                    sceneViewer.removeChild(lastChild);
                    lastChild.queueFree();
                }
            }
        }

        if (fontViewer != null) fontViewer.setVisible(false);
        if (zipViewer != null) {
            zipViewer.setVisible(false);
            if (zipViewerFileList != null) zipViewerFileList.clear();
        }
        if (errorLabel != null) errorLabel.setVisible(false);
        if (exportButton != null) exportButton.setDisabled(false);
    }

    @GodotMethod
    public void _onBrowsePressed() {
        if (fileDialog != null) fileDialog.popupCenteredRatio();
    }

    @GodotMethod
    public void _onFilePathTextSubmitted(String newText) {
        openFile(newText);
        if (filePathEdit != null) {
            filePathEdit.setCaretColumn(filePathEdit.getText().length());
        }
    }

    @GodotMethod
    public void _onFileDialogFileSelected(String path) {
        openFile(path);
    }

    @GodotMethod
    public void _onAudioPlayerPressed() {
        if (audioStreamPlayer != null) audioStreamPlayer.play();
    }

    @GodotMethod
    public void _onSceneViewerZoomValueChanged(double value) {
        if (sceneViewerCamera != null) {
            sceneViewerCamera.setSize(Math.abs(value));
        }
    }

    @GodotMethod
    public void _onZipViewerItemSelected(int index) {
        if (zipViewerFileList != null && zipReader != null && zipViewerFilePreview != null) {
            String itemText = zipViewerFileList.getItemText(index);
            byte[] data = zipReader.readFile(itemText);
            if (data != null) {
                zipViewerFilePreview.setText(getStringFromUtf8(data));
            }
        }
    }

    @GodotMethod
    public void _onExportPressed() {
        if (exportFileDialog != null) exportFileDialog.popupCenteredRatio();
    }

    @GodotMethod
    public void _onExportFileDialogFileSelected(String path) {
        if (plainTextViewer.isVisible()) {
            FileAccess fa = FileAccess.open(path, 2);
            if (fa != null) {
                fa.storeString(plainTextViewerLabel.getText());
                fa.close();
            }
        } else if (textureViewer.isVisible()) {
            Texture2D texture = textureViewer.getTexture();
            Image image = texture != null ? texture.getImage() : null;
            if (image != null) {
                String pathLower = path.toLowerCase();
                if (pathLower.endsWith(".png")) {
                    image.savePng(path);
                } else if (pathLower.endsWith(".jpg") || pathLower.endsWith(".jpeg")) {
                    image.saveJpg(path, 0.9);
                } else if (pathLower.endsWith(".webp")) {
                    image.saveWebp(path);
                }
            }
        } else if (sceneViewer.isVisible()) {
            if (sceneViewerRootNode != null) {
                GLTFDocument gltfDocument = GLTFDocument.create();
                GLTFState gltfState = GLTFState.create();
                gltfDocument.appendFromScene(sceneViewerRootNode, gltfState);
                gltfDocument.writeToFilesystem(gltfState, path);
            }
        } else if (zipViewer.isVisible()) {
            ZIPPacker zipPacker = ZIPPacker.create();
            int error = zipPacker.open(path);
            if (error != 0) {
                return;
            }

            for (String file : zipReader.getFiles()) {
                zipPacker.startFile(file);
                zipPacker.writeFile(zipReader.readFile(file));
                zipPacker.closeFile();
            }
            zipPacker.close();
        }
    }

    private void showError(String message) {
        resetVisibility();
        if (errorLabel != null) {
            errorLabel.setText("ERROR: " + message);
            errorLabel.setVisible(true);
        }
    }

    public void openFile(String path) {
        System.out.println("Opening: " + path);
        if (filePathEdit != null) filePathEdit.setText(path);
        String pathLower = path.toLowerCase();

        if (pathLower.endsWith(".jpg") || pathLower.endsWith(".jpeg") ||
            pathLower.endsWith(".png") || pathLower.endsWith(".webp") ||
            pathLower.endsWith(".svg") || pathLower.endsWith(".tga") ||
            pathLower.endsWith(".bmp")) {

            Image image = Image.loadFromFile(path);
            resetVisibility();
            if (exportFileDialog != null) {
                exportFileDialog.setFilters(new String[]{"*.png ; PNG Image", "*.jpg, *.jpeg ; JPEG Image",
                        "*.webp ; WebP Image"});
            }
            if (textureViewer != null) {
                textureViewer.setVisible(true);
                ImageTexture tex = ImageTexture.createFromImage(image);
                textureViewer.setTexture(tex);
            }
        } else if (pathLower.endsWith(".ogg") || pathLower.endsWith(".mp3") || pathLower.endsWith(".wav")) {
            AudioStream stream = null;
            if (pathLower.endsWith(".ogg")) {
                stream = AudioStreamOggVorbis.loadFromFile(path);
            } else if (pathLower.endsWith(".mp3")) {
                stream = AudioStreamMP3.loadFromFile(path);
            } else if (pathLower.endsWith(".wav")) {
                stream = AudioStreamWAV.loadFromFile(path);
            }
            if (audioStreamPlayer != null) audioStreamPlayer.setStream(stream);
            resetVisibility();
            if (exportButton != null) exportButton.setDisabled(true);
            if (audioPlayer != null) audioPlayer.setVisible(true);

            if (stream != null && audioPlayerInformation != null) {
                long roundedDuration = Math.round(stream.getLength());
                long minutes = roundedDuration / 60;
                long seconds = roundedDuration % 60;
                audioPlayerInformation.setText(String.format("Duration: %02d:%02d", minutes, seconds));
            }
        } else if (pathLower.endsWith(".gltf") || pathLower.endsWith(".glb")) {
            GLTFDocument gltfDocument = GLTFDocument.create();
            GLTFState gltfState = GLTFState.create();
            int error = gltfDocument.appendFromFile(path, gltfState);
            if (error == 0) {
                sceneViewerRootNode = gltfDocument.generateScene(gltfState);
                resetVisibility();
                if (sceneViewer != null) {
                    sceneViewer.addChild(sceneViewerRootNode);
                    sceneViewer.setVisible(true);
                }
                if (exportFileDialog != null) {
                    exportFileDialog.setFilters(new String[]{"*.gltf ; glTF Text Scene", "*.glb ; glTF Binary Scene"});
                }
            } else {
                showError("Couldn't load the file as a glTF scene.");
            }
        } else if (pathLower.endsWith(".fbx")) {
            FBXDocument fbxDocument = FBXDocument.create();
            FBXState fbxState = FBXState.create();
            int error = fbxDocument.appendFromFile(path, fbxState);
            if (error == 0) {
                sceneViewerRootNode = fbxDocument.generateScene(fbxState);
                resetVisibility();
                if (sceneViewer != null) {
                    sceneViewer.addChild(sceneViewerRootNode);
                    sceneViewer.setVisible(true);
                }
                if (exportFileDialog != null) {
                    exportFileDialog.setFilters(new String[]{"*.fbx ; FBX Scene"});
                }
            } else {
                showError("Couldn't load the file as a FBX scene.");
            }
        } else if (pathLower.endsWith(".ttf") || pathLower.endsWith(".otf") ||
                 pathLower.endsWith(".woff") || pathLower.endsWith(".woff2") ||
                 pathLower.endsWith(".pfb") || pathLower.endsWith(".pfm") ||
                 pathLower.endsWith(".fnt") || pathLower.endsWith(".font")) {
            FontFile fontFile = FontFile.create();
            if (pathLower.endsWith(".fnt") || pathLower.endsWith(".font")) {
                fontFile.loadBitmapFont(path);
            } else {
                fontFile.loadDynamicFont(path);
            }

            byte[] data = fontFile.getData();
            if (data != null && data.length > 0 && fontViewer != null) {
                fontViewer.addThemeFontOverride("font", fontFile);
                resetVisibility();
                fontViewer.setVisible(true);
                if (exportButton != null) exportButton.setDisabled(true);
            } else {
                showError("Couldn't load the file as a font.");
            }
        } else if (pathLower.endsWith(".zip")) {
            if (zipReader != null) {
                zipReader.open(path);
                if (exportFileDialog != null) {
                    exportFileDialog.setFilters(new String[]{"*.zip ; ZIP Archive"});
                }
                resetVisibility();
                if (zipViewerFileList != null) {
                    for (String file : zipReader.getFiles()) {
                        zipViewerFileList.addItem(file);
                        zipViewerFileList.setItemDisabled(-1, file.endsWith("/"));
                    }
                }
                if (zipViewer != null) zipViewer.setVisible(true);
            }
        } else {
            String fileContents = FileAccess.getFileAsString(path);
            if (fileContents == null || fileContents.isEmpty()) {
                showError("File is empty or is a binary file.");
            } else {
                if (plainTextViewerLabel != null) {
                    plainTextViewerLabel.setText(fileContents);
                }
                resetVisibility();
                if (plainTextViewer != null) plainTextViewer.setVisible(true);
            }
        }
    }
}
