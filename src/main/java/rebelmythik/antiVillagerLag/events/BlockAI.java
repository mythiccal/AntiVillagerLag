package rebelmythik.antiVillagerLag.events;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Villager;
import rebelmythik.antiVillagerLag.utils.PluginSettings;
import rebelmythik.antiVillagerLag.utils.RegionTasks;
import rebelmythik.antiVillagerLag.utils.VillagerUtilities;

public class BlockAI {
    public static boolean call(Villager villager, PluginSettings settings) {
        if (!settings.useBlocks()) return false;
        Location loc = villager.getLocation();
        Location below = new Location(loc.getWorld(), loc.getBlockX(), loc.getBlockY() - 1, loc.getBlockZ());
        if (!RegionTasks.ownsChunk(below.getWorld(), below.getBlockX() >> 4, below.getBlockZ() >> 4)) {
            return false;
        }
        Material matBelow = below.getBlock().getType();
        return settings.standingBlocks().contains(matBelow);
    }
}
