package demos.audio.text_to_speech;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import org.godot.annotation.GodotClass;
import org.godot.annotation.GodotMethod;
import org.godot.collection.GodotArray;
import org.godot.collection.GodotDictionary;
import org.godot.core.Callable;
import org.godot.math.Color;
import org.godot.node.BaseButton;
import org.godot.node.ColorRect;
import org.godot.node.Control;
import org.godot.node.Label;
import org.godot.node.LineEdit;
import org.godot.node.Node;
import org.godot.node.Range;
import org.godot.node.RichTextLabel;
import org.godot.node.TextEdit;
import org.godot.node.Tree;
import org.godot.node.TreeItem;
import org.godot.singleton.DisplayServer;
import org.godot.singleton.OS;

@GodotClass(name = "VoiceList", parent = "Control")
public class VoiceList extends Control {

    private int id = 0;
    private final Map<Integer, String> utMap = new HashMap<>();
    private GodotArray<GodotDictionary> voices = new GodotArray<>();

    @Override
    public void _ready() {
        DisplayServer displayServer = DisplayServer.singleton();
        voices = displayServer.ttsGetVoices();

        Tree tree = getNodeAs("Tree", Tree.class);
        if (tree != null) {
            TreeItem root = tree.createItem();
            tree.setHideRoot(true);
            tree.setColumnTitle(0, "Name");
            tree.setColumnTitle(1, "Language");
            tree.setColumnTitlesVisible(true);

            for (int i = 0; i < voices.size(); i++) {
                GodotDictionary voice = voices.get(i);
                TreeItem child = tree.createItem(root);
                child.setText(0, voiceName(voice));
                child.setMetadata(0, voiceId(voice));
                child.setText(1, voiceLanguage(voice));
            }
        }

        appendLog(voices.size() + " voices available.\n=======\n");

        if (tree != null) {
            TreeItem rootItem = tree.getRoot();
            if (rootItem != null) {
                TreeItem firstChild = rootItem.getChild(0);
                if (firstChild != null) firstChild.select(0);
            }
        }

        displayServer.ttsSetUtteranceCallback(DisplayServer.TTSUtteranceEvent.TTS_UTTERANCE_STARTED, new Callable(this, "_onUtteranceStart"));
        displayServer.ttsSetUtteranceCallback(DisplayServer.TTSUtteranceEvent.TTS_UTTERANCE_ENDED, new Callable(this, "_onUtteranceEnd"));
        displayServer.ttsSetUtteranceCallback(DisplayServer.TTSUtteranceEvent.TTS_UTTERANCE_CANCELED, new Callable(this, "_onUtteranceError"));
        displayServer.ttsSetUtteranceCallback(DisplayServer.TTSUtteranceEvent.TTS_UTTERANCE_BOUNDARY, new Callable(this, "_onUtteranceBoundary"));
    }

    @Override
    public void _process(double delta) {
        DisplayServer displayServer = DisplayServer.singleton();

        BaseButton pauseBtn = getNodeAs("ButtonPause", BaseButton.class);
        if (pauseBtn != null) {
            pauseBtn.setButtonPressed(displayServer.ttsIsPaused());
        }

        ColorRect colorRect = getNodeAs("ColorRect", ColorRect.class);
        if (colorRect != null) {
            colorRect.setColor(displayServer.ttsIsSpeaking() ? new Color(0.9, 0.3, 0.1) : new Color(1, 1, 1));
        }
    }

    @GodotMethod
    public void _onUtteranceBoundary(int pos, int utId) {
        if (utMap.containsKey(utId)) {
            String text = utMap.get(utId);
            String bbcode = "[bgcolor=yellow][color=black]" + text.substring(0, Math.min(pos, text.length()))
                    + "[/color][/bgcolor]" + (pos < text.length() ? text.substring(pos) : "");
            setNodeText("RichTextLabel", bbcode);
        }
    }

    @GodotMethod
    public void _onUtteranceStart(int utId) {
        appendLog("Utterance " + utId + " started.\n");
    }

    @GodotMethod
    public void _onUtteranceEnd(int utId) {
        if (utMap.containsKey(utId)) {
            setNodeText("RichTextLabel", "[bgcolor=yellow][color=black]" + utMap.get(utId) + "[/color][/bgcolor]");
        }
        appendLog("Utterance " + utId + " ended.\n");
        utMap.remove(utId);
    }

    @GodotMethod
    public void _onUtteranceError(int utId) {
        setNodeText("RichTextLabel", "");
        appendLog("Utterance " + utId + " canceled/failed.\n");
        utMap.remove(utId);
    }

    @GodotMethod
    public void _onButtonStopPressed() {
        DisplayServer.singleton().ttsStop();
    }

    @GodotMethod
    public void _onButtonPausePressed() {
        BaseButton pauseBtn = getNodeAs("ButtonPause", BaseButton.class);
        if (pauseBtn != null && pauseBtn.isButtonPressed()) {
            DisplayServer.singleton().ttsPause();
        } else {
            DisplayServer.singleton().ttsResume();
        }
    }

    @GodotMethod
    public void _onButtonSpeakPressed() {
        speakSelected(false, "queried", "Speak");
    }

    @GodotMethod
    public void _onButtonIntSpeakPressed() {
        speakSelected(true, "interrupted", "Interrupt");
    }

    @GodotMethod
    public void _onButtonClearLogPressed() {
        setNodeText("Log", "");
    }

