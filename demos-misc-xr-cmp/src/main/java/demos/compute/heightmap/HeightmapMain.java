package demos.compute.heightmap;

import org.godot.Godot;
import org.godot.annotation.GodotClass;
import org.godot.annotation.GodotMethod;
import org.godot.node.Control;
import org.godot.math.Vector2i;
import org.godot.math.Color;

/**
 * Port of compute/heightmap/main.gd
 *
 * Demonstrates GPU compute shaders for heightmap generation.
 * Uses RenderingDevice API to run a compute shader that generates
 * an island from noise data.
 */
@GodotClass(name = "HeightmapMain", parent = "Control")
public class HeightmapMain extends Control {

    // Export properties
    private String shaderFile = "";
    private int dimension = 512;

    // Node references
    private Godot seedInput;
    private Godot heightmapRect;
    private Godot islandRect;

    // Noise and gradient objects
    private Godot noise;
    private Godot gradient;
    private Godot gradientTex;

    // GPU resources
    private Godot rd; // RenderingDevice
    private Godot shaderRid; // RID
    private Godot heightmapRid; // RID
    private Godot gradientRid; // RID
    private Godot uniformSet; // RID
    private Godot pipeline; // RID

    private int po2Dimensions;
    private long startTime;

    private boolean initialized = false;

    @Override
    public void _ready() {
        if (initialized) return;
        initialized = true;

        // Read export properties
        shaderFile = (String) getProperty("shader_file");
        if (shaderFile == null) shaderFile = "";
        Object dimObj = getProperty("dimension");
        if (dimObj instanceof Number) {
            dimension = ((Number) dimObj).intValue();
        }

        // Get node references
        seedInput = (Godot) call("get_node", "CenterContainer/VBoxContainer/PanelContainer/VBoxContainer/GridContainer/SeedInput");
        heightmapRect = (Godot) call("get_node", "CenterContainer/VBoxContainer/PanelContainer2/VBoxContainer/GridContainer/RawHeightmap");
        islandRect = (Godot) call("get_node", "CenterContainer/VBoxContainer/PanelContainer2/VBoxContainer/GridContainer/ComputedHeightmap");

        // Create noise
        noise = (Godot) org.godot.singleton.ClassDB.singleton().instantiate("FastNoiseLite");
        noise.call("set", "noise_type", 2); // TYPE_SIMPLEX_SMOOTH
        noise.call("set", "fractal_octaves", 5);
        noise.call("set", "fractal_lacunarity", 1.9);

        // Create gradient
        gradient = (Godot) org.godot.singleton.ClassDB.singleton().instantiate("Gradient");
        gradient.call("add_point", 0.6, new Color(0.9, 0.9, 0.9, 1.0));
        gradient.call("add_point", 0.8, new Color(1.0, 1.0, 1.0, 1.0));
        gradient.call("reverse");

        // Create gradient texture
        gradientTex = (Godot) org.godot.singleton.ClassDB.singleton().instantiate("GradientTexture1D");
        gradientTex.call("set", "gradient", gradient);

        randomizeSeed();

        // Find nearest power of 2
        po2Dimensions = nearestPo2(dimension);

        double frequency = 0.003 / ((double) po2Dimensions / 512.0);
        noise.call("set", "frequency", frequency);

        // Append GPU and CPU model names
        String gpuName = (String) call("RenderingServer.get_video_adapter_name");
        String cpuName = (String) call("OS.get_processor_name");
        Godot gpuButton = (Godot) call("get_node", "CenterContainer/VBoxContainer/PanelContainer/VBoxContainer/HBoxContainer/CreateButtonGPU");
        Godot cpuButton = (Godot) call("get_node", "CenterContainer/VBoxContainer/PanelContainer/VBoxContainer/HBoxContainer/CreateButtonCPU");
        if (gpuButton != null) {
            String gpuText = (String) gpuButton.call("get", "text");
            gpuButton.call("set", "text", gpuText + "\n" + gpuName);
        }
        if (cpuButton != null) {
            String cpuText = (String) cpuButton.call("get", "text");
            cpuButton.call("set", "text", cpuText + "\n" + cpuName);
        }
    }

    @Override
    public void onNotification(int what) {
        // NOTIFICATION_PREDELETE = 1
        if (what == 1) {
            cleanupGpu();
        }
    }

    private void randomizeSeed() {
        if (seedInput != null) {
            Object randVal = call("randi");
            seedInput.call("set", "value", randVal);
        }
    }

