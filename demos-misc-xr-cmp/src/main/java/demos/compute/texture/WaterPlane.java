package demos.compute.texture;

import java.util.concurrent.ThreadLocalRandom;
import org.godot.annotation.GodotClass;
import org.godot.annotation.GodotMethod;
import org.godot.collection.GodotDictionary;
import org.godot.core.Callable;
import org.godot.math.Color;
import org.godot.math.Transform3D;
import org.godot.math.Vector2;
import org.godot.math.Vector2i;
import org.godot.math.Vector3;
import org.godot.math.Vector4;
import org.godot.node.Area3D;
import org.godot.node.Camera3D;
import org.godot.node.InputEventMouseButton;
import org.godot.node.InputEventMouseMotion;
import org.godot.node.MeshInstance3D;
import org.godot.node.PhysicsDirectSpaceState3D;
import org.godot.node.PhysicsRayQueryParameters3D;
import org.godot.node.RDShaderFile;
import org.godot.node.RDShaderSPIRV;
import org.godot.node.RDTextureFormat;
import org.godot.node.RDTextureView;
import org.godot.node.RDUniform;
import org.godot.node.RenderingDevice;
import org.godot.node.Resource;
import org.godot.node.ShaderMaterial;
import org.godot.node.Texture2DRD;
import org.godot.node.Viewport;
import org.godot.node.World3D;
import org.godot.singleton.RenderingServer;
import org.godot.singleton.ResourceLoader;

@GodotClass(name = "WaterPlane", parent = "Area3D")
public class WaterPlane extends Area3D {

    private double rainSize = 3.0;
    private double mouseSize = 5.0;
    private Vector2i textureSize = new Vector2i(512, 512);
    private double damp = 1.0;

    private double t = 0.0;
    private final double maxT = 0.1;

    private Texture2DRD texture;
    private int nextTexture = 0;

    private Vector4 addWavePoint = new Vector4(0, 0, 0, 0);
    private Vector2 mousePos = new Vector2(0, 0);
    private boolean mousePressed = false;

    private RenderingDevice rd;
    private long shader;
    private long pipeline;
    private final long[] textureRds = new long[3];
    private final long[] textureSets = new long[9];

    private int renderNextTexture;
    private Vector4 renderWavePoint = new Vector4(0, 0, 0, 0);
    private Vector2i renderTextureSize = new Vector2i(512, 512);
    private double renderDamp;

    private boolean initialized = false;

    @Override
    public void _ready() {
        if (initialized) return;
        initialized = true;

        Object rsObj = getProperty("rain_size");
        if (rsObj instanceof Number) rainSize = ((Number) rsObj).doubleValue();
        Object msObj = getProperty("mouse_size");
        if (msObj instanceof Number) mouseSize = ((Number) msObj).doubleValue();
        Object tsObj = getProperty("texture_size");
        if (tsObj instanceof Vector2i) textureSize = (Vector2i) tsObj;
        Object dObj = getProperty("damp");
        if (dObj instanceof Number) damp = ((Number) dObj).doubleValue();

        RenderingServer.singleton().callOnRenderThread(new Callable(this, "_initializeComputeCode"));

        MeshInstance3D meshInstance = getNodeAs("MeshInstance3D", MeshInstance3D.class);
        if (meshInstance != null && meshInstance.getMaterialOverride() instanceof ShaderMaterial material) {
            material.setShaderParameter("effect_texture_size", textureSize);
            if (material.getShaderParameter("effect_texture") instanceof Texture2DRD texture2DRD) {
                texture = texture2DRD;
            }
        }
    }

    public double getRainSize() {
        return rainSize;
    }

    public void setRainSize(double rainSize) {
        this.rainSize = rainSize;
    }

    public double getMouseSize() {
        return mouseSize;
    }

    public void setMouseSize(double mouseSize) {
        this.mouseSize = mouseSize;
    }

    @Override
    public void _exitTree() {
        if (texture != null) {
            texture.setTextureRdRid(0);
        }

        RenderingServer.singleton().callOnRenderThread(new Callable(this, "_freeComputeResources"));
    }

    @Override
    public boolean _input(Object inputEvent) {
        if (inputEvent instanceof InputEventMouseMotion event) {
            mousePos = event.getGlobalPosition();
        }

        if (inputEvent instanceof InputEventMouseButton event) {
            mousePos = event.getGlobalPosition();
            if (event.getButtonIndex() == 1) {
                mousePressed = event.isPressed();
            }
        }

        return false;
    }