    @GodotMethod
    public void _onHSliderRateValueChanged(double value) {
        Label valueLabel = getNodeAs("HSliderRate/Value", Label.class);
        if (valueLabel != null) valueLabel.setText(String.format(Locale.ROOT, "%.2fx", value));
    }

    @GodotMethod
    public void _onHSliderPitchValueChanged(double value) {
        Label valueLabel = getNodeAs("HSliderPitch/Value", Label.class);
        if (valueLabel != null) valueLabel.setText(String.format(Locale.ROOT, "%.2fx", value));
    }

    @GodotMethod
    public void _onHSliderVolumeValueChanged(double value) {
        Label valueLabel = getNodeAs("HSliderVolume/Value", Label.class);
        if (valueLabel != null) valueLabel.setText(String.format(Locale.ROOT, "%d%%", (int) value));
    }

    @GodotMethod
    public void _onButtonPressed() {
        DisplayServer displayServer = DisplayServer.singleton();
        speakDemo(displayServer, "en", "Beware the Jabberwock, my son!", "The jaws that bite, the claws that catch!");
        speakDemo(displayServer, "es", "¡Cuidado, hijo, con el Fablistanón!", "¡Con sus dientes y garras, muerde, apresa!");
        speakDemo(displayServer, "ru", "О, бойся Бармаглота, сын!", "Он так свирлеп и дик!");
    }

    @GodotMethod
    public void _onLineEditFilterNameTextChanged(String newText) {
        Tree tree = getNodeAs("Tree", Tree.class);
        if (tree == null) return;

        tree.clear();
        TreeItem root = tree.createItem();

        String nameFilter = getNodeText("LineEditFilterName");
        String langFilter = getNodeText("LineEditFilterLang");

        for (int vi = 0; vi < voices.size(); vi++) {
            GodotDictionary voice = voices.get(vi);
            String vName = voiceName(voice);
            String vLang = voiceLanguage(voice);

            boolean nameMatch = nameFilter.isEmpty() || vName.toLowerCase(Locale.ROOT).contains(nameFilter.toLowerCase(Locale.ROOT));
            boolean langMatch = langFilter.isEmpty() || vLang.toLowerCase(Locale.ROOT).contains(langFilter.toLowerCase(Locale.ROOT));

            if (nameMatch && langMatch) {
                TreeItem child = tree.createItem(root);
                child.setText(0, vName);
                child.setMetadata(0, voiceId(voice));
                child.setText(1, vLang);
            }
        }
    }

    private void speakSelected(boolean interrupt, String action, String buttonLabel) {
        Tree tree = getNodeAs("Tree", Tree.class);
        if (tree == null) return;

        TreeItem selected = tree.getSelected();
        if (selected == null) {
            OS.singleton().alert("No voice selected.\nSelect a voice in the list, then try using " + buttonLabel + " again.", "");
            return;
        }

        appendLog("Utterance " + id + " " + action + ".\n");
        String text = getNodeText("Utterance");
        utMap.put(id, text);
        DisplayServer.singleton().ttsSpeak(text, String.valueOf(selected.getMetadata(0)), (int) Math.round(getVolume()), getPitch(), getRate(), id, interrupt);
        id++;
    }

    private void speakDemo(DisplayServer displayServer, String language, String first, String second) {
        String[] voiceIds = displayServer.ttsGetVoicesForLanguage(language);
        if (voiceIds.length == 0) return;

        String voiceId = voiceIds[0];
        utMap.put(id, first);
        utMap.put(id + 1, second);
        displayServer.ttsSpeak(first, voiceId, (int) Math.round(getVolume()), getPitch(), getRate(), id);
        displayServer.ttsSpeak(second, voiceId, (int) Math.round(getVolume()), getPitch(), getRate(), id + 1);
        id += 2;
    }

    private double getRate() {
        Range slider = getNodeAs("HSliderRate", Range.class);
        return slider != null ? slider.getValue() : 1.0;
    }

    private double getPitch() {
        Range slider = getNodeAs("HSliderPitch", Range.class);
        return slider != null ? slider.getValue() : 1.0;
    }

    private double getVolume() {
        Range slider = getNodeAs("HSliderVolume", Range.class);
        return slider != null ? slider.getValue() : 50.0;
    }

    private void appendLog(String text) {
        setNodeText("Log", getNodeText("Log") + text);
    }

    private String voiceName(GodotDictionary voice) {
        return String.valueOf(voice.get("name"));
    }

    private String voiceLanguage(GodotDictionary voice) {
        return String.valueOf(voice.get("language"));
    }

    private String voiceId(GodotDictionary voice) {
        return String.valueOf(voice.get("id"));
    }

    private String getNodeText(String path) {
        Node node = getNode(path);
        if (node instanceof LineEdit lineEdit) return lineEdit.getText();
        if (node instanceof TextEdit textEdit) return textEdit.getText();
        if (node instanceof RichTextLabel richTextLabel) return richTextLabel.getText();
        if (node instanceof Label label) return label.getText();
        return "";
    }

    private void setNodeText(String path, String text) {
        Node node = getNode(path);
        if (node instanceof LineEdit lineEdit) lineEdit.setText(text);
        if (node instanceof TextEdit textEdit) textEdit.setText(text);
        if (node instanceof RichTextLabel richTextLabel) richTextLabel.setText(text);
        if (node instanceof Label label) label.setText(text);
    }
}
