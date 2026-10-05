package rebelmythik.antiVillagerLag.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import rebelmythik.antiVillagerLag.AntiVillagerLag;
import rebelmythik.antiVillagerLag.utils.VillagerUtilities;

public class ReloadCommand implements CommandExecutor {

    AntiVillagerLag plugin;

    public ReloadCommand(AntiVillagerLag plugin) {
        this.plugin = plugin;
    }

    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (cmd.getName().equalsIgnoreCase("avlreload")) {
            if (!sender.hasPermission("avl.reload")) {
                sender.sendMessage(VillagerUtilities.colorcodes.cm(VillagerUtilities.settings().noPermission()));
                return true;
            }
            sender.sendMessage(VillagerUtilities.colorcodes.cm(VillagerUtilities.settings().reloadMessage()));
            synchronized (plugin) {
                plugin.reloadConfig();
                VillagerUtilities.publish(plugin);
            }
        }
        return true;
    }
}
