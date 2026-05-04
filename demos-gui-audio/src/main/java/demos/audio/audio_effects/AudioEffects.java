package demos.audio.audio_effects;

import org.godot.annotation.GodotClass;
import org.godot.annotation.GodotMethod;
import org.godot.node.Control;

/**
 * Audio effects demo - toggles various AudioServer bus effects on and off,
 * and plays sound effects and music.
 */
@GodotClass(name = "AudioEffects", parent = "Control")
public class AudioEffects extends Control {

    @GodotMethod
    public void _onToggleMusicToggled(boolean buttonPressed) {
        org.godot.Godot music = (org.godot.Godot) call("get_node", "SoundEffects/Music");
        if (music == null) return;
        if (buttonPressed) {
            music.call("play");
        } else {
            music.call("stop");
        }
    }

    @GodotMethod
    public void _onDingButtonPressed() {
        playSoundEffect("Ding");
    }

    @GodotMethod
    public void _onGlassButtonPressed() {
        playSoundEffect("Glass");
    }

    @GodotMethod
    public void _onMeowButtonPressed() {
        playSoundEffect("Meow");
    }

    @GodotMethod
    public void _onBeepsButtonPressed() {
        playSoundEffect("Beeps");
    }

    @GodotMethod
    public void _onTromboneButtonPressed() {
        playSoundEffect("Trombone");
    }

    @GodotMethod
    public void _onStaticButtonPressed() {
        playSoundEffect("Static");
    }

    @GodotMethod
    public void _onWhistleButtonPressed() {
        playSoundEffect("Whistle");
    }

    @GodotMethod
    public void _onToggleAmplifyToggled(boolean buttonPressed) {
        toggleEffect(0, buttonPressed);
    }

    @GodotMethod
    public void _onToggleBandLimiterToggled(boolean buttonPressed) {
        toggleEffect(1, buttonPressed);
    }

    @GodotMethod
    public void _onToggleBandPassFilterToggled(boolean buttonPressed) {
        toggleEffect(2, buttonPressed);
    }

    @GodotMethod
    public void _onToggleChorusToggled(boolean buttonPressed) {
        toggleEffect(3, buttonPressed);
    }

    @GodotMethod
    public void _onToggleCompressorToggled(boolean buttonPressed) {
        toggleEffect(4, buttonPressed);
    }

    @GodotMethod
    public void _onToggleDelayToggled(boolean buttonPressed) {
        toggleEffect(5, buttonPressed);
    }

    @GodotMethod
    public void _onToggleDistortionToggled(boolean buttonPressed) {
        toggleEffect(6, buttonPressed);
    }

    @GodotMethod
    public void _onToggleEq6Toggled(boolean buttonPressed) {
        toggleEffect(7, buttonPressed);
    }

    @GodotMethod
    public void _onToggleEq10Toggled(boolean buttonPressed) {
        toggleEffect(8, buttonPressed);
    }

    @GodotMethod
    public void _onToggleEq21Toggled(boolean buttonPressed) {
        toggleEffect(9, buttonPressed);
    }

    @GodotMethod
    public void _onToggleHighPassFilterToggled(boolean buttonPressed) {
        toggleEffect(10, buttonPressed);
    }

    @GodotMethod
    public void _onToggleLowShelfFilterToggled(boolean buttonPressed) {
        toggleEffect(11, buttonPressed);
    }

    @GodotMethod
    public void _onToggleNotchFilterToggled(boolean buttonPressed) {
        toggleEffect(12, buttonPressed);
    }

    @GodotMethod
    public void _onTogglePannerToggled(boolean buttonPressed) {
        toggleEffect(13, buttonPressed);
    }

    @GodotMethod
    public void _onTogglePhaserToggled(boolean buttonPressed) {
        toggleEffect(14, buttonPressed);
    }

    @GodotMethod
    public void _onTogglePitchShiftToggled(boolean buttonPressed) {
        toggleEffect(15, buttonPressed);
    }

    @GodotMethod
    public void _onToggleReverbToggled(boolean buttonPressed) {
        toggleEffect(16, buttonPressed);
    }

    @GodotMethod
    public void _onToggleStereoEnhanceToggled(boolean buttonPressed) {
        toggleEffect(17, buttonPressed);
    }

    private void playSoundEffect(String name) {
        org.godot.Godot sfx = (org.godot.Godot) call("get_node", "SoundEffects/" + name);
        if (sfx != null) sfx.call("play");
    }

    private void toggleEffect(int effectIndex, boolean enabled) {
        org.godot.singleton.AudioServer.singleton().call("set_bus_effect_enabled", 0, effectIndex, enabled);
    }
}
