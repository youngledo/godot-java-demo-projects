package demos.threed.voxel;

import org.godot.annotation.GodotClass;
import org.godot.math.Vector3;
import org.godot.node.Node;
import org.godot.node.StaticBody3D;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Manages chunk creation, deletion, and block lookups.
 * Port of world/voxel_world.gd.
 */
@GodotClass(name = "VXVoxelWorld", parent = "Node")
public class VXVoxelWorld extends Node {

    private static final Vector3 CHUNK_MIDPOINT = new Vector3(0.5, 0.5, 0.5).mul(VXChunk.CHUNK_SIZE);
    private static final int CHUNK_END_SIZE = VXChunk.CHUNK_SIZE - 1;
    private static final int[][] DIRECTIONS = {
            {-1, 0, 0}, {1, 0, 0},
            {0, -1, 0}, {0, 1, 0},
            {0, 0, -1}, {0, 0, 1}
    };

    private int renderDistance = 7;
    private int deleteDistance = 0;
    public int effectiveRenderDistance = 0;
    private int oldPlayerChunkX, oldPlayerChunkY, oldPlayerChunkZ;

    private boolean generating = true;
    private boolean deleting = false;

    private final Map<String, VXChunk.ChunkData> chunks = new LinkedHashMap<>();

    private org.godot.node.Node player;
    private VXSettings settings;
    private boolean initialized = false;

    @Override
    public void _ready() {
        if (initialized) return;
        initialized = true;

        player = getNode("../Player");
        java.lang.Object settingsObj = getNode("/root/Settings");
        if (settingsObj instanceof VXSettings) {
            settings = (VXSettings) settingsObj;
        }
    }

    @Override
    public void _process(double delta) {
        if (settings != null) {
            renderDistance = settings.renderDistance;
        }
        deleteDistance = renderDistance + 2;

        if (player == null) return;

        Vector3 playerPos = (Vector3) player.call("get_position");
        int pcx = (int) Math.round(playerPos.getX() / VXChunk.CHUNK_SIZE);
        int pcy = (int) Math.round(playerPos.getY() / VXChunk.CHUNK_SIZE);
        int pcz = (int) Math.round(playerPos.getZ() / VXChunk.CHUNK_SIZE);

        if (deleting || pcx != oldPlayerChunkX || pcy != oldPlayerChunkY || pcz != oldPlayerChunkZ) {
            deleteFarAwayChunks(pcx, pcy, pcz);
            generating = true;
        }

        if (!generating) return;

        Vector3 velocity = (Vector3) player.getProperty("velocity");
        if (velocity != null) {
            double vy = velocity.getY();
            double clamped = Math.max(-renderDistance / 4.0, Math.min(renderDistance / 4.0, vy));
            pcy += (int) Math.round(clamped);
        }

        for (int x = pcx - effectiveRenderDistance; x < pcx + effectiveRenderDistance; x++) {
            for (int y = pcy - effectiveRenderDistance; y < pcy + effectiveRenderDistance; y++) {
                for (int z = pcz - effectiveRenderDistance; z < pcz + effectiveRenderDistance; z++) {
                    Vector3 chunkPos = new Vector3(x, y, z);
                    double dist = new Vector3(pcx, pcy, pcz).sub(chunkPos).length();
                    if (dist > renderDistance) continue;

                    String key = VXChunk.chunkKey(x, y, z);
                    if (chunks.containsKey(key)) continue;

                    int worldType = (settings != null) ? settings.worldType : 0;

                    StaticBody3D node = StaticBody3D.create();
                    VXChunk.ChunkData cd = VXChunk.initChunk(node, x, y, z, worldType);
                    chunks.put(key, cd);
                    addChild(node);

                    // Try initial mesh generation.
                    tryInitialGenerateMesh(cd);

                    // Check neighbors.
                    for (int[] dir : DIRECTIONS) {
                        String neighborKey = VXChunk.chunkKey(x + dir[0], y + dir[1], z + dir[2]);
                        VXChunk.ChunkData neighbor = chunks.get(neighborKey);
                        if (neighbor != null && !neighbor.isInitialMeshGenerated) {
                            tryInitialGenerateMesh(neighbor);
                        }
                    }
                    return;
                }
            }
        }

        if (effectiveRenderDistance < renderDistance) {
            effectiveRenderDistance += 1;
        } else {
            generating = false;
        }
    }