    private Godot prepareImage() {
        startTime = (Long) call("Time.get_ticks_usec");

        Object seedVal = seedInput.call("get", "value");
        noise.call("set", "seed", seedVal);

        // Create image from noise
        Godot heightmap = (Godot) noise.call("get_image", po2Dimensions, po2Dimensions, false, false);

        // Create a clone for display
        Godot clone = (Godot) org.godot.singleton.ClassDB.singleton().instantiate("Image");
        clone.call("copy_from", heightmap);
        clone.call("resize", 512, 512, 0); // INTERPOLATE_NEAREST = 0
        Godot cloneTex = (Godot) call("ImageTexture.create_from_image", clone);
        heightmapRect.call("set", "texture", cloneTex);

        return heightmap;
    }

    private void initGpu() {
        // Create a local rendering device
        rd = (Godot) call("RenderingServer.create_local_rendering_device");

        if (rd == null) {
            call("OS.alert",
                "Couldn't create local RenderingDevice.\n\n" +
                "Note: RenderingDevice is only available in the Forward+ and Mobile rendering methods, not Compatibility.");
            return;
        }

        // Load the shader
        shaderRid = loadShader(rd, shaderFile);

        // Create format for heightmap
        Godot heightmapFormat = (Godot) org.godot.singleton.ClassDB.singleton().instantiate("RDTextureFormat");
        heightmapFormat.call("set", "format", 41); // DATA_FORMAT_R8_UNORM
        heightmapFormat.call("set", "width", po2Dimensions);
        heightmapFormat.call("set", "height", po2Dimensions);
        // TEXTURE_USAGE_STORAGE_BIT + TEXTURE_USAGE_CAN_UPDATE_BIT + TEXTURE_USAGE_CAN_COPY_FROM_BIT
        heightmapFormat.call("set", "usage_bits", 8 + 64 + 128);

        // Create heightmap texture
        Godot heightmapView = (Godot) org.godot.singleton.ClassDB.singleton().instantiate("RDTextureView");
        heightmapRid = (Godot) rd.call("texture_create", heightmapFormat, heightmapView);

        // Create uniform for heightmap
        Godot heightmapUniform = (Godot) org.godot.singleton.ClassDB.singleton().instantiate("RDUniform");
        heightmapUniform.call("set", "uniform_type", 9); // UNIFORM_TYPE_IMAGE
        heightmapUniform.call("set", "binding", 0);
        heightmapUniform.call("add_id", heightmapRid);

        // Create format for gradient
        Godot gradientFormat = (Godot) org.godot.singleton.ClassDB.singleton().instantiate("RDTextureFormat");
        gradientFormat.call("set", "format", 43); // DATA_FORMAT_R8G8B8A8_UNORM
        Object gradientWidth = gradientTex.call("get", "width");
        gradientFormat.call("set", "width", gradientWidth);
        gradientFormat.call("set", "height", 1);
        // TEXTURE_USAGE_STORAGE_BIT + TEXTURE_USAGE_CAN_UPDATE_BIT
        gradientFormat.call("set", "usage_bits", 8 + 64);

        // Get gradient image data
        Godot gradientImage = (Godot) gradientTex.call("get_image");
        Object gradientData = gradientImage.call("get_data");

        // Storage gradient as texture
        Godot gradientView = (Godot) org.godot.singleton.ClassDB.singleton().instantiate("RDTextureView");
        gradientRid = (Godot) rd.call("texture_create", gradientFormat, gradientView, new Object[]{gradientData});

        // Create uniform for gradient
        Godot gradientUniform = (Godot) org.godot.singleton.ClassDB.singleton().instantiate("RDUniform");
        gradientUniform.call("set", "uniform_type", 9); // UNIFORM_TYPE_IMAGE
        gradientUniform.call("set", "binding", 1);
        gradientUniform.call("add_id", gradientRid);

        uniformSet = (Godot) rd.call("uniform_set_create", new Object[]{heightmapUniform, gradientUniform}, shaderRid, 0);
        pipeline = (Godot) rd.call("compute_pipeline_create", shaderRid);
    }

