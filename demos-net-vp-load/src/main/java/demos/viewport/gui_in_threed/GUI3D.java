package demos.viewport.gui_in_threed;

import org.godot.annotation.GodotClass;
import org.godot.annotation.GodotMethod;
import org.godot.core.Callable;
import org.godot.math.Vector2;
import org.godot.math.Vector3;
import org.godot.node.Area3D;
import org.godot.node.Camera3D;
import org.godot.node.MeshInstance3D;
import org.godot.node.Node3D;
import org.godot.node.SubViewport;
import org.godot.node.Node;

@GodotClass(name = "GUI3D", parent = "Node3D")
public class GUI3D extends Node3D {

    private boolean isMouseInside = false;
    private Vector2 lastEventPos2D = new Vector2();
    private double lastEventTime = -1.0;

    private SubViewport nodeViewport;
    private MeshInstance3D nodeQuad;
    private Area3D nodeArea;
    private boolean initialized = false;
    private boolean billboardEnabled = false;

    @Override
    public void _ready() {
        if (initialized) return;
        initialized = true;

        nodeViewport = (SubViewport) getNode("SubViewport");
        nodeQuad = (MeshInstance3D) getNode("Quad");
        nodeArea = (Area3D) getNode("Quad/Area3D");

        // Connect signals
        if (nodeArea != null) {
            nodeArea.connect("mouse_entered", new Callable(this, "_mouseEnteredArea"), 0);
            nodeArea.connect("mouse_exited", new Callable(this, "_mouseExitedArea"), 0);
            nodeArea.connect("input_event", new Callable(this, "_mouseInputEvent"), 0);
        }

        // Check if billboard is enabled
        if (nodeQuad != null) {
            Object material = nodeQuad.call("get_surface_override_material", 0);
            if (material != null) {
                Object billboardMode = ((org.godot.Godot) material).getProperty("billboard_mode");
                if (billboardMode instanceof Number) {
                    int mode = ((Number) billboardMode).intValue();
                    billboardEnabled = mode > 0;
                }
            }
        }

        if (!billboardEnabled) {
            setProcess(false);
        }
    }

    @Override
    public void _process(double delta) {
        if (billboardEnabled) {
            rotateAreaToBillboard();
        }
    }

    @Override
    public boolean _unhandledInput(Object inputEvent) {
        if (inputEvent instanceof org.godot.Godot) {
            org.godot.Godot evt = (org.godot.Godot) inputEvent;
            String className = (String) evt.call("get_class");

            // If the event is a mouse/touch event, ignore it - handled via physics picking
            if ("InputEventMouseButton".equals(className) ||
                "InputEventMouseMotion".equals(className) ||
                "InputEventScreenDrag".equals(className) ||
                "InputEventScreenTouch".equals(className)) {
                return false;
            }

            if (nodeViewport != null) {
                nodeViewport.call("push_input", evt);
            }
        }
        return false;
    }

    @GodotMethod
    public void _mouseEnteredArea() {
        isMouseInside = true;
        // Notify the viewport that the mouse is now hovering it
        if (nodeViewport != null) {
            nodeViewport.notification(42); // NOTIFICATION_VP_MOUSE_ENTER
        }
    }

    @GodotMethod
    public void _mouseExitedArea() {
        if (nodeViewport != null) {
            nodeViewport.notification(43); // NOTIFICATION_VP_MOUSE_EXIT
        }
        isMouseInside = false;
    }

