package demos.xr.openxr_render_models;

import org.godot.annotation.GodotClass;
import org.godot.annotation.GodotMethod;
import org.godot.collection.GodotArray;
import org.godot.core.Callable;
import org.godot.node.Node3D;
import org.godot.node.OpenXRInterface;
import org.godot.node.SceneTree;
import org.godot.node.Viewport;
import org.godot.singleton.DisplayServer;
import org.godot.singleton.Engine;
import org.godot.singleton.RenderingServer;
import org.godot.singleton.XRServer;

@GodotClass(name = "OXRMStartVR", parent = "Node3D")
public class OXRMStartVR extends Node3D {

    private OpenXRInterface xrInterface;
    private boolean xrIsFocused = false;
    private boolean initialized = false;

    @Override
    public void _ready() {
        if (initialized) return;
        initialized = true;

        if (XRServer.singleton().findInterface("OpenXR") instanceof OpenXRInterface openXr) {
            xrInterface = openXr;
        }
        if (xrInterface != null && xrInterface.isInitialized()) {
            System.out.println("OpenXR instantiated successfully.");
            Viewport vp = getViewport();

            vp.setUseXr(true);
            DisplayServer.singleton().windowSetVsyncMode(1);

            if (RenderingServer.singleton().getRenderingDevice() != null) {
                vp.setVrsMode(2);
            }

            xrInterface.connect("session_begun", new Callable(this, "OnOpenxrSessionBegun"), 0);
            xrInterface.connect("session_visible", new Callable(this, "OnOpenxrVisibleState"), 0);
            xrInterface.connect("session_focussed", new Callable(this, "OnOpenxrFocusedState"), 0);
            xrInterface.connect("session_stopping", new Callable(this, "OnOpenxrStopping"), 0);
            xrInterface.connect("pose_recentered", new Callable(this, "OnOpenxrPoseRecentered"), 0);
        } else {
            System.out.println("OpenXR not instantiated!");
            SceneTree tree = getTree();
            if (tree != null) tree.quit();
        }
    }

    @GodotMethod
    public void OnOpenxrSessionBegun() {
        if (xrInterface == null) return;

        double currentRefreshRate = xrInterface.getDisplayRefreshRate();
        if (currentRefreshRate > 0) {
            System.out.println("OpenXR: Refresh rate reported as " + currentRefreshRate);
        } else {
            System.out.println("OpenXR: No refresh rate given by XR runtime");
        }

        double newRate = currentRefreshRate;
        GodotArray availableRates = xrInterface.getAvailableDisplayRefreshRates();
        if (availableRates == null || availableRates.size() == 0) {
            System.out.println("OpenXR: Target does not support refresh rate extension");
        } else if (availableRates.size() == 1) {
            Object rateObj = availableRates.get(0);
            if (rateObj instanceof Number number) {
                newRate = number.doubleValue();
            }
        } else {
            int maxRefreshRate = maximumRefreshRate();
            for (int i = 0; i < availableRates.size(); i++) {
                Object rateObj = availableRates.get(i);
                if (rateObj instanceof Number number) {
                    double rate = number.doubleValue();
                    if (rate > newRate && rate <= maxRefreshRate) {
                        newRate = rate;
                    }
                }
            }
        }

        if (currentRefreshRate != newRate) {
            System.out.println("OpenXR: Setting refresh rate to " + newRate);
            xrInterface.setDisplayRefreshRate(newRate);
            currentRefreshRate = newRate;
        }

        Engine.singleton().setPhysicsTicksPerSecond((int) Math.round(currentRefreshRate));
    }

    @GodotMethod
    public void OnOpenxrVisibleState() {
        if (xrIsFocused) {
            System.out.println("OpenXR lost focus");
            xrIsFocused = false;
            setProcessMode(3);
        }
    }

    @GodotMethod
    public void OnOpenxrFocusedState() {
        System.out.println("OpenXR gained focus");
        xrIsFocused = true;
        setProcessMode(0);
    }

    @GodotMethod
    public void OnOpenxrStopping() {
        System.out.println("OpenXR is stopping");
    }

    @GodotMethod
    public void OnOpenxrPoseRecentered() {
    }

    private int maximumRefreshRate() {
        Object value = getProperty("maximum_refresh_rate");
        return value instanceof Number number ? number.intValue() : 90;
    }
}
