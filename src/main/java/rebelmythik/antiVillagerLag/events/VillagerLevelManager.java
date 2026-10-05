package rebelmythik.antiVillagerLag.events;

import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import rebelmythik.antiVillagerLag.AntiVillagerLag;
import rebelmythik.antiVillagerLag.utils.CalculateLevel;
import rebelmythik.antiVillagerLag.utils.PluginSettings;
import rebelmythik.antiVillagerLag.utils.RegionTasks;
import rebelmythik.antiVillagerLag.utils.VillagerUtilities;

public class VillagerLevelManager {

    public static void call(Villager vil, AntiVillagerLag plugin, Player player) {
        if (!RegionTasks.owns(vil)) {
            RegionTasks.onEntity(plugin, vil, () -> call(vil, plugin, player));
            return;
        }

        int cooldown = 5;
        PluginSettings settings = VillagerUtilities.settings();
        int vilLevel = vil.getVillagerLevel();
        long newLevel = CalculateLevel.villagerEXP(vil);
        long currentTime = System.currentTimeMillis() / 1000;

        long vilLevelCooldown = VillagerUtilities.getLevelCooldown(vil, plugin);
        long totalSeconds = vilLevelCooldown - currentTime;
        long sec = totalSeconds % 60;

        if (vilLevelCooldown > currentTime) {
            String message = settings.cooldownLevelupMessage();
            message = message.replaceAll("%avlseconds%", Long.toString(sec));
            RegionTasks.sendMessage(plugin, player, VillagerUtilities.colorcodes.cm(message));
            return;
        }

        if (vilLevel < newLevel) {
            VillagerUtilities.setLevelCooldown(vil, plugin, cooldown);
            // make villager immovable while AI is disabled
            vil.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, (int)(cooldown * 20)+20, 120, false, false));
            vil.setAware(true);
        } else return;

        ScheduledTask task = vil.getScheduler().runDelayed(plugin, scheduled -> {
            if (!plugin.isEnabled() || !vil.isValid()) {
                return;
            }
            vil.setAware(false);
        }, null, 100L);
        plugin.track(task);
    }
}
