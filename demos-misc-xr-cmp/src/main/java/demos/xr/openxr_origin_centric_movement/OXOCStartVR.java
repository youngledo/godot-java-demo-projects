package demos.xr.openxr_origin_centric_movement;

import org.godot.annotation.GodotClass;
import org.godot.annotation.GodotMethod;
import org.godot.annotation.Signal;
import org.godot.node.Node3D;
import org.godot.node.Node;
import org.godot.node.SceneTree;
import org.godot.node.Viewport;

@GodotClass(name = "OXOCStartVR", parent = "Node3D")
public class OXOCStartVR extends Node3D {

    private org.godot.Godot xrInterface;
    private boolean xrIsFocused = false;
    private boolean initialized = false;

    @Signal
    public void poseRecentered() {}

    @Override
    public void _ready() {
        if (initialized) return;
        initialized = true;

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
    }

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
        emitSignal("pose_recentered");
    }
}
