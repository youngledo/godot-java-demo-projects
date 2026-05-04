package demos.audio.text_to_speech;

import org.godot.annotation.GodotClass;
import org.godot.annotation.GodotMethod;
import org.godot.math.Color;
import org.godot.node.Control;

/**
 * Text-to-speech demo - demonstrates TTS capabilities using DisplayServer.
 * Lists available voices, allows speaking text with various parameters,
 * and shows utterance callbacks.
 */
@GodotClass(name = "VoiceList", parent = "Control")
public class VoiceList extends Control {

    /** The utterance ID to use for text to speech. */
    private int id = 0;

    private final java.util.Map<Integer, String> utMap = new java.util.HashMap<>();
    private org.godot.collection.GodotArray vs;

    @Override
    public void _ready() {
        org.godot.singleton.DisplayServer ds = org.godot.singleton.DisplayServer.singleton();

        // Get voice data.
        vs = (org.godot.collection.GodotArray) ds.call("tts_get_voices");

        org.godot.Godot tree = (org.godot.Godot) call("get_node", "Tree");
        if (tree != null) {
            org.godot.Godot root = (org.godot.Godot) tree.call("create_item");
            tree.call("set_hide_root", true);
            tree.call("set_column_title", 0, "Name");
            tree.call("set_column_title", 1, "Language");
            tree.call("set_column_titles_visible", true);

            for (int vi = 0; vi < vs.size(); vi++) {
                org.godot.Godot v = (org.godot.Godot) vs.get(vi);
                org.godot.Godot child = (org.godot.Godot) tree.call("create_item", root);
                child.call("set_text", 0, v.getProperty("name"));
                child.call("set_metadata", 0, v.getProperty("id"));
                child.call("set_text", 1, v.getProperty("language"));
            }
        }

        org.godot.Godot log = (org.godot.Godot) call("get_node", "Log");
        if (log != null) {
            appendLog(vs.size() + " voices available.\n=======\n");
        }

        // Ensure the first voice added to the list is preselected.
        if (tree != null) {
            org.godot.Godot rootItem = (org.godot.Godot) tree.call("get_root");
            if (rootItem != null) {
                org.godot.Godot firstChild = (org.godot.Godot) rootItem.call("get_child", 0);
                if (firstChild != null) firstChild.call("select", 0);
            }
        }

        // Add callbacks using Callables.
        org.godot.core.Callable startCb = new org.godot.core.Callable(this, "_onUtteranceStart");
        org.godot.core.Callable endCb = new org.godot.core.Callable(this, "_onUtteranceEnd");
        org.godot.core.Callable errorCb = new org.godot.core.Callable(this, "_onUtteranceError");
        org.godot.core.Callable boundaryCb = new org.godot.core.Callable(this, "_onUtteranceBoundary");

        ds.call("tts_set_utterance_callback", 0, startCb); // TTS_UTTERANCE_STARTED
        ds.call("tts_set_utterance_callback", 1, endCb);   // TTS_UTTERANCE_ENDED
        ds.call("tts_set_utterance_callback", 3, errorCb);  // TTS_UTTERANCE_CANCELED
        ds.call("tts_set_utterance_callback", 4, boundaryCb); // TTS_UTTERANCE_BOUNDARY
    }

    @Override
    public void _process(double delta) {
        org.godot.singleton.DisplayServer ds = org.godot.singleton.DisplayServer.singleton();

        org.godot.Godot pauseBtn = (org.godot.Godot) call("get_node", "ButtonPause");
        if (pauseBtn != null) {
            pauseBtn.setProperty("button_pressed", ds.call("tts_is_paused"));
        }

        org.godot.Godot colorRect = (org.godot.Godot) call("get_node", "ColorRect");
        if (colorRect != null) {
            boolean speaking = (boolean) ds.call("tts_is_speaking");
            colorRect.setProperty("color", speaking ? new Color(0.9, 0.3, 0.1) : new Color(1, 1, 1));
        }
    }

    @GodotMethod
    public void _onUtteranceBoundary(int pos, int utId) {
        org.godot.Godot rtl = (org.godot.Godot) call("get_node", "RichTextLabel");
        if (rtl != null && utMap.containsKey(utId)) {
            String text = utMap.get(utId);
            String bbcode = "[bgcolor=yellow][color=black]" + text.substring(0, Math.min(pos, text.length()))
                    + "[/color][/bgcolor]" + (pos < text.length() ? text.substring(pos) : "");
            rtl.setProperty("text", bbcode);
        }
    }