    @GodotMethod
    public void _mouseInputEvent(Object camera, Object inputEvent, Object eventPosition, Object normal, Object shapeIdx) {
        if (nodeQuad == null || nodeViewport == null) return;

        // Get mesh size
        Object meshObj = nodeQuad.getProperty("mesh");
        Vector2 quadMeshSize = new Vector2(3, 2);
        if (meshObj != null) {
            Object sizeObj = ((org.godot.Godot) meshObj).getProperty("size");
            if (sizeObj instanceof Vector2) {
                quadMeshSize = (Vector2) sizeObj;
            }
        }

        Vector3 eventPos3D = (Vector3) eventPosition;
        if (eventPos3D == null) return;

        double now = System.currentTimeMillis() / 1000.0;

        // Convert position to a coordinate space relative to the Area3D node
        Object globalTransform = nodeQuad.call("get_global_transform");
        if (globalTransform != null) {
            Object inverse = ((org.godot.Godot) globalTransform).call("affine_inverse");
            if (inverse != null) {
                Object transformed = ((org.godot.Godot) inverse).call("xform", eventPos3D);
                if (transformed instanceof Vector3) {
                    eventPos3D = (Vector3) transformed;
                }
            }
        }

        Vector2 eventPos2D = new Vector2();

        if (isMouseInside) {
            eventPos2D = new Vector2(eventPos3D.getX(), -eventPos3D.getY());

            // Convert from (-quad_size/2 -> quad_size/2) to (0 -> 1)
            eventPos2D = new Vector2(
                eventPos2D.getX() / quadMeshSize.getX() + 0.5,
                eventPos2D.getY() / quadMeshSize.getY() + 0.5
            );

            // Convert to (0 -> viewport.size)
            Vector2 vpSize = (Vector2) nodeViewport.getProperty("size");
            if (vpSize != null) {
                eventPos2D = new Vector2(
                    eventPos2D.getX() * vpSize.getX(),
                    eventPos2D.getY() * vpSize.getY()
                );
            }
        } else {
            eventPos2D = lastEventPos2D;
        }

        org.godot.Godot evt = (org.godot.Godot) inputEvent;
        if (evt != null) {
            evt.setProperty("position", eventPos2D);

            String className = (String) evt.call("get_class");
            if ("InputEventMouse".equals(className)) {
                evt.setProperty("global_position", eventPos2D);
            }

            if ("InputEventMouseMotion".equals(className) || "InputEventScreenDrag".equals(className)) {
                Vector2 rel = new Vector2(eventPos2D.getX() - lastEventPos2D.getX(), eventPos2D.getY() - lastEventPos2D.getY());
                evt.setProperty("relative", rel);
                double dt = now - lastEventTime;
                if (dt > 0) {
                    evt.setProperty("velocity", new Vector2(rel.getX() / dt, rel.getY() / dt));
                }
            }
        }

        lastEventPos2D = eventPos2D;
        lastEventTime = now;

        if (nodeViewport != null && evt != null) {
            nodeViewport.call("push_input", evt);
        }
    }

    private void rotateAreaToBillboard() {
        if (nodeQuad == null || nodeArea == null) return;

        Object material = nodeQuad.call("get_surface_override_material", 0);
        if (material == null) return;

        Object billboardMode = ((org.godot.Godot) material).getProperty("billboard_mode");
        int mode = 0;
        if (billboardMode instanceof Number) {
            mode = ((Number) billboardMode).intValue();
        }

        if (mode > 0) {
            Object vp = getViewport();
            if (vp == null) return;

            Object cam = ((org.godot.Godot) vp).call("get_camera_3d");
            if (cam == null) return;

            org.godot.Godot camera = (org.godot.Godot) cam;

            Object camGlobalTransform = camera.getProperty("global_transform");
            if (camGlobalTransform == null) return;

            Vector3 camOrigin = (Vector3) ((org.godot.Godot) camGlobalTransform).getProperty("origin");
            Vector3 lookTarget = (Vector3) camera.call("to_global", new Vector3(0, 0, -100));
            if (lookTarget == null || camOrigin == null) return;

            Vector3 look = new Vector3(
                lookTarget.getX() - camOrigin.getX(),
                lookTarget.getY() - camOrigin.getY(),
                lookTarget.getZ() - camOrigin.getZ()
            );

            Vector3 areaPos = (Vector3) nodeArea.getProperty("position");
            if (areaPos == null) areaPos = new Vector3(0, 0, 0);

            look = new Vector3(
                areaPos.getX() + look.getX(),
                areaPos.getY() + look.getY(),
                areaPos.getZ() + look.getZ()
            );

            // Y-Billboard: Lock Y rotation
            if (mode == 2) {
                look = new Vector3(look.getX(), 0, look.getZ());
            }

            nodeArea.call("look_at", look, new Vector3(0, 1, 0));

            // Rotate in the Z axis to compensate camera tilt
            Vector3 camRotation = (Vector3) camera.getProperty("rotation");
            if (camRotation != null) {
                nodeArea.call("rotate_object_local", new Vector3(0, 0, 1), camRotation.getZ());
            }
        }
    }
}
