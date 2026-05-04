package demos.xr.openxr_render_models;

import org.godot.annotation.GodotClass;
import org.godot.annotation.GodotMethod;
import org.godot.node.Node3D;

@GodotClass(name = "OXRMStartVR", parent = "Node3D")
public class OXRMStartVR extends Node3D {

    private org.godot.Godot xrInterface;
    private boolean xrIsFocused = false;
    private boolean initialized = false;

    @Override
    public void _ready() {
        if (initialized) return;
        initialized = true;

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
    }

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
}
