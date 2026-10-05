package rebelmythik.antiVillagerLag.utils;

import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager;
import org.bukkit.plugin.Plugin;
import org.bukkit.util.BoundingBox;

import java.util.function.Consumer;

public final class VillagerArea {

    private VillagerArea() {
    }

    public static void forEachOverlapping(Plugin plugin, Player player, int radius, Consumer<Villager> action) {
        BoundingBox search = player.getBoundingBox().expand(radius, radius, radius);
        World world = player.getWorld();
        int minChunkX = ChunkRanges.floorChunk(search.getMinX() - 2);
        int maxChunkX = ChunkRanges.floorChunk(search.getMaxX() + 2);
        int minChunkZ = ChunkRanges.floorChunk(search.getMinZ() - 2);
        int maxChunkZ = ChunkRanges.floorChunk(search.getMaxZ() + 2);
        for (int chunkX = minChunkX; chunkX <= maxChunkX; chunkX++) {
            for (int chunkZ = minChunkZ; chunkZ <= maxChunkZ; chunkZ++) {
                if (!world.isChunkLoaded(chunkX, chunkZ)) {
                    continue;
                }
                int x = chunkX;
                int z = chunkZ;
                RegionTasks.onChunk(plugin, world, x, z, () -> visitChunk(plugin, world, x, z, search, action));
            }
        }
    }

    private static void visitChunk(Plugin plugin, World world, int chunkX, int chunkZ, BoundingBox search, Consumer<Villager> action) {
        if (!plugin.isEnabled() || !world.isChunkLoaded(chunkX, chunkZ)) {
            return;
        }
        for (Entity entity : world.getChunkAt(chunkX, chunkZ).getEntities()) {
            if (!(entity instanceof Villager villager)) {
                continue;
            }
            if (!villager.getBoundingBox().overlaps(search)) {
                continue;
            }
            RegionTasks.onEntity(plugin, villager, () -> action.accept(villager));
        }
    }
}
