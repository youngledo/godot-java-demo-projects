package demos.xr.webxr;

import org.godot.annotation.GodotClass;
import org.godot.annotation.GodotMethod;
import org.godot.core.Callable;
import org.godot.math.Vector2;
import org.godot.node.BaseButton;
import org.godot.node.CanvasItem;
import org.godot.node.Node3D;
import org.godot.node.Viewport;
import org.godot.node.WebXRInterface;
import org.godot.node.XRController3D;
import org.godot.node.XRControllerTracker;
import org.godot.node.XRPose;
import org.godot.singleton.OS;
import org.godot.singleton.XRServer;

@GodotClass(name = "WebXRMain", parent = "Node3D")
public class WebXRMain extends Node3D {

    private WebXRInterface webxrInterface;
    private boolean vrSupported = false;
    private XRController3D leftController;

    private boolean initialized = false;

    @Override
    public void _ready() {
        if (initialized) return;
        initialized = true;

        BaseButton enterVrButton = getNodeAs("CanvasLayer/EnterVRButton", BaseButton.class);
        if (enterVrButton != null) {
            enterVrButton.connect("pressed", new Callable(this, "OnEnterVrButtonPressed"));
        }

        leftController = getNodeAs("XROrigin3D/LeftController", XRController3D.class);

        if (XRServer.singleton().findInterface("WebXR") instanceof WebXRInterface webXr) {
            webxrInterface = webXr;
        }
        if (webxrInterface != null) {
            webxrInterface.connect("session_supported", new Callable(this, "WebxrSessionSupported"), 0);
            webxrInterface.connect("session_started", new Callable(this, "WebxrSessionStarted"), 0);
            webxrInterface.connect("session_ended", new Callable(this, "WebxrSessionEnded"), 0);
            webxrInterface.connect("session_failed", new Callable(this, "WebxrSessionFailed"), 0);
            webxrInterface.connect("select", new Callable(this, "WebxrOnSelect"), 0);
            webxrInterface.connect("selectstart", new Callable(this, "WebxrOnSelectStart"), 0);
            webxrInterface.connect("selectend", new Callable(this, "WebxrOnSelectEnd"), 0);
            webxrInterface.connect("squeeze", new Callable(this, "WebxrOnSqueeze"), 0);
            webxrInterface.connect("squeezestart", new Callable(this, "WebxrOnSqueezeStart"), 0);
            webxrInterface.connect("squeezeend", new Callable(this, "WebxrOnSqueezeEnd"), 0);

            webxrInterface.isSessionSupported("immersive-vr");
        }

        if (leftController != null) {
            leftController.connect("button_pressed", new Callable(this, "OnLeftControllerButtonPressed"));
            leftController.connect("button_released", new Callable(this, "OnLeftControllerButtonReleased"));
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
            OS.singleton().alert("Your browser doesn't support VR", "VR Not Supported");
            return;
        }

        webxrInterface.setSessionMode("immersive-vr");
        webxrInterface.setRequestedReferenceSpaceTypes("bounded-floor, local-floor, local");
        webxrInterface.setRequiredFeatures("local-floor");
        webxrInterface.setOptionalFeatures("bounded-floor");

        if (!webxrInterface.initialize()) {
            OS.singleton().alert("Failed to initialize WebXR", "Error");
        }
    }

    @GodotMethod
    public void WebxrSessionStarted() {
        CanvasItem canvasLayer = getNodeAs("CanvasLayer", CanvasItem.class);
        if (canvasLayer != null) canvasLayer.setVisible(false);

        Viewport vp = getViewport();
        if (vp != null) vp.setUseXr(true);

        System.out.println("Reference space type: " + webxrInterface.getReferenceSpaceType());
        System.out.println("Enabled features: " + webxrInterface.getEnabledFeatures());
    }

    @GodotMethod
    public void WebxrSessionEnded() {
        CanvasItem canvasLayer = getNodeAs("CanvasLayer", CanvasItem.class);
        if (canvasLayer != null) canvasLayer.setVisible(true);

        Viewport vp = getViewport();
        if (vp != null) vp.setUseXr(false);
    }

    @GodotMethod
    public void WebxrSessionFailed(String message) {
        OS.singleton().alert("Failed to initialize: " + message, "Error");
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
        Vector2 thumbstickVector = leftController.getVector2("thumbstick");
        if (thumbstickVector.x != 0.0 || thumbstickVector.y != 0.0) {
            System.out.println("Left thumbstick position: " + thumbstickVector);
        }
    }

    @GodotMethod
    public void WebxrOnSelect(int inputSourceId) {
        System.out.println("Select: " + inputSourceId);
        XRControllerTracker tracker = webxrInterface.getInputSourceTracker(inputSourceId);
        if (tracker != null) {
            XRPose pose = tracker.getPose("default");
            if (pose != null) {
                System.out.println(pose.getAdjustedTransform().getOrigin());
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
