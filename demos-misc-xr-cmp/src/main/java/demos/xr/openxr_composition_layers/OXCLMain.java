package demos.xr.openxr_composition_layers;

import org.godot.annotation.GodotClass;
import org.godot.annotation.GodotMethod;
import org.godot.node.Node3D;
import org.godot.node.Node;
import org.godot.node.SceneTree;
import org.godot.node.Viewport;

@GodotClass(name = "MainComposition", parent = "Node3D")
public class OXCLMain extends Node3D {

    private org.godot.Godot xrInterface;
    private boolean xrIsFocused = false;
    private boolean initialized = false;

    // Pointer handling state (from original main.gd)
    private org.godot.Godot tween;
    private org.godot.node.Node activeHand;

    @Override
    public void _ready() {
        if (initialized) return;
        initialized = true;

        // XR initialization
        xrInterface = (org.godot.Godot) call("find_interface", "OpenXR");
        if (xrInterface != null && (boolean) xrInterface.call("is_initialized")) {
            System.out.println("OpenXR instantiated successfully.");
            org.godot.node.Viewport vp = getViewport();

            vp.setProperty("use_xr", true);
            org.godot.singleton.DisplayServer.singleton().windowSetVsyncMode(1);

            org.godot.Godot renderingServer = org.godot.singleton.RenderingServer.singleton();
            if (renderingServer != null && renderingServer.call("get_rendering_device") != null) {
                vp.setProperty("vrs_mode", 2);
            }

            org.godot.core.Callable sessionBegunCb = new org.godot.core.Callable(this, "_on_openxr_session_begun");
            org.godot.core.Callable sessionVisibleCb = new org.godot.core.Callable(this, "_on_openxr_visible_state");
            org.godot.core.Callable sessionFocussedCb = new org.godot.core.Callable(this, "_on_openxr_focused_state");
            org.godot.core.Callable sessionStoppingCb = new org.godot.core.Callable(this, "_on_openxr_stopping");
            org.godot.core.Callable poseRecenteredCb = new org.godot.core.Callable(this, "_on_openxr_pose_recentered");

            xrInterface.connect("session_begun", sessionBegunCb, 0);
            xrInterface.connect("session_visible", sessionVisibleCb, 0);
            xrInterface.connect("session_focussed", sessionFocussedCb, 0);
            xrInterface.connect("session_stopping", sessionStoppingCb, 0);
            xrInterface.connect("pose_recentered", poseRecenteredCb, 0);
        } else {
            System.out.println("OpenXR not instantiated!");
            org.godot.node.SceneTree tree = getTree();
            if (tree != null) tree.quit();
        }

        // Pointer initialization (from original main.gd _ready)
        org.godot.node.Node leftPointer = getNode("XROrigin3D/LeftHand/Pointer");
        org.godot.node.Node rightPointer = getNode("XROrigin3D/RightHand/Pointer");

        if (leftPointer != null) leftPointer.setProperty("visible", false);
        if (rightPointer != null) rightPointer.setProperty("visible", true);

        activeHand = getNode("XROrigin3D/RightHand");
    }

    // --- XR session callbacks ---

    @GodotMethod
    public void OnOpenxrSessionBegun() {
        double currentRefreshRate = (double) xrInterface.call("get_display_refresh_rate");
        if (currentRefreshRate > 0) {
            System.out.println("OpenXR: Refresh rate reported as " + currentRefreshRate);
        } else {
            System.out.println("OpenXR: No refresh rate given by XR runtime");
        }

        double newRate = currentRefreshRate;
        Object[] availableRates = (Object[]) xrInterface.call("get_available_display_refresh_rates");
        if (availableRates == null || availableRates.length == 0) {
            System.out.println("OpenXR: Target does not support refresh rate extension");
        } else if (availableRates.length == 1) {
            newRate = (double) availableRates[0];
        } else {
            int maxRefreshRate = (int) getProperty("maximum_refresh_rate");
            for (Object rateObj : availableRates) {
                double rate = (double) rateObj;
                if (rate > newRate && rate <= maxRefreshRate) {
                    newRate = rate;
                }
            }
        }

        if (currentRefreshRate != newRate) {
            System.out.println("OpenXR: Setting refresh rate to " + newRate);
            xrInterface.call("set_display_refresh_rate", newRate);
            currentRefreshRate = newRate;
        }

        org.godot.node.Node engine = getNode("/root/Engine");
        if (engine != null) {
            engine.setProperty("physics_ticks_per_second", (int) Math.round(currentRefreshRate));
        }
    }

