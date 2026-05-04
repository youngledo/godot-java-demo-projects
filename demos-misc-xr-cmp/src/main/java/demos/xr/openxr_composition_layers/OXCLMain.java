package demos.xr.openxr_composition_layers;

import org.godot.annotation.GodotClass;
import org.godot.annotation.GodotMethod;
import org.godot.node.Node3D;

@GodotClass(name = "MainComposition", parent = "Node3D")
public class OXCLMain extends Node3D {

    private org.godot.Godot xrInterface;
    private boolean xrIsFocused = false;
    private boolean initialized = false;

    // Pointer handling state (from original main.gd)
    private org.godot.Godot tween;
    private org.godot.Godot activeHand;

    @Override
    public void _ready() {
        if (initialized) return;
        initialized = true;

        // XR initialization
        xrInterface = (org.godot.Godot) call("find_interface", "OpenXR");
        if (xrInterface != null && (boolean) xrInterface.call("is_initialized")) {
            System.out.println("OpenXR instantiated successfully.");
            org.godot.Godot vp = (org.godot.Godot) call("get_viewport");

            vp.setProperty("use_xr", true);
            org.godot.singleton.DisplayServer.singleton().call("window_set_vsync_mode", 1);

            org.godot.Godot renderingServer = org.godot.singleton.RenderingServer.singleton();
            if (renderingServer != null && renderingServer.call("get_rendering_device") != null) {
                vp.setProperty("vrs_mode", 2);
            }

            org.godot.core.Callable sessionBegunCb = new org.godot.core.Callable(this, "_on_openxr_session_begun");
            org.godot.core.Callable sessionVisibleCb = new org.godot.core.Callable(this, "_on_openxr_visible_state");
            org.godot.core.Callable sessionFocussedCb = new org.godot.core.Callable(this, "_on_openxr_focused_state");
            org.godot.core.Callable sessionStoppingCb = new org.godot.core.Callable(this, "_on_openxr_stopping");
            org.godot.core.Callable poseRecenteredCb = new org.godot.core.Callable(this, "_on_openxr_pose_recentered");

            xrInterface.call("connect", "session_begun", sessionBegunCb);
            xrInterface.call("connect", "session_visible", sessionVisibleCb);
            xrInterface.call("connect", "session_focussed", sessionFocussedCb);
            xrInterface.call("connect", "session_stopping", sessionStoppingCb);
            xrInterface.call("connect", "pose_recentered", poseRecenteredCb);
        } else {
            System.out.println("OpenXR not instantiated!");
            org.godot.Godot tree = (org.godot.Godot) call("get_tree");
            if (tree != null) tree.call("quit");
        }

        // Pointer initialization (from original main.gd _ready)
        org.godot.Godot leftPointer = (org.godot.Godot) call("get_node", "XROrigin3D/LeftHand/Pointer");
        org.godot.Godot rightPointer = (org.godot.Godot) call("get_node", "XROrigin3D/RightHand/Pointer");

        if (leftPointer != null) leftPointer.setProperty("visible", false);
        if (rightPointer != null) rightPointer.setProperty("visible", true);

        activeHand = (org.godot.Godot) call("get_node", "XROrigin3D/RightHand");
    }

    // --- XR session callbacks ---

    @GodotMethod
    public void _on_openxr_session_begun() {
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

        org.godot.Godot engine = (org.godot.Godot) call("get_node", "/root/Engine");
        if (engine != null) {
            engine.setProperty("physics_ticks_per_second", (int) Math.round(currentRefreshRate));
        }
    }

    @GodotMethod
    public void _on_openxr_visible_state() {
        if (xrIsFocused) {
            System.out.println("OpenXR lost focus");
            xrIsFocused = false;
            setProperty("process_mode", 3);
        }
    }

    @GodotMethod
    public void _on_openxr_focused_state() {
        System.out.println("OpenXR gained focus");
        xrIsFocused = true;
        setProperty("process_mode", 0);
    }

    @GodotMethod
    public void _on_openxr_stopping() {
        System.out.println("OpenXR is stopping");
    }

    @GodotMethod
    public void _on_openxr_pose_recentered() {
        // Pose recentered signal.
    }

    // --- Pointer handling (from original main.gd) ---

    @GodotMethod
    public void _update_energy(double newValue) {
        if (activeHand == null) return;
        org.godot.Godot pointer = (org.godot.Godot) activeHand.call("get_node", "Pointer");
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
        tween = (org.godot.Godot) call("create_tween");
        if (tween != null) {
            org.godot.core.Callable updateCb = new org.godot.core.Callable(this, "_update_energy");
            tween.call("tween_method", updateCb, 5.0, 1.0, 0.5);
        }
    }

    @GodotMethod
    public void _on_left_hand_button_pressed(String actionName) {
        if (!"select".equals(actionName)) return;

        org.godot.Godot leftPointer = (org.godot.Godot) call("get_node", "XROrigin3D/LeftHand/Pointer");
        org.godot.Godot rightPointer = (org.godot.Godot) call("get_node", "XROrigin3D/RightHand/Pointer");
        if (leftPointer != null) leftPointer.setProperty("visible", true);
        if (rightPointer != null) rightPointer.setProperty("visible", false);

        activeHand = (org.godot.Godot) call("get_node", "XROrigin3D/LeftHand");

        org.godot.Godot equirect = (org.godot.Godot) call("get_node", "XROrigin3D/OpenXRCompositionLayerEquirect");
        if (equirect != null) equirect.setProperty("controller", activeHand);

        doTweenEnergy();
        if (activeHand != null) {
            activeHand.call("trigger_haptic_pulse", "haptic", 0.0, 1.0, 0.5, 0.0);
        }
    }

    @GodotMethod
    public void _on_right_hand_button_pressed(String actionName) {
        if (!"select".equals(actionName)) return;

        org.godot.Godot leftPointer = (org.godot.Godot) call("get_node", "XROrigin3D/LeftHand/Pointer");
        org.godot.Godot rightPointer = (org.godot.Godot) call("get_node", "XROrigin3D/RightHand/Pointer");
        if (leftPointer != null) leftPointer.setProperty("visible", false);
        if (rightPointer != null) rightPointer.setProperty("visible", true);

        activeHand = (org.godot.Godot) call("get_node", "XROrigin3D/RightHand");

        org.godot.Godot equirect = (org.godot.Godot) call("get_node", "XROrigin3D/OpenXRCompositionLayerEquirect");
        if (equirect != null) equirect.setProperty("controller", activeHand);

        doTweenEnergy();
        if (activeHand != null) {
            activeHand.call("trigger_haptic_pulse", "haptic", 0.0, 1.0, 0.5, 0.0);
        }
    }
}
