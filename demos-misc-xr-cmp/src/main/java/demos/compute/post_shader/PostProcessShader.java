package demos.compute.post_shader;

import org.godot.Godot;
import org.godot.annotation.GodotClass;
import org.godot.annotation.GodotMethod;
import org.godot.node.CompositorEffect;

/**
 * Port of compute/post_shader/post_process_shader.gd
 *
 * A CompositorEffect that applies a user-editable shader post-processing effect
 * using a compute shader via the RenderingDevice API.
 *
 * The shader code is defined in the shader_code export property, which is
 * inserted into a template compute shader at runtime.
 *
 * Note: This is a @tool script that runs on the rendering thread.
 * In Java, we use call() extensively to access RenderingDevice methods.
 */
@GodotClass(name = "PostProcessShader", parent = "CompositorEffect")
public class PostProcessShader extends CompositorEffect {

    private static final String TEMPLATE_SHADER =
        "#version 450\n" +
        "\n" +
        "#define MAX_VIEWS 2\n" +
        "\n" +
        "#include \"godot/scene_data_inc.glsl\"\n" +
        "\n" +
        "// Invocations in the (x, y, z) dimension.\n" +
        "layout(local_size_x = 8, local_size_y = 8, local_size_z = 1) in;\n" +
        "\n" +
        "layout(set = 0, binding = 0, std140) uniform SceneDataBlock {\n" +
        "    SceneData data;\n" +
        "    SceneData prev_data;\n" +
        "}\n" +
        "scene_data_block;\n" +
        "\n" +
        "layout(rgba16f, set = 0, binding = 1) uniform image2D color_image;\n" +
        "layout(set = 0, binding = 2) uniform sampler2D depth_texture;\n" +
        "\n" +
        "// Our push constant.\n" +
        "// Must be aligned to 16 bytes, just like the push constant we passed from the script.\n" +
        "layout(push_constant, std430) uniform Params {\n" +
        "    vec2 raster_size;\n" +
        "    float view;\n" +
        "    float pad;\n" +
        "} params;\n" +
        "\n" +
        "// The code we want to execute in each invocation.\n" +
        "void main() {\n" +
        "    ivec2 uv = ivec2(gl_GlobalInvocationID.xy);\n" +
        "    ivec2 size = ivec2(params.raster_size);\n" +
        "    int view = int(params.view);\n" +
        "\n" +
        "    if (uv.x >= size.x || uv.y >= size.y) {\n" +
        "        return;\n" +
        "    }\n" +
        "\n" +
        "    vec2 uv_norm = vec2(uv) / params.raster_size;\n" +
        "\n" +
        "    vec4 color = imageLoad(color_image, uv);\n" +
        "    float depth = texture(depth_texture, uv_norm).r;\n" +
        "\n" +
        "    #COMPUTE_CODE\n" +
        "\n" +
        "    imageStore(color_image, uv, color);\n" +
        "}\n";

    private Godot rd; // RenderingDevice
    private Godot shader; // RID
    private Godot pipeline; // RID
    private Godot nearestSampler; // RID

    private String shaderCode = "";
    private volatile boolean shaderIsDirty = true;

    private boolean initialized = false;

    @Override
    public void _ready() {
        if (initialized) return;
        initialized = true;

        // Set effect callback type to post-transparent (4)
        call("set", "effect_callback_type", 4);

        // Read shader_code property
        Object codeObj = getProperty("shader_code");
        if (codeObj instanceof String) {
            shaderCode = (String) codeObj;
        }
        shaderIsDirty = true;

        // Get the rendering device
        rd = (Godot) call("RenderingServer.get_rendering_device");
    }

    @Override
    public void onNotification(int what) {
        // NOTIFICATION_PREDELETE = 1
        if (what == 1) {
            if (rd != null) {
                if (shader != null) {
                    boolean valid = (boolean) rd.call("rid_is_valid", shader);
                    if (valid) {
                        // Freeing shader will also free dependents like pipeline
                        rd.call("free_rid", shader);
                    }
                }
                if (nearestSampler != null) {
                    boolean valid = (boolean) rd.call("rid_is_valid", nearestSampler);
                    if (valid) {
                        rd.call("free_rid", nearestSampler);
                    }
                }
            }
        }
    }

    /**
     * Called to set the shader code property.
     */
    public void setShaderCode(String value) {
        shaderCode = value;
        shaderIsDirty = true;
    }

    public String getShaderCode() {
        return shaderCode;
    }

