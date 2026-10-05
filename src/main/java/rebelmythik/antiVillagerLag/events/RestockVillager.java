package rebelmythik.antiVillagerLag.events;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager;
import rebelmythik.antiVillagerLag.AntiVillagerLag;
import rebelmythik.antiVillagerLag.utils.PluginSettings;
import rebelmythik.antiVillagerLag.utils.RegionTasks;
import rebelmythik.antiVillagerLag.utils.RestockMath;
import rebelmythik.antiVillagerLag.utils.VillagerUtilities;

public class RestockVillager {

    private static void restockMessage(long timeTillNextRestock, Player player, AntiVillagerLag plugin, PluginSettings settings) {
        long totalsec = timeTillNextRestock / 20;
        long sec = totalsec % 60;
        long min = (totalsec - sec) / 60;
        String message = settings.nextRestock();
        message = message.replaceAll("%avlrestockmin%", Long.toString(min));
        message = message.replaceAll("%avlrestocksec%", Long.toString(sec));
        RegionTasks.sendMessage(plugin, player, VillagerUtilities.colorcodes.cm(message));
    }

    public static void call(Villager vil, AntiVillagerLag plugin, Player player, boolean restockBypass, boolean showNextRestock) {
        if (!RegionTasks.owns(vil)) {
            RegionTasks.onEntity(plugin, vil, () -> call(vil, plugin, player, restockBypass, showNextRestock));
            return;
        }
        PluginSettings settings = VillagerUtilities.settings();

        // Permission to Bypass restock cooldown
        if (restockBypass) {
            VillagerUtilities.restock(vil);
            VillagerUtilities.setLastRestock(vil, plugin);
            return;
        }

        // Check if it's time to restock. The overworld clock is global state, not a region.
        World overworld = Bukkit.getWorlds().getFirst();
        long worldTick = overworld.getFullTime();
        long currentDayTick = overworld.getTime();
        long vilTick = VillagerUtilities.getLastRestock(vil, plugin);

        if (RestockMath.shouldRestock(worldTick, currentDayTick, vilTick, settings.restockTimes())) {
            VillagerUtilities.restock(vil);
            VillagerUtilities.setLastRestock(vil, plugin);
            return;
        }

        // check if he gets to see cool-down time
        if (showNextRestock) {
            long timeTillNextRestock = RestockMath.ticksUntilNextRestock(worldTick, currentDayTick, settings.restockTimes());
            restockMessage(timeTillNextRestock, player, plugin, settings);
        }
    }
}
