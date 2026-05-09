package demos.viewport.gui_in_threed;

import org.godot.annotation.GodotClass;
import org.godot.annotation.GodotMethod;
import org.godot.builtin.Transform3DExtensions;
import org.godot.core.Callable;
import org.godot.math.Transform3D;
import org.godot.math.Vector2;
import org.godot.math.Vector2i;
import org.godot.math.Vector3;
import org.godot.node.Area3D;
import org.godot.node.BaseMaterial3D;
import org.godot.node.Camera3D;
import org.godot.node.InputEvent;
import org.godot.node.InputEventMouse;
import org.godot.node.InputEventMouseMotion;
import org.godot.node.InputEventScreenDrag;
import org.godot.node.InputEventScreenTouch;
import org.godot.node.Material;
import org.godot.node.Mesh;
import org.godot.node.MeshInstance3D;
import org.godot.node.Node3D;
import org.godot.node.PlaneMesh;
import org.godot.node.SubViewport;
import org.godot.node.Viewport;

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

        nodeViewport = getNodeAs("SubViewport", SubViewport.class);
        nodeQuad = getNodeAs("Quad", MeshInstance3D.class);
        nodeArea = getNodeAs("Quad/Area3D", Area3D.class);

        if (nodeArea != null) {
            nodeArea.connect("mouse_entered", new Callable(this, "_mouseEnteredArea"), 0);
            nodeArea.connect("mouse_exited", new Callable(this, "_mouseExitedArea"), 0);
            nodeArea.connect("input_event", new Callable(this, "_mouseInputEvent"), 0);
        }

        if (nodeQuad != null) {
            billboardEnabled = getBillboardMode() > 0;
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
        if (inputEvent instanceof InputEvent event && !isMouseOrTouchEvent(event) && nodeViewport != null) {
            nodeViewport.pushInput(event);
        }
        return false;
    }

    @GodotMethod
    public void _mouseEnteredArea() {
        isMouseInside = true;
        if (nodeViewport != null) {
            nodeViewport.notification(42);
        }
    }

    @GodotMethod
    public void _mouseExitedArea() {
        if (nodeViewport != null) {
            nodeViewport.notification(43);
        }
        isMouseInside = false;
    }

    @GodotMethod
    public void _mouseInputEvent(Object camera, Object inputEvent, Object eventPosition, Object normal, Object shapeIdx) {
        if (nodeQuad == null || nodeViewport == null || !(eventPosition instanceof Vector3 eventPos3D)) return;

        Vector2 quadMeshSize = getQuadMeshSize();
        double now = System.currentTimeMillis() / 1000.0;

        Transform3D inverse = Transform3DExtensions.affineInverse(nodeQuad.getGlobalTransform());
        eventPos3D = inverse.apply(eventPos3D);

        Vector2 eventPos2D;
        if (isMouseInside) {
            eventPos2D = new Vector2(eventPos3D.getX(), -eventPos3D.getY());
            eventPos2D = new Vector2(
                eventPos2D.getX() / quadMeshSize.getX() + 0.5,
                eventPos2D.getY() / quadMeshSize.getY() + 0.5
            );

            Vector2i vpSize = nodeViewport.getSize();
            eventPos2D = new Vector2(
                eventPos2D.getX() * vpSize.getX(),
                eventPos2D.getY() * vpSize.getY()
            );
        } else {
            eventPos2D = lastEventPos2D;
        }

        if (inputEvent instanceof InputEventMouse mouseEvent) {
            mouseEvent.setPosition(eventPos2D);
            mouseEvent.setGlobalPosition(eventPos2D);
            if (mouseEvent instanceof InputEventMouseMotion mouseMotion) {
                updateMotionEvent(mouseMotion, eventPos2D, now);
            }
        } else if (inputEvent instanceof InputEventScreenDrag screenDrag) {
            screenDrag.setPosition(eventPos2D);
            updateMotionEvent(screenDrag, eventPos2D, now);
        } else if (inputEvent instanceof InputEventScreenTouch screenTouch) {
            screenTouch.setPosition(eventPos2D);
        }

        lastEventPos2D = eventPos2D;
        lastEventTime = now;

        if (inputEvent instanceof InputEvent event) {
            nodeViewport.pushInput(event);
        }
    }

    private void rotateAreaToBillboard() {
        if (nodeQuad == null || nodeArea == null || getBillboardMode() <= 0) return;

        Viewport vp = getViewport();
        if (vp == null) return;

        Camera3D camera = vp.getCamera3d();
        if (camera == null) return;

        Vector3 camOrigin = camera.getGlobalTransform().getOrigin();
        Vector3 lookTarget = camera.toGlobal(new Vector3(0, 0, -100));
        if (lookTarget == null || camOrigin == null) return;

        Vector3 look = new Vector3(
            lookTarget.getX() - camOrigin.getX(),
            lookTarget.getY() - camOrigin.getY(),
            lookTarget.getZ() - camOrigin.getZ()
        );

        Vector3 areaPos = nodeArea.getPosition();
        if (areaPos == null) areaPos = new Vector3(0, 0, 0);

        look = new Vector3(
            areaPos.getX() + look.getX(),
            areaPos.getY() + look.getY(),
            areaPos.getZ() + look.getZ()
        );

        if (getBillboardMode() == 2) {
            look = new Vector3(look.getX(), 0, look.getZ());
        }

        nodeArea.lookAt(look, new Vector3(0, 1, 0));
        Vector3 camRotation = camera.getRotation();
        if (camRotation != null) {
            nodeArea.rotateObjectLocal(new Vector3(0, 0, 1), camRotation.getZ());
        }
    }

    private boolean isMouseOrTouchEvent(InputEvent event) {
        return event instanceof InputEventMouse || event instanceof InputEventScreenDrag || event instanceof InputEventScreenTouch;
    }

    private int getBillboardMode() {
        Material material = nodeQuad.getSurfaceOverrideMaterial(0);
        if (material instanceof BaseMaterial3D baseMaterial) {
            return (int) baseMaterial.getBillboardMode();
        }
        return 0;
    }

    private Vector2 getQuadMeshSize() {
        Mesh mesh = nodeQuad.getMesh();
        if (mesh instanceof PlaneMesh planeMesh) {
            return planeMesh.getSize();
        }
        return new Vector2(3, 2);
    }

    private void updateMotionEvent(InputEventMouseMotion event, Vector2 eventPos2D, double now) {
        Vector2 rel = getEventRelative(eventPos2D);
        event.setRelative(rel);
        setEventVelocity(event, rel, now);
    }

    private void updateMotionEvent(InputEventScreenDrag event, Vector2 eventPos2D, double now) {
        Vector2 rel = getEventRelative(eventPos2D);
        event.setRelative(rel);
        setEventVelocity(event, rel, now);
    }

    private Vector2 getEventRelative(Vector2 eventPos2D) {
        return new Vector2(eventPos2D.getX() - lastEventPos2D.getX(), eventPos2D.getY() - lastEventPos2D.getY());
    }

    private void setEventVelocity(InputEventMouseMotion event, Vector2 rel, double now) {
        double dt = now - lastEventTime;
        if (dt > 0) {
            event.setVelocity(new Vector2(rel.getX() / dt, rel.getY() / dt));
        }
    }

    private void setEventVelocity(InputEventScreenDrag event, Vector2 rel, double now) {
        double dt = now - lastEventTime;
        if (dt > 0) {
            event.setVelocity(new Vector2(rel.getX() / dt, rel.getY() / dt));
        }
    }
}