    @GodotMethod
    public void OnOpenxrVisibleState() {
        if (xrIsFocused) {
            System.out.println("OpenXR lost focus");
            xrIsFocused = false;
            setProperty("process_mode", 3);
        }
    }

    @GodotMethod
    public void OnOpenxrFocusedState() {
        System.out.println("OpenXR gained focus");
        xrIsFocused = true;
        setProperty("process_mode", 0);
    }

    @GodotMethod
    public void OnOpenxrStopping() {
        System.out.println("OpenXR is stopping");
    }

    @GodotMethod
    public void OnOpenxrPoseRecentered() {
        // Pose recentered signal.
    }

    // --- Pointer handling (from original main.gd) ---

    @GodotMethod
    public void UpdateEnergy(double newValue) {
        if (activeHand == null) return;
        org.godot.Godot pointer = (org.godot.Godot) activeHand.getNode("Pointer");
        if (pointer == null) return;
        org.godot.Godot material = (org.godot.Godot) pointer.getProperty("material_override");
        if (material != null) {
            material.call("set_shader_parameter", "energy", newValue);
        }
    }

    private void doTweenEnergy() {
        if (tween != null) {
            tween.call("kill");
        }
        tween = (org.godot.Godot) createTween();
        if (tween != null) {
            org.godot.core.Callable updateCb = new org.godot.core.Callable(this, "_update_energy");
            tween.call("tween_method", updateCb, 5.0, 1.0, 0.5);
        }
    }

    @GodotMethod
    public void OnLeftHandButtonPressed(String actionName) {
        if (!"select".equals(actionName)) return;

        org.godot.node.Node leftPointer = getNode("XROrigin3D/LeftHand/Pointer");
        org.godot.node.Node rightPointer = getNode("XROrigin3D/RightHand/Pointer");
        if (leftPointer != null) leftPointer.setProperty("visible", true);
        if (rightPointer != null) rightPointer.setProperty("visible", false);

        activeHand = getNode("XROrigin3D/LeftHand");

        org.godot.node.Node equirect = getNode("XROrigin3D/OpenXRCompositionLayerEquirect");
        if (equirect != null) equirect.setProperty("controller", activeHand);

        doTweenEnergy();
        if (activeHand != null) {
            activeHand.call("trigger_haptic_pulse", "haptic", 0.0, 1.0, 0.5, 0.0);
        }
    }

    @GodotMethod
    public void OnRightHandButtonPressed(String actionName) {
        if (!"select".equals(actionName)) return;

        org.godot.node.Node leftPointer = getNode("XROrigin3D/LeftHand/Pointer");
        org.godot.node.Node rightPointer = getNode("XROrigin3D/RightHand/Pointer");
        if (leftPointer != null) leftPointer.setProperty("visible", false);
        if (rightPointer != null) rightPointer.setProperty("visible", true);

        activeHand = getNode("XROrigin3D/RightHand");

        org.godot.node.Node equirect = getNode("XROrigin3D/OpenXRCompositionLayerEquirect");
        if (equirect != null) equirect.setProperty("controller", activeHand);

        doTweenEnergy();
        if (activeHand != null) {
            activeHand.call("trigger_haptic_pulse", "haptic", 0.0, 1.0, 0.5, 0.0);
        }
    }
}
