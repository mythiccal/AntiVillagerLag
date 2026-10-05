package rebelmythik.antiVillagerLag.events;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager;
import org.bukkit.entity.WanderingTrader;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.inventory.TradeSelectEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;
import rebelmythik.antiVillagerLag.AntiVillagerLag;
import rebelmythik.antiVillagerLag.utils.PluginSettings;
import rebelmythik.antiVillagerLag.utils.RegionTasks;
import rebelmythik.antiVillagerLag.utils.UpdateChecker;
import rebelmythik.antiVillagerLag.utils.VillagerUtilities;

public class EventListener implements Listener {

    AntiVillagerLag plugin;

    public EventListener(AntiVillagerLag plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onRightClick(PlayerInteractEntityEvent event) {
        if (event.isCancelled()) return;
        if (!(event.getRightClicked() instanceof Villager villager)) return;
        Player player = event.getPlayer();

        if (RegionTasks.owns(player) && RegionTasks.owns(villager)) {
            handleSameRegion(event, player, villager);
            return;
        }
        handleCrossRegion(event, player, villager);
    }

    private void handleSameRegion(PlayerInteractEntityEvent event, Player player, Villager villager) {
        PluginSettings settings = VillagerUtilities.settings();
        VillagerUtilities.initialize(villager, plugin);

        long currentTime = System.currentTimeMillis() / 1000;
        long vilLevelCooldown = VillagerUtilities.getLevelCooldown(villager, plugin);
        long vilAiCooldown = VillagerUtilities.getAiCooldown(villager, plugin);
        long totalSeconds = vilAiCooldown - currentTime;
        long sec = totalSeconds % 60;
        long min = totalSeconds / 60;

        if (vilLevelCooldown > currentTime) {
            String message = settings.cooldownLevelupMessage();
            long levelSec = vilLevelCooldown - currentTime;
            message = message.replaceAll("%avlseconds%", Long.toString(levelSec));
            player.sendMessage(VillagerUtilities.colorcodes.cm(message));
            villager.shakeHead();
            event.setCancelled(true);
            return;
        }

        boolean nametagResult = NameTagAI.call(villager, settings, player);
        boolean blockResult = BlockAI.call(villager, settings);
        boolean workblockResult = WorkblockAI.call(villager, settings);
        boolean shouldBeDisabled = nametagResult || blockResult || workblockResult;

        if (shouldBeDisabled == VillagerUtilities.getMarker(villager, plugin)) {
            if ((vilAiCooldown > currentTime) && !player.hasPermission("avl.cooldown.bypass")) {
                String message = settings.cooldownAiMessage();
                message = message.replaceAll("%avlminutes%", Long.toString(min));
                message = message.replaceAll("%avlseconds%", Long.toString(sec));
                player.sendMessage(VillagerUtilities.colorcodes.cm(message));
                event.setCancelled(true);
            } else {
                VillagerUtilities.setMarker(villager, plugin, !shouldBeDisabled);
                villager.setAware(!shouldBeDisabled);
                if (settings.silenceVillagers()) {
                    villager.setSilent(shouldBeDisabled);
                }
                VillagerUtilities.setAiCooldown(villager, plugin, settings.aiToggleCooldown());
                if (refundNametag(player, settings)) return;
            }
        } else {
            if (!VillagerUtilities.hasMarker(villager, plugin)) return;
            if (refundNametag(player, settings)) return;
        }

        if (!VillagerUtilities.getMarker(villager, plugin)) {
            RestockVillager.call(villager, plugin, player, player.hasPermission("avl.restockcooldown.bypass"), player.hasPermission("avl.message.nextrestock"));
        }
    }

    private void handleCrossRegion(PlayerInteractEntityEvent event, Player player, Villager villager) {
        long now = System.currentTimeMillis() / 1000;
        boolean ownsVillager = RegionTasks.owns(villager);
        boolean ownsPlayer = RegionTasks.owns(player);
        if (ownsVillager) {
            VillagerUtilities.initialize(villager, plugin);
        }

        Long levelCooldown = ownsVillager
                ? VillagerUtilities.getLevelCooldown(villager, plugin)
                : mirroredLevelCooldown(villager);
        if (levelCooldown != null && levelCooldown > now) {
            event.setCancelled(true);
            String message = VillagerUtilities.settings().cooldownLevelupMessage();
            message = message.replaceAll("%avlseconds%", Long.toString(levelCooldown - now));
            RegionTasks.sendMessage(plugin, player, VillagerUtilities.colorcodes.cm(message));
            RegionTasks.onEntity(plugin, villager, villager::shakeHead);
            return;
        }

        PluginSettings settings = VillagerUtilities.settings();
        Boolean held = ownsPlayer ? NameTagAI.fromHeldItem(player, settings) : null;
        Boolean shouldDisable = null;
        Boolean marker = null;
        Long aiCooldown = null;
        // The player is owned and the villager is not. A named nametag decides the toggle
        // without the villager; block scans still need the villager's region.
        if (ownsPlayer && held != null && !settings.useBlocks() && !settings.useWorkstations()) {
            VillagerUtilities.View view = VillagerUtilities.peek(villager.getUniqueId());
            if (view != null && view.present()) {
                shouldDisable = held;
                marker = view.marker();
                aiCooldown = view.aiCooldown();
            }
        }

        boolean knowsBypass = ownsPlayer;
        boolean bypass = ownsPlayer && player.hasPermission("avl.cooldown.bypass");
        if (shouldDisable != null && marker != null && aiCooldown != null
                && shouldDisable.equals(marker)
                && aiCooldown > now
                && knowsBypass
                && !bypass) {
            event.setCancelled(true);
            long totalSeconds = aiCooldown - now;
            String message = settings.cooldownAiMessage();
            message = message.replaceAll("%avlminutes%", Long.toString(totalSeconds / 60));
            message = message.replaceAll("%avlseconds%", Long.toString(totalSeconds % 60));
            RegionTasks.sendMessage(plugin, player, VillagerUtilities.colorcodes.cm(message));
            if (!marker) {
                boolean restockBypass = player.hasPermission("avl.restockcooldown.bypass");
                boolean showNext = player.hasPermission("avl.message.nextrestock");
                RegionTasks.onEntity(plugin, villager, () -> {
                    if (!VillagerUtilities.getMarker(villager, plugin)) {
                        RestockVillager.call(villager, plugin, player, restockBypass, showNext);
                    }
                });
            }
            return;
        }

        boolean restockBypass = ownsPlayer && player.hasPermission("avl.restockcooldown.bypass");
        boolean showNext = ownsPlayer && player.hasPermission("avl.message.nextrestock");
        RegionTasks.onEntity(plugin, villager, () -> applyWithoutEvent(villager, player, held, knowsBypass, bypass, restockBypass, showNext));
    }

    private void applyWithoutEvent(Villager villager, Player player, Boolean held, boolean knowsBypass, boolean bypass, boolean restockBypass, boolean showNext) {
        if (!knowsBypass) {
            RegionTasks.onEntity(plugin, player, () -> {
                PluginSettings settings = VillagerUtilities.settings();
                boolean playerBypass = player.hasPermission("avl.cooldown.bypass");
                boolean playerRestockBypass = player.hasPermission("avl.restockcooldown.bypass");
                boolean playerShowNext = player.hasPermission("avl.message.nextrestock");
                Boolean playerHeld = NameTagAI.fromHeldItem(player, settings);
                RegionTasks.onEntity(plugin, villager, () -> applyWithoutEvent(villager, player, playerHeld, true, playerBypass, playerRestockBypass, playerShowNext));
            });
            return;
        }

        PluginSettings settings = VillagerUtilities.settings();
        VillagerUtilities.initialize(villager, plugin);
        long now = System.currentTimeMillis() / 1000;
        long levelCooldown = VillagerUtilities.getLevelCooldown(villager, plugin);
        if (levelCooldown > now) {
            String message = settings.cooldownLevelupMessage();
            message = message.replaceAll("%avlseconds%", Long.toString(levelCooldown - now));
            RegionTasks.sendMessage(plugin, player, VillagerUtilities.colorcodes.cm(message));
            villager.shakeHead();
            return;
        }

        boolean nametagResult = held != null ? held : NameTagAI.fromName(villager, settings);
        boolean shouldBeDisabled = nametagResult || BlockAI.call(villager, settings) || WorkblockAI.call(villager, settings);
        long aiCooldown = VillagerUtilities.getAiCooldown(villager, plugin);
        if (shouldBeDisabled == VillagerUtilities.getMarker(villager, plugin)) {
            if (aiCooldown > now && !bypass) {
                long totalSeconds = aiCooldown - now;
                String message = settings.cooldownAiMessage();
                message = message.replaceAll("%avlminutes%", Long.toString(totalSeconds / 60));
                message = message.replaceAll("%avlseconds%", Long.toString(totalSeconds % 60));
                RegionTasks.sendMessage(plugin, player, VillagerUtilities.colorcodes.cm(message));
            } else {
                VillagerUtilities.setMarker(villager, plugin, !shouldBeDisabled);
                villager.setAware(!shouldBeDisabled);
                if (settings.silenceVillagers()) {
                    villager.setSilent(shouldBeDisabled);
                }
                VillagerUtilities.setAiCooldown(villager, plugin, settings.aiToggleCooldown());
                finishRefundAndRestock(villager, player, settings, restockBypass, showNext);
                return;
            }
        } else {
            if (!VillagerUtilities.hasMarker(villager, plugin)) return;
            finishRefundAndRestock(villager, player, settings, restockBypass, showNext);
            return;
        }

        if (!VillagerUtilities.getMarker(villager, plugin)) {
            RestockVillager.call(villager, plugin, player, restockBypass, showNext);
        }
    }

    private void finishRefundAndRestock(Villager villager, Player player, PluginSettings settings, boolean restockBypass, boolean showNext) {
        boolean restock = !VillagerUtilities.getMarker(villager, plugin);
        if (!RegionTasks.owns(player)) {
            RegionTasks.onEntity(plugin, player, () -> {
                if (refundNametag(player, settings)) return;
                if (restock) {
                    RegionTasks.onEntity(plugin, villager, () -> RestockVillager.call(villager, plugin, player, restockBypass, showNext));
                }
            });
            return;
        }
        if (refundNametag(player, settings)) return;
        if (restock) {
            RestockVillager.call(villager, plugin, player, restockBypass, showNext);
        }
    }

    private static Long mirroredLevelCooldown(Villager villager) {
        VillagerUtilities.View view = VillagerUtilities.peek(villager.getUniqueId());
        if (view == null || !view.present()) {
            return null;
        }
        return view.levelCooldown();
    }

    /**
     * @return true when the nametag has no display name and the caller must stop before restock
     */
    private static boolean refundNametag(Player player, PluginSettings settings) {
        if (player.getInventory().getItemInMainHand().getType().equals(Material.NAME_TAG) && !settings.useNametags()) {
            ItemStack nametag = player.getInventory().getItemInMainHand();
            if (!nametag.getItemMeta().hasDisplayName()) return true;
            player.getInventory().getItemInMainHand().setAmount(player.getInventory().getItemInMainHand().getAmount() + 1);
        }
        return false;
    }

    @EventHandler
    public void inventoryMove(InventoryClickEvent event) {
        if (event.isCancelled()) return;
        if (!(event.getInventory().getHolder() instanceof Villager vil)) return;
        if (!tradeBlocked(vil)) return;
        Player player = (Player) event.getWhoClicked();
        event.setCancelled(true);
        RegionTasks.sendMessage(plugin, player, VillagerUtilities.colorcodes.cm(VillagerUtilities.settings().mustBeDisabled()));
    }

    @EventHandler
    public void villagerTradeClick(TradeSelectEvent event) {
        if (event.isCancelled()) return;
        if (!(event.getInventory().getHolder() instanceof Villager vil)) return;
        if (!tradeBlocked(vil)) return;
        Player player = (Player) event.getWhoClicked();
        event.setCancelled(true);
        RegionTasks.onEntity(plugin, player, player::closeInventory);
        RegionTasks.sendMessage(plugin, player, VillagerUtilities.colorcodes.cm(VillagerUtilities.settings().mustBeDisabled()));
    }

    private boolean tradeBlocked(Villager vil) {
        if (!VillagerUtilities.settings().preventTrading()) return false;
        if (RegionTasks.owns(vil)) {
            if (!VillagerUtilities.hasMarker(vil, plugin)) return false;
            return VillagerUtilities.getMarker(vil, plugin);
        }
        VillagerUtilities.View view = VillagerUtilities.peek(vil.getUniqueId());
        if (view == null || !view.present()) return false;
        return view.marker();
    }

    @EventHandler
    public void onCancelVillagerDamage(EntityDamageByEntityEvent event) {
        if (event.isCancelled()) return;
        if (!(event.getEntity() instanceof Villager vil && event.getDamager() instanceof org.bukkit.entity.Zombie)) return;

        if (RegionTasks.owns(vil)) {
            if (VillagerUtilities.hasMarker(vil, plugin) && !VillagerUtilities.getMarker(vil, plugin)) {
                event.setCancelled(true);
            }
            return;
        }
        VillagerUtilities.View view = VillagerUtilities.peek(vil.getUniqueId());
        if (view != null && view.present() && !view.marker()) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void playerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        if (!RegionTasks.owns(player)) {
            RegionTasks.onEntity(plugin, player, () -> {
                if (player.hasPermission("avl.notify.update")) {
                    notifyUpdate(player);
                }
            });
            return;
        }
        if (!player.hasPermission("avl.notify.update")) return;
        notifyUpdate(player);
    }

    private void notifyUpdate(Player player) {
        new UpdateChecker(plugin, 102949).getVersion(version -> {
            if (!plugin.isEnabled()) return;
            String text = plugin.getDescription().getVersion().equals(version)
                    ? ChatColor.GREEN + "AntiVillagerLag is up to date!"
                    : ChatColor.GREEN + "There is an update for AntiVillagerLag! https://www.spigotmc.org/resources/antivillagerlag.102949/";
            RegionTasks.sendMessage(plugin, player, text);
        });
    }

    @EventHandler
    public void afterTrade(InventoryCloseEvent event) {
        Player player = (Player) event.getPlayer();
        if (event.getInventory().getHolder() == null) return;
        if (event.getInventory().getHolder() instanceof WanderingTrader) return;
        if (event.getInventory().getType() != InventoryType.MERCHANT) return;

        Villager vil = (Villager) event.getInventory().getHolder();
        if (!RegionTasks.owns(player)) {
            RegionTasks.onEntity(plugin, player, () -> finishTrade(player, vil));
            return;
        }
        finishTrade(player, vil);
    }

    private void finishTrade(Player player, Villager vil) {
        if (!plugin.isEnabled() || !player.isOnline()) return;
        if (player.hasPermission("avl.disable")) return;
        RegionTasks.onEntity(plugin, vil, () -> {
            if (!VillagerUtilities.hasMarker(vil, plugin)) return;
            if (VillagerUtilities.getMarker(vil, plugin)) return;
            VillagerLevelManager.call(vil, plugin, player);
        });
    }
}