    @GodotMethod
    public void _onUtteranceStart(int utId) {
        appendLog("Utterance " + utId + " started.\n");
    }

    @GodotMethod
    public void _onUtteranceEnd(int utId) {
        org.godot.Godot rtl = (org.godot.Godot) call("get_node", "RichTextLabel");
        if (rtl != null && utMap.containsKey(utId)) {
            rtl.setProperty("text", "[bgcolor=yellow][color=black]" + utMap.get(utId) + "[/color][/bgcolor]");
        }
        appendLog("Utterance " + utId + " ended.\n");
        utMap.remove(utId);
    }

    @GodotMethod
    public void _onUtteranceError(int utId) {
        org.godot.Godot rtl = (org.godot.Godot) call("get_node", "RichTextLabel");
        if (rtl != null) {
            rtl.setProperty("text", "");
        }
        appendLog("Utterance " + utId + " canceled/failed.\n");
        utMap.remove(utId);
    }

    @GodotMethod
    public void _onButtonStopPressed() {
        org.godot.singleton.DisplayServer.singleton().call("tts_stop");
    }

    @GodotMethod
    public void _onButtonPausePressed() {
        org.godot.singleton.DisplayServer ds = org.godot.singleton.DisplayServer.singleton();
        org.godot.Godot pauseBtn = (org.godot.Godot) call("get_node", "ButtonPause");
        if (pauseBtn != null && (boolean) pauseBtn.getProperty("pressed")) {
            ds.call("tts_pause");
        } else {
            ds.call("tts_resume");
        }
    }

    @GodotMethod
    public void _onButtonSpeakPressed() {
        org.godot.Godot tree = (org.godot.Godot) call("get_node", "Tree");
        if (tree == null) return;

        org.godot.Godot selected = (org.godot.Godot) tree.call("get_selected");
        if (selected != null) {
            appendLog("Utterance " + id + " queried.\n");
            org.godot.Godot utterance = (org.godot.Godot) call("get_node", "Utterance");
            String text = utterance != null ? (String) utterance.getProperty("text") : "";
            utMap.put(id, text);
            String voiceId = (String) selected.call("get_metadata", 0);
            org.godot.singleton.DisplayServer.singleton().call("tts_speak",
                    text, voiceId, getVolume(), getPitch(), getRate(), id, false);
            id++;
        } else {
            org.godot.singleton.OS.singleton().call("alert",
                    "No voice selected.\nSelect a voice in the list, then try using Speak again.", "");
        }
    }

    @GodotMethod
    public void _onButtonIntSpeakPressed() {
        org.godot.Godot tree = (org.godot.Godot) call("get_node", "Tree");
        if (tree == null) return;

        org.godot.Godot selected = (org.godot.Godot) tree.call("get_selected");
        if (selected != null) {
            appendLog("Utterance " + id + " interrupted.\n");
            org.godot.Godot utterance = (org.godot.Godot) call("get_node", "Utterance");
            String text = utterance != null ? (String) utterance.getProperty("text") : "";
            utMap.put(id, text);
            String voiceId = (String) selected.call("get_metadata", 0);
            org.godot.singleton.DisplayServer.singleton().call("tts_speak",
                    text, voiceId, getVolume(), getPitch(), getRate(), id, true);
            id++;
        } else {
            org.godot.singleton.OS.singleton().call("alert",
                    "No voice selected.\nSelect a voice in the list, then try using Interrupt again.", "");
        }
    }

    @GodotMethod
    public void _onButtonClearLogPressed() {
        org.godot.Godot log = (org.godot.Godot) call("get_node", "Log");
        if (log != null) log.setProperty("text", "");
    }

    @GodotMethod
    public void _onHSliderRateValueChanged(double value) {
        org.godot.Godot valueLabel = (org.godot.Godot) call("get_node", "HSliderRate/Value");
        if (valueLabel != null) valueLabel.setProperty("text", String.format("%.2fx", value));
    }

    @GodotMethod
    public void _onHSliderPitchValueChanged(double value) {
        org.godot.Godot valueLabel = (org.godot.Godot) call("get_node", "HSliderPitch/Value");
        if (valueLabel != null) valueLabel.setProperty("text", String.format("%.2fx", value));
    }

    @GodotMethod
    public void _onHSliderVolumeValueChanged(double value) {
        org.godot.Godot valueLabel = (org.godot.Godot) call("get_node", "HSliderVolume/Value");
        if (valueLabel != null) valueLabel.setProperty("text", String.format("%d%%", (int) value));
    }

