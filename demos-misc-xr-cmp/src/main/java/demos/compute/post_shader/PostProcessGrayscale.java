package demos.compute.post_shader;

import org.godot.annotation.GodotClass;
import org.godot.annotation.GodotMethod;
import org.godot.node.CompositorEffect;

/**
 * Port of compute/post_shader/post_process_grayscale.gd
 *
 * A CompositorEffect that applies a grayscale post-processing effect
 * using a compute shader via the RenderingDevice API.
 *
 * Note: This is a @tool script that runs on the rendering thread.
 * In Java, we use call() extensively to access RenderingDevice methods.
 */
@GodotClass(name = "PostProcessGrayscale", parent = "CompositorEffect")
public class PostProcessGrayscale extends CompositorEffect {

    private Object rd; // RenderingDevice
    private Object shader; // RID
    private Object pipeline; // RID

    private boolean initialized = false;

    @Override
    public void _ready() {
        if (initialized) return;
        initialized = true;

        // Set effect callback type to post-transparent (4)
        setProperty("effect_callback_type", 4);

        // Get the rendering device
        rd = call("RenderingServer.get_rendering_device");

        // Initialize compute on the render thread
        call("RenderingServer.call_on_render_thread", "_initializeCompute");
    }

    public void _notification(int what) {
        // NOTIFICATION_PREDELETE = 1
        if (what == 1) {
            if (rd != null && shader != null) {
                boolean valid = (boolean) callOn(rd, "rid_is_valid", shader);
                if (valid) {
                    callOn(rd, "free_rid", shader);
                }
            }
        }
    }

    @GodotMethod
    public void _initializeCompute() {
        // Re-get rendering device (may be on different thread)
        rd = call("RenderingServer.get_rendering_device");
        if (rd == null) return;

        // Load the shader file
        Object shaderFile = call("load", "res://post_process_grayscale.glsl");
        Object shaderSpirv = callOn(shaderFile, "get_spirv");

        shader = callOn(rd, "shader_create_from_spirv", shaderSpirv);
        if (shader != null) {
            boolean valid = (boolean) callOn(rd, "rid_is_valid", shader);
            if (valid) {
                pipeline = callOn(rd, "compute_pipeline_create", shader);
            }
        }
    }

    @GodotMethod
    public void _renderCallback(int effectCallbackType, Object renderData) {
        // EFFECT_CALLBACK_TYPE_POST_TRANSPARENT = 4
        if (rd == null || effectCallbackType != 4) return;

        boolean pipelineValid = pipeline != null && (boolean) callOn(rd, "rid_is_valid", pipeline);
        if (!pipelineValid) return;

        // Get render scene buffers
        Object renderSceneBuffers = callOn(renderData, "get_render_scene_buffers");
        if (renderSceneBuffers == null) return;

        // Get internal size
        Object sizeObj = callOn(renderSceneBuffers, "get_internal_size");
        int sizeX, sizeY;
        if (sizeObj instanceof org.godot.math.Vector2i) {
            org.godot.math.Vector2i size = (org.godot.math.Vector2i) sizeObj;
            sizeX = size.x;
            sizeY = size.y;
        } else {
            return;
        }

        if (sizeX == 0 && sizeY == 0) return;

        // Calculate dispatch groups
        int xGroups = (sizeX - 1) / 8 + 1;
        int yGroups = (sizeY - 1) / 8 + 1;
        int zGroups = 1;

        // Create push constant (4 floats = 16 bytes)
        float[] pushConstant = new float[]{
            (float) sizeX,
            (float) sizeY,
            0.0f,
            0.0f
        };

        // Get view count
        int viewCount = ((Number) callOn(renderSceneBuffers, "get_view_count")).intValue();

        for (int view = 0; view < viewCount; view++) {
            // Get color image
            Object inputImage = callOn(renderSceneBuffers, "get_color_layer", view);

            // Create uniform
            Object uniform = call("RDUniform.new");
            callOn(uniform, "set", "uniform_type", 9); // UNIFORM_TYPE_IMAGE
            callOn(uniform, "set", "binding", 0);
            callOn(uniform, "add_id", inputImage);

            // Get cached uniform set
            Object uniformSet = call("UniformSetCacheRD.get_cache", shader, 0, new Object[]{uniform});

            // Convert push constant to byte array
            byte[] pushConstantBytes = floatArrayToBytes(pushConstant);

            // Run compute shader
            Object computeList = callOn(rd, "compute_list_begin");
            callOn(rd, "compute_list_bind_compute_pipeline", computeList, pipeline);
            callOn(rd, "compute_list_bind_uniform_set", computeList, uniformSet, 0);
            callOn(rd, "compute_list_set_push_constant", computeList, pushConstantBytes, pushConstantBytes.length);
            callOn(rd, "compute_list_dispatch", computeList, xGroups, yGroups, zGroups);
            callOn(rd, "compute_list_end");
        }
    }

    private Object callOn(Object obj, String method, Object... args) {
        if (obj instanceof org.godot.Godot) {
            return ((org.godot.Godot) obj).call(method, args);
        }
        return null;
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
