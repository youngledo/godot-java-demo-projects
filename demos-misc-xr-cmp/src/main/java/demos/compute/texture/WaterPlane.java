package demos.compute.texture;

import org.godot.annotation.GodotClass;
import org.godot.annotation.GodotMethod;
import org.godot.node.Area3D;
import org.godot.math.Vector2;
import org.godot.math.Vector2i;
import org.godot.math.Vector3;
import org.godot.math.Vector4;

/**
 * Port of compute/texture/water_plane/water_plane.gd
 *
 * Water ripple effect using compute shaders.
 * Implements a classic 2D wave simulation on the GPU with:
 * - 3 rotating textures (current, previous, output)
 * - Mouse-driven and rain-driven wave sources
 * - RenderingDevice compute pipeline
 */
@GodotClass(name = "WaterPlane", parent = "Area3D")
public class WaterPlane extends Area3D {

    // Export properties
    private double rainSize = 3.0;
    private double mouseSize = 5.0;
    private Vector2i textureSize = new Vector2i(512, 512);
    private double damp = 1.0;

    private double t = 0.0;
    private double maxT = 0.1;

    private Object texture; // Texture2DRD
    private int nextTexture = 0;

    private Vector4 addWavePoint = new Vector4(0, 0, 0, 0);
    private Vector2 mousePos = new Vector2(0, 0);
    private boolean mousePressed = false;

    // GPU resources
    private Object rd; // RenderingDevice
    private Object shader; // RID
    private Object pipeline; // RID
    private Object[] textureRds = new Object[3]; // RID[3]
    private Object[] textureSets = new Object[9]; // RID[9]

    private boolean initialized = false;

    @Override
    public void _ready() {
        if (initialized) return;
        initialized = true;

        // Read export properties
        Object rsObj = getProperty("rain_size");
        if (rsObj instanceof Number) rainSize = ((Number) rsObj).doubleValue();
        Object msObj = getProperty("mouse_size");
        if (msObj instanceof Number) mouseSize = ((Number) msObj).doubleValue();
        Object tsObj = getProperty("texture_size");
        if (tsObj instanceof Vector2i) textureSize = (Vector2i) tsObj;
        Object dObj = getProperty("damp");
        if (dObj instanceof Number) damp = ((Number) dObj).doubleValue();

        // Initialize compute on render thread
        call("RenderingServer.call_on_render_thread", "_initializeComputeCode", textureSize);

        // Get material and set texture size
        Object meshInstance = getNode("MeshInstance3D");
        if (meshInstance != null) {
            Object material = callOn(meshInstance, "get", "material_override");
            if (material != null) {
                callOn(material, "set_shader_parameter", "effect_texture_size", textureSize);
                texture = callOn(material, "get_shader_parameter", "effect_texture");
            }
        }
    }

    @Override
    public void _exitTree() {
        // Clean up texture reference
        if (texture != null) {
            callOn(texture, "set", "texture_rd_rid", null);
        }

        // Free compute resources on render thread
        call("RenderingServer.call_on_render_thread", "_freeComputeResources");
    }

    @Override
    public boolean _input(Object inputEvent) {
        // Check if it's a mouse event
        String className = (String) callOn(inputEvent, "get_class");
        if ("InputEventMouseMotion".equals(className) || "InputEventMouseButton".equals(className)) {
            mousePos = (Vector2) callOn(inputEvent, "get", "global_position");
        }

        if ("InputEventMouseButton".equals(className)) {
            long buttonIndex = ((Number) callOn(inputEvent, "get", "button_index")).longValue();
            if (buttonIndex == 1) { // MOUSE_BUTTON_LEFT
                mousePressed = (boolean) callOn(inputEvent, "get", "pressed");
            }
        }

        return false;
    }

