package demos.threed.voxel;

import org.godot.math.Vector3;
import org.godot.node.*;

import java.util.Map;

/**
 * Utility class for voxel chunk mesh generation and block calculations.
 * Port of world/chunk.gd logic.
 */
public class VXChunk {

    public static final int CHUNK_SIZE = 16;
    public static final int TEXTURE_SHEET_WIDTH = 8;
    public static final int CHUNK_LAST_INDEX = CHUNK_SIZE - 1;
    public static final double TEXTURE_TILE_SIZE = 1.0 / TEXTURE_SHEET_WIDTH;
    public static final Vector3 CHUNK_EXTENTS = new Vector3(0.5, 0.5, 0.5);

    // Six cardinal directions as block offsets.
    private static final int[][] DIRECTIONS = {
            {-1, 0, 0}, {1, 0, 0},
            {0, -1, 0}, {0, 1, 0},
            {0, 0, -1}, {0, 0, 1}
    };

    /**
     * Data holder for a single chunk.
     */
    public static class ChunkData {
        public Map<String, Integer> data;
        public int chunkPosX, chunkPosY, chunkPosZ;
        public boolean isInitialMeshGenerated = false;
        public long meshTaskId = 0;
        public StaticBody3D node;
    }

    /**
     * Initialize a chunk: set position, name, generate data and collider.
     */
    public static ChunkData initChunk(StaticBody3D node, int posX, int posY, int posZ, int worldType) {
        ChunkData cd = new ChunkData();
        cd.node = node;
        cd.chunkPosX = posX;
        cd.chunkPosY = posY;
        cd.chunkPosZ = posZ;

        Vector3 worldPos = new Vector3(posX * CHUNK_SIZE, posY * CHUNK_SIZE, posZ * CHUNK_SIZE);
        node.setPosition(worldPos);
        node.call("set_name", posX + "," + posY + "," + posZ);

        if (worldType == 0) {
            cd.data = VXTerrainGenerator.randomBlocks();
        } else {
            cd.data = VXTerrainGenerator.flat(posX, posY, posZ);
        }

        generateChunkCollider(node, cd.data);
        return cd;
    }

    /**
     * Generate colliders for all solid blocks in the chunk.
     */
    public static void generateChunkCollider(StaticBody3D node, Map<String, Integer> data) {
        if (data.isEmpty()) return;

        for (Map.Entry<String, Integer> entry : data.entrySet()) {
            int blockId = entry.getValue();
            if (blockId != 27 && blockId != 28) {
                int[] pos = parseBlockKey(entry.getKey());
                createBlockCollider(node, pos[0], pos[1], pos[2]);
            }
        }
    }

    private static void createBlockCollider(StaticBody3D node, int bx, int by, int bz) {
        CollisionShape3D collider = CollisionShape3D.create();
        BoxShape3D boxShape = BoxShape3D.create();
        boxShape.setSize(new Vector3(1, 1, 1));
        collider.setShape(boxShape);
        collider.setPosition(new Vector3(bx + 0.5, by + 0.5, bz + 0.5));
        node.call("add_child", collider);
    }

    /**
     * Generate mesh for a chunk. Must be called on the main thread.
     */
    public static void generateChunkMesh(ChunkData cd, VXVoxelWorld voxelWorld) {
        if (cd.data.isEmpty()) return;

        SurfaceTool surfaceTool = SurfaceTool.create();
        surfaceTool.begin(Mesh.PrimitiveType.PRIMITIVE_TRIANGLES.value);

        for (Map.Entry<String, Integer> entry : cd.data.entrySet()) {
            int[] blockPos = parseBlockKey(entry.getKey());
            int blockId = entry.getValue();
            drawBlockMesh(surfaceTool, blockPos[0], blockPos[1], blockPos[2], blockId,
                    cd, voxelWorld);
        }

        surfaceTool.generate_tangents();
        surfaceTool.index();
        ArrayMesh arrayMesh = surfaceTool.commit(null, 0);

        MeshInstance3D mi = MeshInstance3D.create();
        mi.setProperty("mesh", arrayMesh);
        java.lang.Object matObj = cd.node.call("load", "res://world/textures/material.tres");
        if (matObj != null) {
            mi.setProperty("material_override", matObj);
        }
        cd.node.call("call_deferred", "add_child", mi);
    }

