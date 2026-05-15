package demos.compute.post_shader;

import org.godot.annotation.GodotClass;
import org.godot.annotation.GodotMethod;
import org.godot.core.Callable;
import org.godot.math.Vector2i;
import org.godot.collection.GodotArray;
import org.godot.node.CompositorEffect;
import org.godot.node.RDShaderFile;
import org.godot.node.RDShaderSPIRV;
import org.godot.node.RDUniform;
import org.godot.node.RenderData;
import org.godot.node.RenderSceneBuffers;
import org.godot.node.RenderSceneBuffersRD;
import org.godot.node.RenderingDevice;
import org.godot.node.Resource;
import org.godot.node.UniformSetCacheRD;
import org.godot.singleton.RenderingServer;
import org.godot.singleton.ResourceLoader;

@GodotClass(name = "PostProcessGrayscale", parent = "CompositorEffect")
public class PostProcessGrayscale extends CompositorEffect {

    private RenderingDevice rd;
    private long shader;
    private long pipeline;

    private boolean initialized = false;

    @Override
    public void _ready() {
        if (initialized) return;
        initialized = true;

        setEffectCallbackType(4);
        rd = RenderingServer.singleton().getRenderingDevice();
        RenderingServer.singleton().callOnRenderThread(new Callable(this, "_initializeCompute"));
    }

    @Override
    public void onNotification(int what) {
        if (what == 1 && rd != null && shader != 0) {
            rd.freeRid(shader);
            shader = 0;
            pipeline = 0;
        }
    }

    @GodotMethod
    public void _initializeCompute() {
        rd = RenderingServer.singleton().getRenderingDevice();
        if (rd == null) return;

        Resource resource = ResourceLoader.singleton().load("res://post_process_grayscale.glsl");
        if (!(resource instanceof RDShaderFile shaderFile)) return;

        RDShaderSPIRV shaderSpirv = shaderFile.getSpirv();
        shader = rd.shaderCreateFromSpirv(shaderSpirv);
        if (shader == 0) return;

        pipeline = rd.computePipelineCreate(shader);
    }

    @GodotMethod
    public void _renderCallback(int effectCallbackType, Object renderData) {
        if (rd == null || effectCallbackType != 4 || pipeline == 0) return;
        if (!(renderData instanceof RenderData renderDataObj)) return;

        RenderSceneBuffers renderSceneBuffersBase = renderDataObj.getRenderSceneBuffers();
        if (!(renderSceneBuffersBase instanceof RenderSceneBuffersRD renderSceneBuffers)) return;

        Vector2i size = renderSceneBuffers.getInternalSize();
        int sizeX = size.x;
        int sizeY = size.y;
        if (sizeX == 0 && sizeY == 0) return;

        int xGroups = (sizeX - 1) / 8 + 1;
        int yGroups = (sizeY - 1) / 8 + 1;
        int zGroups = 1;

        float[] pushConstant = new float[] {
            (float) sizeX,
            (float) sizeY,
            0.0f,
            0.0f
        };

        long viewCount = renderSceneBuffers.getViewCount();
        for (int view = 0; view < viewCount; view++) {
            long inputImage = renderSceneBuffers.getColorLayer(view);

            RDUniform uniform = RDUniform.create();
            uniform.setUniformType(9);
            uniform.setBinding(0);
            uniform.addId(inputImage);

            GodotArray<RDUniform> uniformArray = new GodotArray<>();
            uniformArray.add(uniform);
            long uniformSet = UniformSetCacheRD.getCache(shader, 0, uniformArray);
            byte[] pushConstantBytes = floatArrayToBytes(pushConstant);

            long computeList = rd.computeListBegin();
            rd.computeListBindComputePipeline(computeList, pipeline);
            rd.computeListBindUniformSet(computeList, uniformSet, 0);
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