    private void checkMousePos() {
        // Raycast from camera to find where mouse intersects our water plane
        Object viewport = getViewport();
        if (viewport == null) return;
        Object camera = callOn(viewport, "get_camera_3d");
        if (camera == null) return;

        Object rayOrigin = callOn(camera, "project_ray_origin", mousePos);
        Object rayNormal = callOn(camera, "project_ray_normal", mousePos);

        // Create ray query parameters
        Object parameters = call("PhysicsRayQueryParameters3D.new");
        callOn(parameters, "set", "from", rayOrigin);
        // to = from + normal * 100
        Object rayNormalVec3 = rayNormal;
        if (rayNormalVec3 instanceof Vector3) {
            Vector3 to = ((Vector3) rayOrigin).add(((Vector3) rayNormalVec3).mul(100.0));
            callOn(parameters, "set", "to", to);
        } else {
            Object to = callOn(rayNormal, "mul", 100.0);
            to = callOn(rayOrigin, "add", to);
            callOn(parameters, "set", "to", to);
        }
        callOn(parameters, "set", "collision_mask", 1);
        callOn(parameters, "set", "collide_with_bodies", false);
        callOn(parameters, "set", "collide_with_areas", true);

        Object world3d = call("get_world_3d");
        Object directSpaceState = callOn(world3d, "get", "direct_space_state");
        Object result = callOn(directSpaceState, "intersect_ray", parameters);

        // Check if result is not empty
        boolean empty = (boolean) callOn(result, "is_empty");
        if (!empty) {
            Object position = callOn(result, "get", "position");
            Object globalTransform = call("get_global_transform");
            Object pos = callOn(globalTransform, "affine_inverse");
            if (position instanceof Vector3 && pos instanceof org.godot.math.Transform3D) {
                Vector3 pos3d = ((org.godot.math.Transform3D) pos).apply((Vector3) position);
                double px = pos3d.x;
                double pz = pos3d.z;
                addWavePoint.x = clamp(px / 5.0, -0.5, 0.5) * textureSize.x + 0.5 * textureSize.x;
                addWavePoint.y = clamp(pz / 5.0, -0.5, 0.5) * textureSize.y + 0.5 * textureSize.y;
                addWavePoint.w = 1.0;
            }
        } else {
            addWavePoint.x = 0.0;
            addWavePoint.y = 0.0;
            addWavePoint.w = 0.0;
        }
    }

    @Override
    public void _process(double delta) {
        // Check mouse position
        checkMousePos();

        // If mouse not over water, animate rain drops
        if (addWavePoint.w == 0.0) {
            t += delta;
            if (t > maxT) {
                t = 0;
                addWavePoint.x = randiRange(0, textureSize.x);
                addWavePoint.y = randiRange(0, textureSize.y);
                addWavePoint.z = rainSize;
            } else {
                addWavePoint.z = 0.0;
            }
        } else {
            addWavePoint.z = mousePressed ? mouseSize : 0.0;
        }

        // Increase next texture index
        nextTexture = (nextTexture + 1) % 3;

        // Update texture to show next result
        if (texture != null && textureRds[nextTexture] != null) {
            callOn(texture, "set", "texture_rd_rid", textureRds[nextTexture]);
        }

        // Run render process on render thread
        call("RenderingServer.call_on_render_thread",
            "_renderProcess", nextTexture, addWavePoint, textureSize, damp);
    }

    @GodotMethod
    public void _initializeComputeCode(Object initWithTextureSize) {
        // Get the main rendering device
        rd = call("RenderingServer.get_rendering_device");

        // Load shader
        Object shaderFile = call("load", "res://water_plane/water_compute.glsl");
        Object shaderSpirv = callOn(shaderFile, "get_spirv");
        shader = callOn(rd, "shader_create_from_spirv", shaderSpirv);
        pipeline = callOn(rd, "compute_pipeline_create", shader);

        // Create texture format
        Object tf = call("RDTextureFormat.new");
        callOn(tf, "set", "format", 96); // DATA_FORMAT_R32_SFLOAT
        callOn(tf, "set", "texture_type", 0); // TEXTURE_TYPE_2D

        int texWidth, texHeight;
        if (initWithTextureSize instanceof Vector2i) {
            Vector2i ts = (Vector2i) initWithTextureSize;
            texWidth = ts.x;
            texHeight = ts.y;
        } else {
            texWidth = textureSize.x;
            texHeight = textureSize.y;
        }

        callOn(tf, "set", "width", texWidth);
        callOn(tf, "set", "height", texHeight);
        callOn(tf, "set", "depth", 1);
        callOn(tf, "set", "array_layers", 1);
        callOn(tf, "set", "mipmaps", 1);
        // TEXTURE_USAGE_SAMPLING_BIT | TEXTURE_USAGE_STORAGE_BIT | TEXTURE_USAGE_CAN_COPY_TO_BIT
        callOn(tf, "set", "usage_bits", 1 | 8 | 32);

        // Create 3 textures
        Object textureView = call("RDTextureView.new");
        for (int i = 0; i < 3; i++) {
            textureRds[i] = callOn(rd, "texture_create", tf, textureView, new Object[]{});
            // Clear textures
            callOn(rd, "texture_clear", textureRds[i],
                new org.godot.math.Color(0, 0, 0, 0), 0, 1, 0, 1);
        }

        // Create uniform sets for all texture combinations
        for (int i = 0; i < 3; i++) {
            Object nextTextureRd = textureRds[i];
            Object currentTextureRd = textureRds[(i + 2) % 3];
            Object previousTextureRd = textureRds[(i + 1) % 3];

            textureSets[i * 3 + 0] = createUniformSet(currentTextureRd, 0);
            textureSets[i * 3 + 1] = createUniformSet(previousTextureRd, 1);
            textureSets[i * 3 + 2] = createUniformSet(nextTextureRd, 2);
        }
    }

