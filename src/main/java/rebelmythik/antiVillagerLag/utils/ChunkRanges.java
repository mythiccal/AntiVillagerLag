package rebelmythik.antiVillagerLag.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

public final class ChunkRanges {

    private ChunkRanges() {
    }

    public record ChunkCoord(int x, int z) {
    }

    public record Split(List<ChunkCoord> inline, List<ChunkCoord> scheduled) {
    }

    public static int floorChunk(double block) {
        return (int) Math.floor(block) >> 4;
    }

    public static List<ChunkCoord> covering(double minX, double minZ, double maxX, double maxZ) {
        int minChunkX = floorChunk(minX);
        int maxChunkX = floorChunk(maxX);
        int minChunkZ = floorChunk(minZ);
        int maxChunkZ = floorChunk(maxZ);
        List<ChunkCoord> coords = new ArrayList<>();
        for (int x = minChunkX; x <= maxChunkX; x++) {
            for (int z = minChunkZ; z <= maxChunkZ; z++) {
                coords.add(new ChunkCoord(x, z));
            }
        }
        return coords;
    }

    public static Split split(List<ChunkCoord> coords, Predicate<ChunkCoord> ownedByCurrentRegion) {
        List<ChunkCoord> inline = new ArrayList<>();
        List<ChunkCoord> scheduled = new ArrayList<>();
        for (ChunkCoord coord : coords) {
            if (ownedByCurrentRegion.test(coord)) {
                inline.add(coord);
            } else {
                scheduled.add(coord);
            }
        }
        return new Split(List.copyOf(inline), List.copyOf(scheduled));
    }
}