    /**
     * Try to generate the initial mesh if all neighbors exist.
     */
    private void tryInitialGenerateMesh(VXChunk.ChunkData cd) {
        for (int[] dir : DIRECTIONS) {
            String neighborKey = VXChunk.chunkKey(
                    cd.chunkPosX + dir[0], cd.chunkPosY + dir[1], cd.chunkPosZ + dir[2]);
            if (!chunks.containsKey(neighborKey)) {
                return;
            }
        }
        cd.isInitialMeshGenerated = true;
        VXChunk.generateChunkMesh(cd, this);
    }

    /**
     * Get the block ID at a given chunk position and block sub-position.
     */
    public int getBlockInChunk(int cx, int cy, int cz, int bx, int by, int bz) {
        String chunkKey = VXChunk.chunkKey(cx, cy, cz);
        VXChunk.ChunkData cd = chunks.get(chunkKey);
        if (cd == null) return 0;
        String blockKey = VXTerrainGenerator.blockKey(bx, by, bz);
        if (cd.data.containsKey(blockKey)) {
            return cd.data.get(blockKey);
        }
        return 0;
    }

    /**
     * Set a block at the given global position.
     */
    public void setBlockGlobalPosition(int gx, int gy, int gz, int blockId) {
        int cx = (int) Math.floor((double) gx / VXChunk.CHUNK_SIZE);
        int cy = (int) Math.floor((double) gy / VXChunk.CHUNK_SIZE);
        int cz = (int) Math.floor((double) gz / VXChunk.CHUNK_SIZE);

        String chunkKey = VXChunk.chunkKey(cx, cy, cz);
        VXChunk.ChunkData cd = chunks.get(chunkKey);
        if (cd == null) return;

        int sx = ((gx % VXChunk.CHUNK_SIZE) + VXChunk.CHUNK_SIZE) % VXChunk.CHUNK_SIZE;
        int sy = ((gy % VXChunk.CHUNK_SIZE) + VXChunk.CHUNK_SIZE) % VXChunk.CHUNK_SIZE;
        int sz = ((gz % VXChunk.CHUNK_SIZE) + VXChunk.CHUNK_SIZE) % VXChunk.CHUNK_SIZE;

        String subKey = VXTerrainGenerator.blockKey(sx, sy, sz);
        if (blockId == 0) {
            cd.data.remove(subKey);
        } else {
            cd.data.put(subKey, blockId);
        }
        VXChunk.regenerate(this, cd);

        if (VXChunk.isBlockTransparent(blockId)) {
            if (sx == 0) regenerateChunk(cx - 1, cy, cz);
            else if (sx == CHUNK_END_SIZE) regenerateChunk(cx + 1, cy, cz);
            if (sz == 0) regenerateChunk(cx, cy, cz - 1);
            else if (sz == CHUNK_END_SIZE) regenerateChunk(cx, cy, cz + 1);
            if (sy == 0) regenerateChunk(cx, cy - 1, cz);
            else if (sy == CHUNK_END_SIZE) regenerateChunk(cx, cy + 1, cz);
        }
    }

    private void regenerateChunk(int cx, int cy, int cz) {
        String key = VXChunk.chunkKey(cx, cy, cz);
        VXChunk.ChunkData cd = chunks.get(key);
        if (cd != null) {
            VXChunk.regenerate(this, cd);
        }
    }

    /**
     * Clean up all chunks and stop processing.
     */
    public void cleanUp() {
        chunks.clear();
        setProcess(false);

        Node[] children = getChildren(false);
        for (Node child : children) {
            child.call("free");
        }
    }

    private void deleteFarAwayChunks(int pcx, int pcy, int pcz) {
        oldPlayerChunkX = pcx;
        oldPlayerChunkY = pcy;
        oldPlayerChunkZ = pcz;

        effectiveRenderDistance = Math.max(1, effectiveRenderDistance - 1);

        int deletedThisFrame = 0;
        int maxDeletions = Math.max(2, Math.min(8, 2 * (renderDistance - effectiveRenderDistance)));

        Vector3 playerChunk = new Vector3(pcx, pcy, pcz);

        java.util.Iterator<Map.Entry<String, VXChunk.ChunkData>> it = chunks.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<String, VXChunk.ChunkData> entry = it.next();
            int[] cp = VXChunk.parseBlockKey(entry.getKey());
            Vector3 chunkPos = new Vector3(cp[0], cp[1], cp[2]);
            double dist = playerChunk.sub(chunkPos).length();
            if (dist > deleteDistance) {
                entry.getValue().node.queueFree();
                it.remove();
                deletedThisFrame++;
                if (deletedThisFrame > maxDeletions) {
                    deleting = true;
                    return;
                }
            }
        }

        deleting = false;
    }
}