    private Object createUniformSet(Object textureRd, int uniformSet) {
        Object uniform = call("RDUniform.new");
        callOn(uniform, "set", "uniform_type", 9); // UNIFORM_TYPE_IMAGE
        callOn(uniform, "set", "binding", 0);
        callOn(uniform, "add_id", textureRd);
        return callOn(rd, "uniform_set_create", new Object[]{uniform}, shader, uniformSet);
    }

    @GodotMethod
    public void _renderProcess(int withNextTexture, Object wavePointObj, Object texSizeObj, double pDamp) {
        Vector4 wavePoint;
        if (wavePointObj instanceof Vector4) {
            wavePoint = (Vector4) wavePointObj;
        } else {
            return;
        }

        Vector2i texSize;
        if (texSizeObj instanceof Vector2i) {
            texSize = (Vector2i) texSizeObj;
        } else {
            return;
        }

        // Build push constant
        float[] pushConstant = new float[]{
            (float) wavePoint.x,
            (float) wavePoint.y,
            (float) wavePoint.z,
            (float) wavePoint.w,
            (float) texSize.x,
            (float) texSize.y,
            (float) pDamp,
            0.0f
        };

        // Calculate dispatch group size
        int xGroups = (texSize.x - 1) / 8 + 1;
        int yGroups = (texSize.y - 1) / 8 + 1;

        // Figure out which texture to assign to which set
        Object currentSet = textureSets[withNextTexture * 3];
        Object previousSet = textureSets[withNextTexture * 3 + 1];
        Object nextSet = textureSets[withNextTexture * 3 + 2];

        // Validate RIDs
        boolean pipelineValid = pipeline != null && (boolean) callOn(rd, "rid_is_valid", pipeline);
        boolean currentValid = currentSet != null && (boolean) callOn(rd, "rid_is_valid", currentSet);
        boolean previousValid = previousSet != null && (boolean) callOn(rd, "rid_is_valid", previousSet);
        boolean nextValid = nextSet != null && (boolean) callOn(rd, "rid_is_valid", nextSet);

        if (!(pipelineValid && currentValid && previousValid && nextValid)) return;

        // Run compute shader
        Object computeList = callOn(rd, "compute_list_begin");
        callOn(rd, "compute_list_bind_compute_pipeline", computeList, pipeline);
        callOn(rd, "compute_list_bind_uniform_set", computeList, currentSet, 0);
        callOn(rd, "compute_list_bind_uniform_set", computeList, previousSet, 1);
        callOn(rd, "compute_list_bind_uniform_set", computeList, nextSet, 2);

        byte[] pushConstantBytes = floatArrayToBytes(pushConstant);
        callOn(rd, "compute_list_set_push_constant", computeList, pushConstantBytes, pushConstantBytes.length);
        callOn(rd, "compute_list_dispatch", computeList, xGroups, yGroups, 1);
        callOn(rd, "compute_list_end");
    }

    @GodotMethod
    public void _freeComputeResources() {
        if (rd == null) return;

        for (int i = 0; i < 3; i++) {
            if (textureRds[i] != null) {
                callOn(rd, "free_rid", textureRds[i]);
            }
        }

        for (int i = 0; i < 9; i++) {
            if (textureSets[i] != null) {
                boolean valid = (boolean) callOn(rd, "rid_is_valid", textureSets[i]);
                if (valid) {
                    callOn(rd, "free_rid", textureSets[i]);
                }
            }
        }

        if (shader != null) {
            callOn(rd, "free_rid", shader);
        }
    }

    private Object callOn(Object obj, String method, Object... args) {
        if (obj instanceof org.godot.Godot) {
            return ((org.godot.Godot) obj).call(method, args);
        }
        return null;
    }

    private double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }

    private int randiRange(int from, int to) {
        Object result = call("randi_range", from, to);
        if (result instanceof Number) {
            return ((Number) result).intValue();
        }
        return from;
    }

    private static byte[] floatArrayToBytes(float[] floats) {
        byte[] bytes = new byte[floats.length * 4];
        for (int i = 0; i < floats.length; i++) {
            int bits = Float.floatToIntBits(floats[i]);
            bytes[i * 4] = (byte) (bits & 0xFF);
            bytes[i * 4 + 1] = (byte) ((bits >> 8) & 0xFF);
            bytes[i * 4 + 2] = (byte) ((bits >> 16) & 0xFF);
            bytes[i * 4 + 3] = (byte) ((bits >> 24) & 0xFF);
        }
        return bytes;
    }
}
