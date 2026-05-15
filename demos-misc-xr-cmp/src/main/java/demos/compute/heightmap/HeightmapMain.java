package demos.compute.heightmap;

import org.godot.annotation.GodotClass;
import org.godot.annotation.GodotMethod;
import org.godot.math.Color;
import org.godot.math.Vector2i;
import org.godot.node.Button;
import org.godot.node.Control;
import org.godot.node.FastNoiseLite;
import org.godot.node.Gradient;
import org.godot.node.GradientTexture1D;
import org.godot.node.Image;
import org.godot.node.ImageTexture;
import org.godot.node.Label;
import org.godot.node.RDShaderFile;
import org.godot.node.RDShaderSPIRV;
import org.godot.node.RDTextureFormat;
import org.godot.node.RDTextureView;
import org.godot.node.RDUniform;
import org.godot.collection.GodotArray;
import org.godot.node.RandomNumberGenerator;
import org.godot.node.RenderingDevice;
import org.godot.node.Resource;
import org.godot.node.SpinBox;
import org.godot.node.TextureRect;
import org.godot.singleton.OS;
import org.godot.singleton.RenderingServer;
import org.godot.singleton.ResourceLoader;
import org.godot.singleton.Time;

@GodotClass(name = "HeightmapMain", parent = "Control")
public class HeightmapMain extends Control {

    private String shaderFile = "";
    private int dimension = 512;

    private SpinBox seedInput;
    private TextureRect heightmapRect;
    private TextureRect islandRect;

    private FastNoiseLite noise;
    private Gradient gradient;
    private GradientTexture1D gradientTex;
    private RandomNumberGenerator randomNumberGenerator;

    private RenderingDevice rd;
    private long shaderRid;
    private long heightmapRid;
    private long gradientRid;
    private long uniformSet;
    private long pipeline;

    private int po2Dimensions;
    private long startTime;

    private boolean initialized = false;

    @Override
    public void _ready() {
        if (initialized) return;
        initialized = true;

        shaderFile = (String) getProperty("shader_file");
        if (shaderFile == null) shaderFile = "";
        Object dimObj = getProperty("dimension");
        if (dimObj instanceof Number number) {
            dimension = number.intValue();
        }

        seedInput = getNodeAs("CenterContainer/VBoxContainer/PanelContainer/VBoxContainer/GridContainer/SeedInput", SpinBox.class);
        heightmapRect = getNodeAs("CenterContainer/VBoxContainer/PanelContainer2/VBoxContainer/GridContainer/RawHeightmap", TextureRect.class);
        islandRect = getNodeAs("CenterContainer/VBoxContainer/PanelContainer2/VBoxContainer/GridContainer/ComputedHeightmap", TextureRect.class);

        noise = FastNoiseLite.create();
        noise.setNoiseType(2);
        noise.setFractalOctaves(5);
        noise.setFractalLacunarity(1.9);

        gradient = Gradient.create();
        gradient.addPoint(0.6, new Color(0.9, 0.9, 0.9, 1.0));
        gradient.addPoint(0.8, new Color(1.0, 1.0, 1.0, 1.0));
        gradient.reverse();

        gradientTex = GradientTexture1D.create();
        gradientTex.setGradient(gradient);

        randomNumberGenerator = RandomNumberGenerator.create();
        randomNumberGenerator.randomize();
        randomizeSeed();

        po2Dimensions = nearestPo2(dimension);

        double frequency = 0.003 / ((double) po2Dimensions / 512.0);
        noise.setFrequency(frequency);

        String gpuName = RenderingServer.singleton().getVideoAdapterName();
        String cpuName = OS.singleton().getProcessorName();
        Button gpuButton = getNodeAs("CenterContainer/VBoxContainer/PanelContainer/VBoxContainer/HBoxContainer/CreateButtonGPU", Button.class);
        Button cpuButton = getNodeAs("CenterContainer/VBoxContainer/PanelContainer/VBoxContainer/HBoxContainer/CreateButtonCPU", Button.class);
        if (gpuButton != null) {
            gpuButton.setText(gpuButton.getText() + "\n" + gpuName);
        }
        if (cpuButton != null) {
            cpuButton.setText(cpuButton.getText() + "\n" + cpuName);
        }
    }