    @GodotMethod
    public void _onButtonPressed() {
        org.godot.singleton.DisplayServer ds = org.godot.singleton.DisplayServer.singleton();

        // Demo - en
        org.godot.collection.GodotArray vc = (org.godot.collection.GodotArray) ds.call("tts_get_voices_for_language", "en");
        if (vc.size() > 0) {
            String voiceId = (String) ((org.godot.Godot) vc.get(0)).getProperty("id");
            utMap.put(id, "Beware the Jabberwock, my son!");
            utMap.put(id + 1, "The jaws that bite, the claws that catch!");
            ds.call("tts_speak", "Beware the Jabberwock, my son!", voiceId, getVolume(), getPitch(), getRate(), id);
            ds.call("tts_speak", "The jaws that bite, the claws that catch!", voiceId, getVolume(), getPitch(), getRate(), id + 1);
            id += 2;
        }

        // Demo - es
        vc = (org.godot.collection.GodotArray) ds.call("tts_get_voices_for_language", "es");
        if (vc.size() > 0) {
            String voiceId = (String) ((org.godot.Godot) vc.get(0)).getProperty("id");
            utMap.put(id, "¡Cuidado, hijo, con el Fablistanón!");
            utMap.put(id + 1, "¡Con sus dientes y garras, muerde, apresa!");
            ds.call("tts_speak", "¡Cuidado, hijo, con el Fablistanón!", voiceId, getVolume(), getPitch(), getRate(), id);
            ds.call("tts_speak", "¡Con sus dientes y garras, muerde, apresa!", voiceId, getVolume(), getPitch(), getRate(), id + 1);
            id += 2;
        }

        // Demo - ru
        vc = (org.godot.collection.GodotArray) ds.call("tts_get_voices_for_language", "ru");
        if (vc.size() > 0) {
            String voiceId = (String) ((org.godot.Godot) vc.get(0)).getProperty("id");
            utMap.put(id, "О, бойся Бармаглота, сын!");
            utMap.put(id + 1, "Он так свирлеп и дик!");
            ds.call("tts_speak", "О, бойся Бармаглота, сын!", voiceId, getVolume(), getPitch(), getRate(), id);
            ds.call("tts_speak", "Он так свирлеп и дик!", voiceId, getVolume(), getPitch(), getRate(), id + 1);
            id += 2;
        }
    }

    @GodotMethod
    public void _onLineEditFilterNameTextChanged(String newText) {
        org.godot.Godot tree = (org.godot.Godot) call("get_node", "Tree");
        if (tree == null) return;

        tree.call("clear");
        org.godot.Godot root = (org.godot.Godot) tree.call("create_item");

        org.godot.Godot filterName = (org.godot.Godot) call("get_node", "LineEditFilterName");
        org.godot.Godot filterLang = (org.godot.Godot) call("get_node", "LineEditFilterLang");
        String nameFilter = filterName != null ? (String) filterName.getProperty("text") : "";
        String langFilter = filterLang != null ? (String) filterLang.getProperty("text") : "";

        for (int vi = 0; vi < vs.size(); vi++) {
            org.godot.Godot v = (org.godot.Godot) vs.get(vi);
            String vName = (String) v.getProperty("name");
            String vLang = (String) v.getProperty("language");

            boolean nameMatch = nameFilter.isEmpty() || vName.toLowerCase().contains(nameFilter.toLowerCase());
            boolean langMatch = langFilter.isEmpty() || vLang.toLowerCase().contains(langFilter.toLowerCase());

            if (nameMatch && langMatch) {
                org.godot.Godot child = (org.godot.Godot) tree.call("create_item", root);
                child.call("set_text", 0, vName);
                child.call("set_metadata", 0, v.getProperty("id"));
                child.call("set_text", 1, vLang);
            }
        }
    }

    private double getRate() {
        org.godot.Godot slider = (org.godot.Godot) call("get_node", "HSliderRate");
        return slider != null ? ((Number) slider.getProperty("value")).doubleValue() : 1.0;
    }

    private double getPitch() {
        org.godot.Godot slider = (org.godot.Godot) call("get_node", "HSliderPitch");
        return slider != null ? ((Number) slider.getProperty("value")).doubleValue() : 1.0;
    }

    private double getVolume() {
        org.godot.Godot slider = (org.godot.Godot) call("get_node", "HSliderVolume");
        return slider != null ? ((Number) slider.getProperty("value")).doubleValue() : 50.0;
    }

    private void appendLog(String text) {
        org.godot.Godot log = (org.godot.Godot) call("get_node", "Log");
        if (log != null) {
            String current = (String) log.getProperty("text");
            log.setProperty("text", current + text);
        }
    }
}
