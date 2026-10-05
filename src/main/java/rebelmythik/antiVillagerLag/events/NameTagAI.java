package rebelmythik.antiVillagerLag.events;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager;
import org.bukkit.inventory.ItemStack;
import rebelmythik.antiVillagerLag.utils.PluginSettings;
import rebelmythik.antiVillagerLag.utils.VillagerUtilities;

public class NameTagAI {

    public static boolean call(Villager villager, PluginSettings settings, Player player) {
        if (!settings.useRenaming()) return false;
        Boolean fromItem = fromHeldItem(player, settings);
        if (fromItem != null) {
            return fromItem;
        }
        return fromName(villager, settings);
    }

    /**
     * @return the nametag decision, or null when the villager's current name decides it
     */
    public static Boolean fromHeldItem(Player player, PluginSettings settings) {
        if (!settings.useRenaming()) return false;
        ItemStack nametag = player.getInventory().getItemInMainHand();
        if (!nametag.getType().equals(Material.NAME_TAG)) {
            return null;
        }
        if (!nametag.getItemMeta().hasDisplayName()) {
            return null;
        }
        String itemName = nametag.getItemMeta().getDisplayName().replaceAll("(?i)[§&][0-9A-FK-ORXLo]", "");
        return settings.disablingNames().contains(itemName.toLowerCase());
    }

    public static boolean fromName(Villager villager, PluginSettings settings) {
        if (!settings.useRenaming()) return false;
        String name = villager.getCustomName();
        if (name != null) name = name.toLowerCase().replaceAll("(?i)[§&][0-9A-FK-ORXLo]", "");
        return settings.disablingNames().contains(name);
    }
}
