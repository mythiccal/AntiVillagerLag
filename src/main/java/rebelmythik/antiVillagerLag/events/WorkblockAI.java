package rebelmythik.antiVillagerLag.events;

import org.bukkit.Location;
import org.bukkit.entity.Villager;
import rebelmythik.antiVillagerLag.utils.ChunkRanges;
import rebelmythik.antiVillagerLag.utils.PluginSettings;
import rebelmythik.antiVillagerLag.utils.RegionTasks;
import rebelmythik.antiVillagerLag.utils.VillagerUtilities;

public class WorkblockAI {

    public static boolean call(Villager villager, PluginSettings settings) {
        //check if workstation is disabled
        if (!settings.useWorkstations()) return false;

        int radius = settings.workstationCheckRadius();
        Location origin = villager.getLocation();
        int minChunkX = ChunkRanges.floorChunk(origin.getX() - radius);
        int maxChunkX = ChunkRanges.floorChunk(origin.getX() + radius);
        int minChunkZ = ChunkRanges.floorChunk(origin.getZ() - radius);
        int maxChunkZ = ChunkRanges.floorChunk(origin.getZ() + radius);
        if (origin.getWorld() == null || !RegionTasks.ownsChunkArea(origin.getWorld(), minChunkX, minChunkZ, maxChunkX, maxChunkZ)) {
            return false;
        }
        // Check for blocks within the specified radius
        for (int x = -radius; x <= radius; x++) {
            for (int y = -radius; y <= radius; y++) {
                for (int z = -radius; z <= radius; z++) {
                    Location blockLocation = new Location(villager.getWorld(), villager.getLocation().getX() + x, villager.getLocation().getY() + y, villager.getLocation().getZ() + z);
                    if (settings.standingBlocks().contains(blockLocation.getBlock().getType())) {
                        return true;
                    }
                }
            }
        }
        return false;
    }
}
