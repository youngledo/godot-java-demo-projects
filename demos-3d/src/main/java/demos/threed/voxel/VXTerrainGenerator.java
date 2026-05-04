package demos.threed.voxel;

import org.godot.annotation.GodotClass;
import org.godot.node.Resource;
import org.godot.math.Vector3;

import java.util.HashMap;
import java.util.Map;

/**
 * Static terrain generation methods.
 * Port of world/terrain_generator.gd.
 */
@GodotClass(name = "VXTerrainGenerator", parent = "Resource")
public class VXTerrainGenerator extends Resource {

    public static final int CHUNK_SIZE = 16; // Keep in sync with VXChunk.
    private static final double RANDOM_BLOCK_PROBABILITY = 0.015;

    /**
     * Returns an empty block map.
     */
    public static Map<String, Integer> empty() {
        return new HashMap<>();
    }

    /**
     * Generates random blocks scattered throughout a chunk.
     */
    public static Map<String, Integer> randomBlocks() {
        Map<String, Integer> data = new HashMap<>();
        for (int x = 0; x < CHUNK_SIZE; x++) {
            for (int y = 0; y < CHUNK_SIZE; y++) {
                for (int z = 0; z < CHUNK_SIZE; z++) {
                    if (Math.random() < RANDOM_BLOCK_PROBABILITY) {
                        String key = blockKey(x, y, z);
                        data.put(key, (int) (Math.random() * 29) + 1);
                    }
                }
            }
        }
        return data;
    }

    /**
     * Generates a flat grass/dirt/bedrock terrain layer for chunks at y == -1.
     */
    public static Map<String, Integer> flat(int chunkPosX, int chunkPosY, int chunkPosZ) {
        Map<String, Integer> data = new HashMap<>();
        if (chunkPosY != -1) {
            return data;
        }
        for (int x = 0; x < CHUNK_SIZE; x++) {
            for (int z = 0; z < CHUNK_SIZE; z++) {
                data.put(blockKey(x, 2, z), 3);   // Grass
                data.put(blockKey(x, 1, z), 2);   // Dirt
                data.put(blockKey(x, 0, z), 2);   // Dirt
                data.put(blockKey(x, -1, z), 9);  // Bedrock
            }
        }
        return data;
    }

    /**
     * Returns a block key string from integer coordinates.
     */
    public static String blockKey(int x, int y, int z) {
        return x + "," + y + "," + z;
    }

    /**
     * Returns a block key string from double coordinates (rounded to int).
     */
    public static String blockKey(double x, double y, double z) {
        return (int) Math.floor(x) + "," + (int) Math.floor(y) + "," + (int) Math.floor(z);
    }
}
