package demos.threed.platformer;

import org.godot.annotation.GodotClass;
import org.godot.node.DirectionalLight3D;
import org.godot.node.Node3D;
import org.godot.singleton.RenderingServer;

@GodotClass(name = "PLStage", parent = "Node3D")
public class PLStage extends Node3D {

    private boolean initialized = false;
    private DirectionalLight3D newLight;

    @Override
    public void _ready() {
        if (initialized) return;
        initialized = true;

        RenderingServer rs = RenderingServer.singleton();
        if (rs == null) return;

        if ("gl_compatibility".equals(rs.getCurrentRenderingMethod())) {
            rs.directionalSoftShadowFilterSetQuality(3);

            DirectionalLight3D light = getNodeAs("DirectionalLight3D", DirectionalLight3D.class);
            if (light != null && light.duplicate() instanceof DirectionalLight3D duplicated) {
                light.setSkyMode(0L);
                newLight = duplicated;
                newLight.setLightEnergy(0.25);
                newLight.setSkyMode(1L);
                addChild(newLight);
            }
        }
    }

    @Override
    public void _exitTree() {
        if (newLight != null) {
            newLight.queueFree();
            newLight = null;
        }
    }
}