    @Override
    public void onNotification(int what) {
        if (what == 1) {
            cleanupGpu();
        }
    }

    private void randomizeSeed() {
        if (seedInput != null && randomNumberGenerator != null) {
            seedInput.setValue(randomNumberGenerator.randi());
        }
    }

    private Image prepareImage() {
        startTime = Time.singleton().getTicksUsec().longValue();

        if (seedInput != null) {
            noise.setSeed((long) seedInput.getValue());
        }

        Image heightmap = noise.getImage(po2Dimensions, po2Dimensions, false, false);

        Image clone = Image.create();
        clone.copyFrom(heightmap);
        clone.resize(512, 512, Image.Interpolation.INTERPOLATE_NEAREST);
        ImageTexture cloneTex = ImageTexture.createFromImage(clone);
        if (heightmapRect != null) {
            heightmapRect.setTexture(cloneTex);
        }

        return heightmap;
    }

    private void initGpu() {
        rd = RenderingServer.singleton().createLocalRenderingDevice();

        if (rd == null) {
            OS.singleton().alert(
                    "Couldn't create local RenderingDevice.\n\n"
                            + "Note: RenderingDevice is only available in the Forward+ and Mobile rendering methods, not Compatibility.");
            return;
        }

        shaderRid = loadShader(rd, shaderFile);

        RDTextureFormat heightmapFormat = RDTextureFormat.create();
        heightmapFormat.setFormat(41);
        heightmapFormat.setWidth(po2Dimensions);
        heightmapFormat.setHeight(po2Dimensions);
        heightmapFormat.setUsageBits(8 + 64 + 128);

        RDTextureView heightmapView = RDTextureView.create();
        heightmapRid = rd.textureCreate(heightmapFormat, heightmapView);

        RDUniform heightmapUniform = RDUniform.create();
        heightmapUniform.setUniformType(9);
        heightmapUniform.setBinding(0);
        heightmapUniform.addId(heightmapRid);

        RDTextureFormat gradientFormat = RDTextureFormat.create();
        gradientFormat.setFormat(43);
        gradientFormat.setWidth(gradientTex.getWidth());
        gradientFormat.setHeight(1);
        gradientFormat.setUsageBits(8 + 64);

        Image gradientImage = gradientTex.getImage();
        byte[] gradientData = gradientImage.getImageData();

        RDTextureView gradientView = RDTextureView.create();
        GodotArray<Object> gradientDataArray = new GodotArray<>();
        gradientDataArray.add(gradientData);
        gradientRid = rd.textureCreate(gradientFormat, gradientView, gradientDataArray);

        RDUniform gradientUniform = RDUniform.create();
        gradientUniform.setUniformType(9);
        gradientUniform.setBinding(1);
        gradientUniform.addId(gradientRid);

        GodotArray<RDUniform> uniformArray = new GodotArray<>();
        uniformArray.add(heightmapUniform);
        uniformArray.add(gradientUniform);
        uniformSet = rd.uniformSetCreate(uniformArray, shaderRid, 0);
        pipeline = rd.computePipelineCreate(shaderRid);
    }

    private void computeIslandGpu(Image heightmap) {
        if (rd == null) {
            initGpu();
        }

        if (rd == null) {
            Label label = getNodeAs("CenterContainer/VBoxContainer/PanelContainer2/VBoxContainer/HBoxContainer2/Label2", Label.class);
            if (label != null) {
                label.setText("RenderingDevice is not available on the current rendering driver");
            }
            return;
        }

        rd.textureUpdate(heightmapRid, 0, heightmap.getImageData());

        long computeList = rd.computeListBegin();
        rd.computeListBindComputePipeline(computeList, pipeline);
        rd.computeListBindUniformSet(computeList, uniformSet, 0);
        rd.computeListDispatch(computeList, po2Dimensions / 8, po2Dimensions / 8, 1);
        rd.computeListEnd();

        rd.submit();
        rd.sync();

        byte[] outputBytes = rd.textureGetData(heightmapRid, 0);
        Image islandImg = Image.createFromData(po2Dimensions, po2Dimensions, false, Image.Format.FORMAT_RGBA8, outputBytes);

        displayIsland(islandImg);
    }