    private void computeIslandGpu(Godot heightmap) {
        if (rd == null) {
            initGpu();
        }

        if (rd == null) {
            Godot label = (Godot) call("get_node", "CenterContainer/VBoxContainer/PanelContainer2/VBoxContainer/HBoxContainer2/Label2");
            if (label != null) {
                label.call("set", "text", "RenderingDevice is not available on the current rendering driver");
            }
            return;
        }

        // Store heightmap as texture
        Object heightmapData = heightmap.call("get_data");
        rd.call("texture_update", heightmapRid, 0, heightmapData);

        // Begin compute list
        Object computeList = rd.call("compute_list_begin");
        rd.call("compute_list_bind_compute_pipeline", computeList, pipeline);
        rd.call("compute_list_bind_uniform_set", computeList, uniformSet, 0);
        // Dispatch with work group size 8x8x1
        rd.call("compute_list_dispatch", computeList, po2Dimensions / 8, po2Dimensions / 8, 1);
        rd.call("compute_list_end");

        // Submit and sync
        rd.call("submit");
        rd.call("sync");

        // Retrieve processed data
        Object outputBytes = rd.call("texture_get_data", heightmapRid, 0);
        // Create image from output bytes - FORMAT_L8 = 4
        Godot islandImg = (Godot) call("Image.create_from_data", po2Dimensions, po2Dimensions, false, 4, outputBytes);

        displayIsland(islandImg);
    }

    private void cleanupGpu() {
        if (rd == null) return;

        // Free all GPU resources
        rd.call("free_rid", pipeline);
        pipeline = null;

        rd.call("free_rid", uniformSet);
        uniformSet = null;

        rd.call("free_rid", gradientRid);
        gradientRid = null;

        rd.call("free_rid", heightmapRid);
        heightmapRid = null;

        rd.call("free_rid", shaderRid);
        shaderRid = null;

        rd.call("free");
        rd = null;
    }

    private Godot loadShader(Godot pRd, String path) {
        Object shaderFileData = org.godot.singleton.ResourceLoader.singleton().load(path, "", 1);
        Object shaderSpirv = ((Godot) shaderFileData).call("get_spirv");
        return (Godot) pRd.call("shader_create_from_spirv", shaderSpirv);
    }

    private void computeIslandCpu(Godot heightmap) {
        Vector2i center = new Vector2i(po2Dimensions / 2, po2Dimensions / 2);

        for (int y = 0; y < po2Dimensions; y++) {
            for (int x = 0; x < po2Dimensions; x++) {
                Vector2i coordObj = new Vector2i(x, y);
                Object pixel = heightmap.call("get_pixelv", coordObj);

                // Calculate distance
                double cx = center.x;
                double cy = center.y;
                double dist = Math.sqrt((x - cx) * (x - cx) + (y - cy) * (y - cy));
                double t = dist / cx;

                // Sample gradient
                Object gradientColor = gradient.call("sample", t);

                // Get the 'v' (value/hsv) component
                double pixelV = ((Number) ((Godot) pixel).call("get", "v")).doubleValue();
                double gradientV = ((Number) ((Godot) gradientColor).call("get", "v")).doubleValue();

                pixelV *= gradientV;
                if (pixelV < 0.2) {
                    pixelV = 0.0;
                }

                ((Godot) pixel).call("set", "v", pixelV);
                heightmap.call("set_pixelv", coordObj, pixel);
            }
        }

        displayIsland(heightmap);
    }

    private void displayIsland(Godot island) {
        Godot islandTex = (Godot) call("ImageTexture.create_from_image", island);
        islandRect.call("set", "texture", islandTex);

        // Calculate and display elapsed time
        long stopTime = (Long) call("Time.get_ticks_usec");
        long elapsed = stopTime - startTime;
        String elapsedStr = String.format("%.1f", elapsed * 0.001);
        Godot label = (Godot) call("get_node", "CenterContainer/VBoxContainer/PanelContainer2/VBoxContainer/HBoxContainer/Label2");
        if (label != null) {
            label.call("set", "text", elapsedStr + " ms");
        }
    }

    @GodotMethod
    public void _on_random_button_pressed() {
        randomizeSeed();
    }

    @GodotMethod
    public void _on_create_button_gpu_pressed() {
        Godot heightmap = prepareImage();
        // Use call_deferred for the compute operation
        call("call_deferred", "_onCreateButtonGpuDeferred", heightmap);
    }

    @GodotMethod
    public void _onCreateButtonGpuDeferred(Godot heightmap) {
        computeIslandGpu(heightmap);
    }

    @GodotMethod
    public void _on_create_button_cpu_pressed() {
        Godot heightmap = prepareImage();
        call("call_deferred", "_onCreateButtonCpuDeferred", heightmap);
    }

    @GodotMethod
    public void _onCreateButtonCpuDeferred(Godot heightmap) {
        computeIslandCpu(heightmap);
    }

    private int nearestPo2(int value) {
        int po2 = 1;
        while (po2 < value) {
            po2 *= 2;
        }
        return po2;
    }
}
