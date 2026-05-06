package demos.threed.voxel;

import org.godot.annotation.GodotClass;
import org.godot.node.Environment;
import org.godot.node.Node;
import org.godot.node.WorldEnvironment;

/**
 * Controls fog based on the VoxelWorld's effective render distance.
 * Port of world/environment.gd.
 */
@GodotClass(name = "VXEnvironment", parent = "WorldEnvironment")
public class VXEnvironment extends WorldEnvironment {

    private org.godot.node.Node voxelWorld;
    private VXSettings settings;
    private boolean initialized = false;

    @Override
    public void _ready() {
        if (initialized) return;
        initialized = true;

        voxelWorld = getNode("../VoxelWorld");
        Object settingsObj = getNode("/root/Settings");
        if (settingsObj instanceof VXSettings) {
            settings = (VXSettings) settingsObj;
        }
    }

    @Override
    public void _process(double delta) {
        Environment env = getEnvironment();
        if (env == null) return;
        if (settings == null) return;

        env.setFogEnabled(settings.fogEnabled);

        int effectiveRD = 2;
        int rd = settings.renderDistance;
        if (voxelWorld != null) {
            Object erd = voxelWorld.getProperty("effectiveRenderDistance");
            if (erd instanceof Integer) effectiveRD = (Integer) erd;
            else if (erd instanceof Long) effectiveRD = ((Long) erd).intValue();
        }
        effectiveRD = Math.max(2, Math.min(effectiveRD, rd - 1));
        int targetDistance = effectiveRD * VXChunk.CHUNK_SIZE;
        double rate = delta * 4.0;

        // Move fog distance toward target.
        double diff = targetDistance - settings.fogDistance;
        if (Math.abs(diff) <= rate) {
            settings.fogDistance = targetDistance;
        } else {
            settings.fogDistance += Math.signum(diff) * rate;
        }

        env.setFogDensity(0.5 / settings.fogDistance);
    }
}