    private void cleanupGpu() {
        if (rd == null) return;

        if (pipeline != 0) {
            rd.freeRid(pipeline);
            pipeline = 0;
        }
        if (uniformSet != 0) {
            rd.freeRid(uniformSet);
            uniformSet = 0;
        }
        if (gradientRid != 0) {
            rd.freeRid(gradientRid);
            gradientRid = 0;
        }
        if (heightmapRid != 0) {
            rd.freeRid(heightmapRid);
            heightmapRid = 0;
        }
        if (shaderRid != 0) {
            rd.freeRid(shaderRid);
            shaderRid = 0;
        }

        rd.free();
        rd = null;
    }

    private long loadShader(RenderingDevice renderingDevice, String path) {
        Resource shaderFileData = ResourceLoader.singleton().load(path, "", ResourceLoader.CacheMode.CACHE_MODE_REUSE);
        if (!(shaderFileData instanceof RDShaderFile shaderFileResource)) return 0;
        RDShaderSPIRV shaderSpirv = shaderFileResource.getSpirv();
        return renderingDevice.shaderCreateFromSpirv(shaderSpirv);
    }

    private void computeIslandCpu(Image heightmap) {
        Vector2i center = new Vector2i(po2Dimensions / 2, po2Dimensions / 2);

        for (int y = 0; y < po2Dimensions; y++) {
            for (int x = 0; x < po2Dimensions; x++) {
                Vector2i coord = new Vector2i(x, y);
                Color pixel = heightmap.getPixelv(coord);

                double cx = center.x;
                double cy = center.y;
                double dist = Math.sqrt((x - cx) * (x - cx) + (y - cy) * (y - cy));
                double t = dist / cx;

                Color gradientColor = gradient.sample(t);

                double pixelV = colorValue(pixel) * colorValue(gradientColor);
                if (pixelV < 0.2) {
                    pixelV = 0.0;
                }

                heightmap.setPixelv(coord, withValue(pixel, pixelV));
            }
        }

        displayIsland(heightmap);
    }

    private void displayIsland(Image island) {
        ImageTexture islandTex = ImageTexture.createFromImage(island);
        if (islandRect != null) {
            islandRect.setTexture(islandTex);
        }

        long stopTime = Time.singleton().getTicksUsec().longValue();
        long elapsed = stopTime - startTime;
        String elapsedStr = String.format("%.1f", elapsed * 0.001);
        Label label = getNodeAs("CenterContainer/VBoxContainer/PanelContainer2/VBoxContainer/HBoxContainer/Label2", Label.class);
        if (label != null) {
            label.setText(elapsedStr + " ms");
        }
    }

    @GodotMethod
    public void OnRandomButtonPressed() {
        randomizeSeed();
    }

    @GodotMethod
    public void OnCreateButtonGpuPressed() {
        Image heightmap = prepareImage();
        callDeferred("_onCreateButtonGpuDeferred", heightmap);
    }

    @GodotMethod
    public void _onCreateButtonGpuDeferred(Image heightmap) {
        computeIslandGpu(heightmap);
    }

    @GodotMethod
    public void OnCreateButtonCpuPressed() {
        Image heightmap = prepareImage();
        callDeferred("_onCreateButtonCpuDeferred", heightmap);
    }

    @GodotMethod
    public void _onCreateButtonCpuDeferred(Image heightmap) {
        computeIslandCpu(heightmap);
    }

    private int nearestPo2(int value) {
        int po2 = 1;
        while (po2 < value) {
            po2 *= 2;
        }
        return po2;
    }

    private double colorValue(Color color) {
        return Math.max(color.r, Math.max(color.g, color.b));
    }

    private Color withValue(Color color, double value) {
        double currentValue = colorValue(color);
        if (currentValue <= 0.0) {
            return new Color(value, value, value, color.a);
        }
        double scale = value / currentValue;
        return new Color(clampColor(color.r * scale), clampColor(color.g * scale), clampColor(color.b * scale), color.a);
    }

    private double clampColor(double value) {
        return Math.max(0.0, Math.min(1.0, value));
    }
}
