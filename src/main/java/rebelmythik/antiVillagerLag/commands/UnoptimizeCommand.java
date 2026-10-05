package rebelmythik.antiVillagerLag.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager;
import rebelmythik.antiVillagerLag.AntiVillagerLag;
import rebelmythik.antiVillagerLag.utils.PluginSettings;
import rebelmythik.antiVillagerLag.utils.RegionTasks;
import rebelmythik.antiVillagerLag.utils.VillagerArea;
import rebelmythik.antiVillagerLag.utils.VillagerUtilities;

public class UnoptimizeCommand implements CommandExecutor {

    AntiVillagerLag plugin;

    public UnoptimizeCommand(AntiVillagerLag plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {
        if (!command.getName().equalsIgnoreCase("avlunoptimize")) return true;
        if (!(commandSender instanceof Player player)) return false;
        if (!RegionTasks.owns(player)) {
            RegionTasks.onEntity(plugin, player, () -> run(player, strings));
            return true;
        }
        run(player, strings);
        return true;
    }

    private void run(Player player, String[] strings) {
        PluginSettings settings = VillagerUtilities.settings();
        if (!player.hasPermission("avl.unoptimize")) {
            player.sendMessage(VillagerUtilities.colorcodes.cm(settings.noPermission()));
            return;
        }

        int radius;
        try {
            radius = (strings != null && strings.length > 0) ? Integer.parseInt(strings[0]) : settings.radiusDefault();
        } catch (NumberFormatException e) {
            player.sendMessage(VillagerUtilities.colorcodes.cm(settings.radiusInvalid()));
            return;
        }
        boolean canSearchRadius = radius <= settings.radiusLimit();
        if (!canSearchRadius) {
            player.sendMessage(VillagerUtilities.colorcodes.cm(settings.radiusLimitMessage()).replace("%avlradiuslimit%", Integer.toString(settings.radiusLimit())));
            return;
        }
        player.sendMessage(VillagerUtilities.colorcodes.cm(settings.searchingRadius()).replace("%avlradius%", String.valueOf(radius)));

        VillagerArea.forEachOverlapping(plugin, player, radius, this::unoptimize);
    }

    private void unoptimize(Villager villager) {
        VillagerUtilities.initialize(villager, plugin);
        villager.setCustomName("");
        VillagerUtilities.setMarker(villager, plugin, true);
        villager.setAware(true);
    }
}