    private static void drawBlockMesh(SurfaceTool st, int bx, int by, int bz, int blockId,
                                       ChunkData cd, VXVoxelWorld voxelWorld) {
        Map<String, Integer> data = cd.data;
        int cpx = cd.chunkPosX, cpy = cd.chunkPosY, cpz = cd.chunkPosZ;

        Vector3[] verts = calculateBlockVerts(bx, by, bz);
        double[][] uvs = calculateBlockUVs(blockId);
        double[][] topUvs = uvs;
        double[][] bottomUvs = uvs;

        if (blockId == 27 || blockId == 28) {
            Vector3 n1 = new Vector3(-1, 0, 1).normalized();
            Vector3 n2 = new Vector3(1, 0, -1).normalized();
            Vector3 n3 = new Vector3(1, 0, 1).normalized();
            Vector3 n4 = new Vector3(-1, 0, -1).normalized();
            drawBlockFace(st, new Vector3[]{verts[2], verts[0], verts[7], verts[5]}, uvs, n1);
            drawBlockFace(st, new Vector3[]{verts[7], verts[5], verts[2], verts[0]}, uvs, n2);
            drawBlockFace(st, new Vector3[]{verts[3], verts[1], verts[6], verts[4]}, uvs, n3);
            drawBlockFace(st, new Vector3[]{verts[6], verts[4], verts[3], verts[1]}, uvs, n4);
            return;
        }

        if (blockId == 3) { topUvs = calculateBlockUVs(0); bottomUvs = calculateBlockUVs(2); }
        else if (blockId == 5) { topUvs = calculateBlockUVs(31); bottomUvs = topUvs; }
        else if (blockId == 12) { topUvs = calculateBlockUVs(30); bottomUvs = topUvs; }
        else if (blockId == 19) { topUvs = calculateBlockUVs(4); bottomUvs = topUvs; }

        // LEFT face (x-)
        int otherBlockId = getNeighborBlock(data, bx - 1, by, bz, cpx - 1, cpy, cpz, CHUNK_SIZE - 1, by, bz, voxelWorld);
        if (blockId != otherBlockId && isBlockTransparent(otherBlockId)) {
            drawBlockFace(st, new Vector3[]{verts[2], verts[0], verts[3], verts[1]}, uvs, Vector3.LEFT);
        }

        // RIGHT face (x+)
        otherBlockId = getNeighborBlock(data, bx + 1, by, bz, cpx + 1, cpy, cpz, 0, by, bz, voxelWorld);
        if (blockId != otherBlockId && isBlockTransparent(otherBlockId)) {
            drawBlockFace(st, new Vector3[]{verts[7], verts[5], verts[6], verts[4]}, uvs, Vector3.RIGHT);
        }

        // FORWARD face (z-)
        otherBlockId = getNeighborBlock(data, bx, by, bz - 1, cpx, cpy, cpz - 1, bx, by, CHUNK_SIZE - 1, voxelWorld);
        if (blockId != otherBlockId && isBlockTransparent(otherBlockId)) {
            drawBlockFace(st, new Vector3[]{verts[6], verts[4], verts[2], verts[0]}, uvs, new Vector3(0, 0, -1));
        }

        // BACK face (z+)
        otherBlockId = getNeighborBlock(data, bx, by, bz + 1, cpx, cpy, cpz + 1, bx, by, 0, voxelWorld);
        if (blockId != otherBlockId && isBlockTransparent(otherBlockId)) {
            drawBlockFace(st, new Vector3[]{verts[3], verts[1], verts[7], verts[5]}, uvs, new Vector3(0, 0, 1));
        }

        // DOWN face (y-)
        otherBlockId = getNeighborBlock(data, bx, by - 1, bz, cpx, cpy - 1, cpz, bx, CHUNK_SIZE - 1, bz, voxelWorld);
        if (blockId != otherBlockId && isBlockTransparent(otherBlockId)) {
            drawBlockFace(st, new Vector3[]{verts[4], verts[5], verts[0], verts[1]}, bottomUvs, Vector3.DOWN);
        }

        // UP face (y+)
        otherBlockId = getNeighborBlock(data, bx, by + 1, bz, cpx, cpy + 1, cpz, bx, 0, bz, voxelWorld);
        if (blockId != otherBlockId && isBlockTransparent(otherBlockId)) {
            drawBlockFace(st, new Vector3[]{verts[2], verts[3], verts[6], verts[7]}, topUvs, Vector3.UP);
        }
    }

