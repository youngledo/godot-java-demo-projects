package demos.xr.webxr;

import org.godot.annotation.GodotClass;
import org.godot.annotation.GodotMethod;
import org.godot.node.Node3D;
import org.godot.node.Node;
import org.godot.node.Viewport;

@GodotClass(name = "WebXRMain", parent = "Node3D")
public class WebXRMain extends Node3D {

    private org.godot.Godot webxrInterface;
    private boolean vrSupported = false;
    private org.godot.node.Node leftController;

    private boolean initialized = false;

    @Override
    public void _ready() {
        if (initialized) return;
        initialized = true;

        org.godot.node.Node enterVrButton = getNode("CanvasLayer/EnterVRButton");
        if (enterVrButton != null) {
            org.godot.core.Callable pressedCb = new org.godot.core.Callable(this, "_on_enter_vr_button_pressed");
            enterVrButton.connect("pressed", pressedCb);
        }

        leftController = getNode("XROrigin3D/LeftController");

        webxrInterface = (org.godot.Godot) call("find_interface", "WebXR");
        if (webxrInterface != null) {
            // Connect WebXR signals.
            org.godot.core.Callable sessionSupportedCb = new org.godot.core.Callable(this, "_webxr_session_supported");
            org.godot.core.Callable sessionStartedCb = new org.godot.core.Callable(this, "_webxr_session_started");
            org.godot.core.Callable sessionEndedCb = new org.godot.core.Callable(this, "_webxr_session_ended");
            org.godot.core.Callable sessionFailedCb = new org.godot.core.Callable(this, "_webxr_session_failed");

            org.godot.core.Callable selectCb = new org.godot.core.Callable(this, "_webxr_on_select");
            org.godot.core.Callable selectStartCb = new org.godot.core.Callable(this, "_webxr_on_select_start");
            org.godot.core.Callable selectEndCb = new org.godot.core.Callable(this, "_webxr_on_select_end");

            org.godot.core.Callable squeezeCb = new org.godot.core.Callable(this, "_webxr_on_squeeze");
            org.godot.core.Callable squeezeStartCb = new org.godot.core.Callable(this, "_webxr_on_squeeze_start");
            org.godot.core.Callable squeezeEndCb = new org.godot.core.Callable(this, "_webxr_on_squeeze_end");

            webxrInterface.connect("session_supported", sessionSupportedCb, 0);
            webxrInterface.connect("session_started", sessionStartedCb, 0);
            webxrInterface.connect("session_ended", sessionEndedCb, 0);
            webxrInterface.connect("session_failed", sessionFailedCb, 0);

            webxrInterface.connect("select", selectCb, 0);
            webxrInterface.connect("selectstart", selectStartCb, 0);
            webxrInterface.connect("selectend", selectEndCb, 0);

            webxrInterface.connect("squeeze", squeezeCb, 0);
            webxrInterface.connect("squeezestart", squeezeStartCb, 0);
            webxrInterface.connect("squeezeend", squeezeEndCb, 0);

            webxrInterface.call("is_session_supported", "immersive-vr");
        }

        // Connect left controller signals.
        if (leftController != null) {
            org.godot.core.Callable btnPressedCb = new org.godot.core.Callable(this, "_on_left_controller_button_pressed");
            org.godot.core.Callable btnReleasedCb = new org.godot.core.Callable(this, "_on_left_controller_button_released");
            leftController.connect("button_pressed", btnPressedCb);
            leftController.connect("button_released", btnReleasedCb);
        }
    }

    @GodotMethod
    public void WebxrSessionSupported(String sessionMode, boolean supported) {
        if ("immersive-vr".equals(sessionMode)) {
            vrSupported = supported;
        }
    }

    @GodotMethod
    public void OnEnterVrButtonPressed() {
        if (!vrSupported) {
            call("alert", "Your browser doesn't support VR", "VR Not Supported");
            return;
        }

        webxrInterface.setProperty("session_mode", "immersive-vr");
        webxrInterface.setProperty("requested_reference_space_types", "bounded-floor, local-floor, local");
        webxrInterface.setProperty("required_features", "local-floor");
        webxrInterface.setProperty("optional_features", "bounded-floor");

        if (!(boolean) webxrInterface.call("initialize")) {
            call("alert", "Failed to initialize WebXR", "Error");
        }
    }

    @GodotMethod
    public void WebxrSessionStarted() {
        org.godot.node.Node canvasLayer = getNode("CanvasLayer");
        if (canvasLayer != null) canvasLayer.setProperty("visible", false);

        org.godot.node.Viewport vp = getViewport();
        if (vp != null) vp.setProperty("use_xr", true);

        String refSpaceType = (String) webxrInterface.getProperty("reference_space_type");
        System.out.println("Reference space type: " + refSpaceType);

        String enabledFeatures = (String) webxrInterface.getProperty("enabled_features");
        System.out.println("Enabled features: " + enabledFeatures);
    }

    @GodotMethod
    public void WebxrSessionEnded() {
        org.godot.node.Node canvasLayer = getNode("CanvasLayer");
        if (canvasLayer != null) canvasLayer.setProperty("visible", true);

        org.godot.node.Viewport vp = getViewport();
        if (vp != null) vp.setProperty("use_xr", false);
    }

    @GodotMethod
    public void WebxrSessionFailed(String message) {
        call("alert", "Failed to initialize: " + message, "Error");
    }

    @GodotMethod
    public void OnLeftControllerButtonPressed(String button) {
        System.out.println("Button pressed: " + button);
    }

    @GodotMethod
    public void OnLeftControllerButtonReleased(String button) {
        System.out.println("Button release: " + button);
    }

    @Override
    public void _process(double delta) {
        if (leftController == null) return;
        Object thumbstickObj = leftController.call("get_vector2", "thumbstick");
        if (thumbstickObj instanceof org.godot.math.Vector2) {
            org.godot.math.Vector2 thumbstickVector = (org.godot.math.Vector2) thumbstickObj;
            if (thumbstickVector.x != 0.0 || thumbstickVector.y != 0.0) {
                System.out.println("Left thumbstick position: " + thumbstickVector);
            }
        }
    }

    @GodotMethod
    public void WebxrOnSelect(int inputSourceId) {
        System.out.println("Select: " + inputSourceId);
        Object trackerObj = webxrInterface.call("get_input_source_tracker", inputSourceId);
        if (trackerObj != null) {
            org.godot.Godot tracker = (org.godot.Godot) trackerObj;
            Object poseObj = tracker.call("get_pose", "default");
            if (poseObj instanceof org.godot.math.Transform3D) {
                org.godot.math.Transform3D xform = (org.godot.math.Transform3D) poseObj;
                System.out.println(xform.getOrigin());
            }
        }
    }

    @GodotMethod
    public void WebxrOnSelectStart(int inputSourceId) {
        System.out.println("Select Start: " + inputSourceId);
    }

    @GodotMethod
    public void WebxrOnSelectEnd(int inputSourceId) {
        System.out.println("Select End: " + inputSourceId);
    }

    @GodotMethod
    public void WebxrOnSqueeze(int inputSourceId) {
        System.out.println("Squeeze: " + inputSourceId);
    }

    @GodotMethod
    public void WebxrOnSqueezeStart(int inputSourceId) {
        System.out.println("Squeeze Start: " + inputSourceId);
    }

    @GodotMethod
    public void WebxrOnSqueezeEnd(int inputSourceId) {
        System.out.println("Squeeze End: " + inputSourceId);
    }
}