    private boolean checkShader() {
        if (rd == null) return false;

        String newShaderCode = "";

        // Check if shader is dirty (thread-safe read)
        if (shaderIsDirty) {
            newShaderCode = shaderCode;
            shaderIsDirty = false;
        }

        // No new shader?
        if (newShaderCode.isEmpty() ) {
            if (pipeline != null) {
                return (boolean) rd.call("rid_is_valid", pipeline);
            }
            return false;
        }

        // Apply template
        String fullCode = TEMPLATE_SHADER.replace("#COMPUTE_CODE", newShaderCode);

        // Out with the old
        if (shader != null) {
            boolean valid = (boolean) rd.call("rid_is_valid", shader);
            if (valid) {
                rd.call("free_rid", shader);
            }
        }
        shader = null;
        pipeline = null;

        // In with the new
        Godot shaderSource = (Godot) call("RDShaderSource.new");
        shaderSource.setProperty("language", 0); // SHADER_LANGUAGE_GLSL
        shaderSource.setProperty("source_compute", fullCode);
        Godot shaderSpirv = (Godot) rd.call("shader_compile_spirv_from_source", shaderSource);

        String compileError = (String) shaderSpirv.getProperty("compile_error_compute");
        if (compileError != null && !compileError.isEmpty() ) {
            call("push_error", compileError);
            call("push_error", "In: " + fullCode);
            return false;
        }

        shader = (Godot) rd.call("shader_create_from_spirv", shaderSpirv);
        if (shader == null) return false;
        boolean shaderValid = (boolean) rd.call("rid_is_valid", shader);
        if (!shaderValid) return false;

        pipeline = (Godot) rd.call("compute_pipeline_create", shader);

        if (pipeline != null) {
            return (boolean) rd.call("rid_is_valid", pipeline);
        }
        return false;
    }

    @GodotMethod
    public void _renderCallback(int effectCallbackType, Object renderData) {
        // EFFECT_CALLBACK_TYPE_POST_TRANSPARENT = 4
        if (rd == null || effectCallbackType != 4) return;

        if (!checkShader()) return;

        Godot renderDataObj = (Godot) renderData;

        // Get render scene buffers and scene data
        Godot renderSceneBuffers = (Godot) renderDataObj.call("get_render_scene_buffers");
        Godot sceneData = (Godot) renderDataObj.call("get_render_scene_data");
        if (renderSceneBuffers == null || sceneData == null) return;

        // Get internal size
        Object sizeObj = renderSceneBuffers.call("get_internal_size");
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

        // Create push constant
        float[] pushConstant = new float[]{
            (float) sizeX,
            (float) sizeY,
            0.0f,
            0.0f
        };

        // Make sure we have a sampler
        if (nearestSampler == null || !(boolean) rd.call("rid_is_valid", nearestSampler)) {
            Godot samplerState = (Godot) call("RDSamplerState.new");
            samplerState.setProperty("min_filter", 0); // SAMPLER_FILTER_NEAREST
            samplerState.setProperty("mag_filter", 0);
            nearestSampler = (Godot) rd.call("sampler_create", samplerState);
        }

        // Loop through views
        int viewCount = ((Number) renderSceneBuffers.call("get_view_count")).intValue();

        for (int view = 0; view < viewCount; view++) {
            // Get scene data uniform buffer
            Object sceneDataBuffers = sceneData.call("get_uniform_buffer");

            // Get color image
            Object colorImage = renderSceneBuffers.call("get_color_layer", view);

            // Get depth image
            Object depthImage = renderSceneBuffers.call("get_depth_layer", view);

            // Create uniform set
            Godot sceneDataUniform = (Godot) call("RDUniform.new");
            sceneDataUniform.setProperty("uniform_type", 6); // UNIFORM_TYPE_UNIFORM_BUFFER
            sceneDataUniform.setProperty("binding", 0);
            sceneDataUniform.call("add_id", sceneDataBuffers);

            Godot colorUniform = (Godot) call("RDUniform.new");
            colorUniform.setProperty("uniform_type", 9); // UNIFORM_TYPE_IMAGE
            colorUniform.setProperty("binding", 1);
            colorUniform.call("add_id", colorImage);

            Godot depthUniform = (Godot) call("RDUniform.new");
            depthUniform.setProperty("uniform_type", 4); // UNIFORM_TYPE_SAMPLER_WITH_TEXTURE
            depthUniform.setProperty("binding", 2);
            depthUniform.call("add_id", nearestSampler);
            depthUniform.call("add_id", depthImage);

            // Get cached uniform set
            Object uniformSetRid = call("UniformSetCacheRD.get_cache", shader, 0,
                new Object[]{sceneDataUniform, colorUniform, depthUniform});

            // Set view in push constant
            pushConstant[2] = (float) view;

            // Convert push constant to byte array
            byte[] pushConstantBytes = floatArrayToBytes(pushConstant);

            // Run compute shader
            Object computeList = rd.call("compute_list_begin");
            rd.call("compute_list_bind_compute_pipeline", computeList, pipeline);
            rd.call("compute_list_bind_uniform_set", computeList, uniformSetRid, 0);
            rd.call("compute_list_set_push_constant", computeList, pushConstantBytes, pushConstantBytes.length);
            rd.call("compute_list_dispatch", computeList, xGroups, yGroups, zGroups);
            rd.call("compute_list_end");
        }
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