    private void checkMousePos() {
        Viewport viewport = getViewport();
        if (viewport == null) return;

        Camera3D camera = viewport.getCamera3d();
        if (camera == null) return;

        Vector3 rayOrigin = camera.projectRayOrigin(mousePos);
        Vector3 rayNormal = camera.projectRayNormal(mousePos);
        Vector3 to = rayOrigin.add(rayNormal.mul(100.0));

        PhysicsRayQueryParameters3D parameters = PhysicsRayQueryParameters3D.create(rayOrigin, to, 1);
        parameters.setCollideWithBodies(false);
        parameters.setCollideWithAreas(true);

        World3D world3d = getWorld3d();
        if (world3d == null) return;

        PhysicsDirectSpaceState3D directSpaceState = world3d.getDirectSpaceState();
        if (directSpaceState == null) return;

        GodotDictionary result = directSpaceState.intersectRay(parameters);
        if (result != null && !result.isEmpty()) {
            Object position = result.get("position");
            Transform3D inverse = getGlobalTransform().inverse();
            if (position instanceof Vector3 position3d) {
                Vector3 pos3d = inverse.apply(position3d);
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
        checkMousePos();

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

        nextTexture = (nextTexture + 1) % 3;

        if (texture != null && textureRds[nextTexture] != 0) {
            texture.setTextureRdRid(textureRds[nextTexture]);
        }

        renderNextTexture = nextTexture;
        renderWavePoint = new Vector4(addWavePoint.x, addWavePoint.y, addWavePoint.z, addWavePoint.w);
        renderTextureSize = textureSize;
        renderDamp = damp;
        RenderingServer.singleton().callOnRenderThread(new Callable(this, "_renderProcess"));
    }

    @GodotMethod
    public void _initializeComputeCode() {
        rd = RenderingServer.singleton().getRenderingDevice();
        if (rd == null) return;

        Resource resource = ResourceLoader.singleton().load("res://water_plane/water_compute.glsl");
        if (!(resource instanceof RDShaderFile shaderFile)) return;

        RDShaderSPIRV shaderSpirv = shaderFile.getSpirv();
        shader = rd.shaderCreateFromSpirv(shaderSpirv);
        if (shader == 0) return;

        pipeline = rd.computePipelineCreate(shader);
        if (pipeline == 0) return;

        RDTextureFormat tf = RDTextureFormat.create();
        tf.setFormat(96);
        tf.setTextureType(0);
        tf.setWidth(textureSize.x);
        tf.setHeight(textureSize.y);
        tf.setDepth(1);
        tf.setArrayLayers(1);
        tf.setMipmaps(1);
        tf.setUsageBits(1 | 8 | 32);

        RDTextureView textureView = RDTextureView.create();
        for (int i = 0; i < 3; i++) {
            textureRds[i] = rd.textureCreate(tf, textureView, new Object[0]);
            rd.textureClear(textureRds[i], new Color(0, 0, 0, 0), 0, 1, 0, 1);
        }

        for (int i = 0; i < 3; i++) {
            long nextTextureRd = textureRds[i];
            long currentTextureRd = textureRds[(i + 2) % 3];
            long previousTextureRd = textureRds[(i + 1) % 3];

            textureSets[i * 3] = createUniformSet(currentTextureRd, 0);
            textureSets[i * 3 + 1] = createUniformSet(previousTextureRd, 1);
            textureSets[i * 3 + 2] = createUniformSet(nextTextureRd, 2);
        }
    }

    private long createUniformSet(long textureRd, int uniformSet) {
        RDUniform uniform = RDUniform.create();
        uniform.setUniformType(9);
        uniform.setBinding(0);
        uniform.addId(textureRd);
        return rd.uniformSetCreate(new RDUniform[] { uniform }, shader, uniformSet);
    }

    @GodotMethod
    public void _renderProcess() {
        Vector4 wavePoint = renderWavePoint;
        Vector2i texSize = renderTextureSize;

        float[] pushConstant = new float[] {
            (float) wavePoint.x,
            (float) wavePoint.y,
            (float) wavePoint.z,
            (float) wavePoint.w,
            (float) texSize.x,
            (float) texSize.y,
            (float) renderDamp,
            0.0f
        };

        int xGroups = (texSize.x - 1) / 8 + 1;
        int yGroups = (texSize.y - 1) / 8 + 1;

        long currentSet = textureSets[renderNextTexture * 3];
        long previousSet = textureSets[renderNextTexture * 3 + 1];
        long nextSet = textureSets[renderNextTexture * 3 + 2];

        boolean pipelineValid = pipeline != 0 && rd.computePipelineIsValid(pipeline);
        boolean currentValid = currentSet != 0 && rd.uniformSetIsValid(currentSet);
        boolean previousValid = previousSet != 0 && rd.uniformSetIsValid(previousSet);
        boolean nextValid = nextSet != 0 && rd.uniformSetIsValid(nextSet);
        if (!(pipelineValid && currentValid && previousValid && nextValid)) return;

        byte[] pushConstantBytes = floatArrayToBytes(pushConstant);
        long computeList = rd.computeListBegin();
        rd.computeListBindComputePipeline(computeList, pipeline);
        rd.computeListBindUniformSet(computeList, currentSet, 0);
        rd.computeListBindUniformSet(computeList, previousSet, 1);
        rd.computeListBindUniformSet(computeList, nextSet, 2);
        rd.computeListSetPushConstant(computeList, pushConstantBytes, pushConstantBytes.length);
        rd.computeListDispatch(computeList, xGroups, yGroups, 1);
        rd.computeListEnd();
    }

    @GodotMethod
    public void _freeComputeResources() {
        if (rd == null) return;

        for (int i = 0; i < 3; i++) {
            if (textureRds[i] != 0) {
                rd.freeRid(textureRds[i]);
                textureRds[i] = 0;
            }
        }

        for (int i = 0; i < 9; i++) {
            if (textureSets[i] != 0) {
                rd.freeRid(textureSets[i]);
                textureSets[i] = 0;
            }
        }

        if (shader != 0) {
            rd.freeRid(shader);
            shader = 0;
            pipeline = 0;
        }
    }

    private double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }

    private int randiRange(int from, int to) {
        return ThreadLocalRandom.current().nextInt(from, to + 1);
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