    /**
     * Get a neighbor block, checking within local data first, then crossing chunk boundaries.
     */
    private static int getNeighborBlock(Map<String, Integer> data,
                                         int localX, int localY, int localZ,
                                         int ncx, int ncy, int ncz,
                                         int nbx, int nby, int nbz,
                                         VXVoxelWorld voxelWorld) {
        // Check if within chunk bounds
        if (localX >= 0 && localX < CHUNK_SIZE &&
            localY >= 0 && localY < CHUNK_SIZE &&
            localZ >= 0 && localZ < CHUNK_SIZE) {
            String key = VXTerrainGenerator.blockKey(localX, localY, localZ);
            if (data.containsKey(key)) return data.get(key);
            return 0;
        }
        // Cross chunk boundary
        if (voxelWorld != null) {
            return voxelWorld.getBlockInChunk(ncx, ncy, ncz, nbx, nby, nbz);
        }
        return 0;
    }

    private static void drawBlockFace(SurfaceTool st, Vector3[] verts, double[][] uvs, Vector3 normal) {
        st.call("set_normal", normal);

        st.call("set_uv", new org.godot.math.Vector2(uvs[1][0], uvs[1][1]));
        st.add_vertex(verts[1]);
        st.call("set_uv", new org.godot.math.Vector2(uvs[2][0], uvs[2][1]));
        st.add_vertex(verts[2]);
        st.call("set_uv", new org.godot.math.Vector2(uvs[3][0], uvs[3][1]));
        st.add_vertex(verts[3]);

        st.call("set_uv", new org.godot.math.Vector2(uvs[2][0], uvs[2][1]));
        st.add_vertex(verts[2]);
        st.call("set_uv", new org.godot.math.Vector2(uvs[1][0], uvs[1][1]));
        st.add_vertex(verts[1]);
        st.call("set_uv", new org.godot.math.Vector2(uvs[0][0], uvs[0][1]));
        st.add_vertex(verts[0]);
    }

    /**
     * Regenerate a chunk after a block change.
     */
    public static void regenerate(VXVoxelWorld voxelWorld, ChunkData cd) {
        voxelWorld.call("remove_child", cd.node);

        Node[] children = cd.node.get_children(false);
        for (Node child : children) {
            cd.node.call("remove_child", child);
            child.queue_free();
        }

        generateChunkCollider(cd.node, cd.data);
        generateChunkMesh(cd, voxelWorld);

        voxelWorld.call("add_child", cd.node);
    }

    // ==================== Static utility methods ====================

    public static double[][] calculateBlockUVs(int blockId) {
        int row = blockId / TEXTURE_SHEET_WIDTH;
        int col = blockId % TEXTURE_SHEET_WIDTH;

        double u0 = TEXTURE_TILE_SIZE * (col + 0.01);
        double v0 = TEXTURE_TILE_SIZE * (row + 0.01);
        double u1 = TEXTURE_TILE_SIZE * (col + 0.99);
        double v1 = TEXTURE_TILE_SIZE * (row + 0.01);
        double u2 = TEXTURE_TILE_SIZE * (col + 0.01);
        double v2 = TEXTURE_TILE_SIZE * (row + 0.99);
        double u3 = TEXTURE_TILE_SIZE * (col + 0.99);
        double v3 = TEXTURE_TILE_SIZE * (row + 0.99);

        return new double[][]{
                {u0, v0}, {u2, v2}, {u1, v1}, {u3, v3}
        };
    }

    public static Vector3[] calculateBlockVerts(int bx, int by, int bz) {
        return new Vector3[]{
                new Vector3(bx, by, bz), new Vector3(bx, by, bz + 1),
                new Vector3(bx, by + 1, bz), new Vector3(bx, by + 1, bz + 1),
                new Vector3(bx + 1, by, bz), new Vector3(bx + 1, by, bz + 1),
                new Vector3(bx + 1, by + 1, bz), new Vector3(bx + 1, by + 1, bz + 1),
        };
    }

    public static boolean isBlockTransparent(int blockId) {
        return blockId == 0 || (blockId > 25 && blockId < 30);
    }

    public static String chunkKey(int cx, int cy, int cz) {
        return cx + "," + cy + "," + cz;
    }

    public static int[] parseBlockKey(String key) {
        String[] parts = key.split(",");
        return new int[]{Integer.parseInt(parts[0]), Integer.parseInt(parts[1]), Integer.parseInt(parts[2])};
    }
}
