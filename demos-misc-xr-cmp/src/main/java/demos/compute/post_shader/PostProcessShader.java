package demos.compute.post_shader;

import org.godot.annotation.GodotClass;
import org.godot.annotation.GodotMethod;
import org.godot.math.Vector2i;
import org.godot.node.CompositorEffect;
import org.godot.node.RDSamplerState;
import org.godot.node.RDShaderSPIRV;
import org.godot.node.RDShaderSource;
import org.godot.node.RDUniform;
import org.godot.node.RenderData;
import org.godot.node.RenderSceneBuffers;
import org.godot.node.RenderSceneBuffersRD;
import org.godot.node.RenderSceneData;
import org.godot.node.RenderingDevice;
import org.godot.node.UniformSetCacheRD;
import org.godot.singleton.RenderingServer;

@GodotClass(name = "PostProcessShader", parent = "CompositorEffect")
public class PostProcessShader extends CompositorEffect {

    private static final String TEMPLATE_SHADER =
            "#version 450\n"
                    + "\n"
                    + "#define MAX_VIEWS 2\n"
                    + "\n"
                    + "#include \"godot/scene_data_inc.glsl\"\n"
                    + "\n"
                    + "layout(local_size_x = 8, local_size_y = 8, local_size_z = 1) in;\n"
                    + "\n"
                    + "layout(set = 0, binding = 0, std140) uniform SceneDataBlock {\n"
                    + "    SceneData data;\n"
                    + "    SceneData prev_data;\n"
                    + "}\n"
                    + "scene_data_block;\n"
                    + "\n"
                    + "layout(rgba16f, set = 0, binding = 1) uniform image2D color_image;\n"
                    + "layout(set = 0, binding = 2) uniform sampler2D depth_texture;\n"
                    + "\n"
                    + "layout(push_constant, std430) uniform Params {\n"
                    + "    vec2 raster_size;\n"
                    + "    float view;\n"
                    + "    float pad;\n"
                    + "} params;\n"
                    + "\n"
                    + "void main() {\n"
                    + "    ivec2 uv = ivec2(gl_GlobalInvocationID.xy);\n"
                    + "    ivec2 size = ivec2(params.raster_size);\n"
                    + "    int view = int(params.view);\n"
                    + "\n"
                    + "    if (uv.x >= size.x || uv.y >= size.y) {\n"
                    + "        return;\n"
                    + "    }\n"
                    + "\n"
                    + "    vec2 uv_norm = vec2(uv) / params.raster_size;\n"
                    + "\n"
                    + "    vec4 color = imageLoad(color_image, uv);\n"
                    + "    float depth = texture(depth_texture, uv_norm).r;\n"
                    + "\n"
                    + "    #COMPUTE_CODE\n"
                    + "\n"
                    + "    imageStore(color_image, uv, color);\n"
                    + "}\n";

    private RenderingDevice rd;
    private long shader;
    private long pipeline;
    private long nearestSampler;

    private String shaderCode = "";
    private volatile boolean shaderIsDirty = true;

    private boolean initialized = false;

    @Override
    public void _ready() {
        if (initialized) return;
        initialized = true;

        setEffectCallbackType(4);

        Object codeObj = getProperty("shader_code");
        if (codeObj instanceof String code) {
            shaderCode = code;
        }
        shaderIsDirty = true;

        rd = RenderingServer.singleton().getRenderingDevice();
    }

    @Override
    public void onNotification(int what) {
        if (what == 1 && rd != null) {
            if (shader != 0) {
                rd.freeRid(shader);
                shader = 0;
                pipeline = 0;
            }
            if (nearestSampler != 0) {
                rd.freeRid(nearestSampler);
                nearestSampler = 0;
            }
        }
    }

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

        if (shaderIsDirty) {
            newShaderCode = shaderCode;
            shaderIsDirty = false;
        }

        if (newShaderCode.isEmpty()) {
            return pipeline != 0;
        }

        String fullCode = TEMPLATE_SHADER.replace("#COMPUTE_CODE", newShaderCode);

        if (shader != 0) {
            rd.freeRid(shader);
        }
        shader = 0;
        pipeline = 0;

        RDShaderSource shaderSource = RDShaderSource.create();
        shaderSource.setLanguage(0);
        shaderSource.setSourceCompute(fullCode);
        RDShaderSPIRV shaderSpirv = rd.shaderCompileSpirvFromSource(shaderSource);

        String compileError = shaderSpirv.getCompileErrorCompute();
        if (compileError != null && !compileError.isEmpty()) {
            System.err.println(compileError);
            System.err.println("In: " + fullCode);
            return false;
        }

        shader = rd.shaderCreateFromSpirv(shaderSpirv);
        if (shader == 0) return false;

        pipeline = rd.computePipelineCreate(shader);
        return pipeline != 0;
    }

    @GodotMethod
    public void _renderCallback(int effectCallbackType, Object renderData) {
        if (rd == null || effectCallbackType != 4) return;
        if (!checkShader()) return;
        if (!(renderData instanceof RenderData renderDataObj)) return;

        RenderSceneBuffers renderSceneBuffersBase = renderDataObj.getRenderSceneBuffers();
        RenderSceneData sceneData = renderDataObj.getRenderSceneData();
        if (!(renderSceneBuffersBase instanceof RenderSceneBuffersRD renderSceneBuffers) || sceneData == null) return;

        Vector2i size = renderSceneBuffers.getInternalSize();
        int sizeX = size.x;
        int sizeY = size.y;
        if (sizeX == 0 && sizeY == 0) return;

        int xGroups = (sizeX - 1) / 8 + 1;
        int yGroups = (sizeY - 1) / 8 + 1;
        int zGroups = 1;

        float[] pushConstant = new float[] { (float) sizeX, (float) sizeY, 0.0f, 0.0f };

        if (nearestSampler == 0) {
            RDSamplerState samplerState = RDSamplerState.create();
            samplerState.setMinFilter(0);
            samplerState.setMagFilter(0);
            nearestSampler = rd.samplerCreate(samplerState);
        }

        long viewCount = renderSceneBuffers.getViewCount();

        for (int view = 0; view < viewCount; view++) {
            long sceneDataBuffers = sceneData.getUniformBuffer();
            long colorImage = renderSceneBuffers.getColorLayer(view);
            long depthImage = renderSceneBuffers.getDepthLayer(view);

            RDUniform sceneDataUniform = RDUniform.create();
            sceneDataUniform.setUniformType(6);
            sceneDataUniform.setBinding(0);
            sceneDataUniform.addId(sceneDataBuffers);

            RDUniform colorUniform = RDUniform.create();
            colorUniform.setUniformType(9);
            colorUniform.setBinding(1);
            colorUniform.addId(colorImage);

            RDUniform depthUniform = RDUniform.create();
            depthUniform.setUniformType(4);
            depthUniform.setBinding(2);
            depthUniform.addId(nearestSampler);
            depthUniform.addId(depthImage);

            long uniformSetRid = UniformSetCacheRD.getCache(shader, 0,
                    new RDUniform[] { sceneDataUniform, colorUniform, depthUniform });

            pushConstant[2] = (float) view;
            byte[] pushConstantBytes = floatArrayToBytes(pushConstant);

            long computeList = rd.computeListBegin();
            rd.computeListBindComputePipeline(computeList, pipeline);
            rd.computeListBindUniformSet(computeList, uniformSetRid, 0);
            rd.computeListSetPushConstant(computeList, pushConstantBytes, pushConstantBytes.length);
            rd.computeListDispatch(computeList, xGroups, yGroups, zGroups);
            rd.computeListEnd();
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
