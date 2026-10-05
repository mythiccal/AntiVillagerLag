package rebelmythik.antiVillagerLag.commands;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager;
import rebelmythik.antiVillagerLag.AntiVillagerLag;
import rebelmythik.antiVillagerLag.utils.RegionTasks;
import rebelmythik.antiVillagerLag.utils.VillagerUtilities;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class RemoveChangesCommand implements CommandExecutor {

    private final AntiVillagerLag plugin;
    private final Set<UUID> pendingConfirm = ConcurrentHashMap.newKeySet();

    public RemoveChangesCommand(AntiVillagerLag plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!command.getName().equalsIgnoreCase("avlremove")) return true;
        if (sender instanceof Player player) {
            if (!RegionTasks.owns(player)) {
                RegionTasks.onEntity(plugin, player, () -> handlePlayer(player, args));
                return true;
            }
            handlePlayer(player, args);
            return true;
        }
        if (args.length == 1 && args[0].equalsIgnoreCase("confirm")) {
            removeVillagerChanges(sender);
            return true;
        }
        sender.sendMessage("Invalid arguments. Usage: /avlremove confirm");
        return true;
    }

    private void handlePlayer(Player player, String[] args) {
        if (args.length == 0) {
            player.sendMessage("Type '/avlremove confirm' to remove all changes made by this plugin");
            pendingConfirm.add(player.getUniqueId());
            return;
        }
        if (args.length == 1 && args[0].equalsIgnoreCase("confirm")) {
            if (pendingConfirm.remove(player.getUniqueId())) {
                removeVillagerChanges(player);
            } else {
                player.sendMessage("No confirmation pending.");
            }
            return;
        }
        player.sendMessage("Invalid arguments. Usage: /avlremove or /avlremove confirm");
    }

    private void removeVillagerChanges(CommandSender sender) {
        List<ChunkTarget> chunks = new ArrayList<>();
        for (World world : Bukkit.getWorlds()) {
            // Paper 26.2 CraftWorld#getLoadedChunks copies loaded chunk coordinates.
            // Entity and block data are read later on the region that owns each chunk.
            for (Chunk chunk : world.getLoadedChunks()) {
                chunks.add(new ChunkTarget(world, chunk.getX(), chunk.getZ()));
            }
        }
        if (chunks.isEmpty()) {
            finish(sender);
            return;
        }
        AtomicInteger remaining = new AtomicInteger(chunks.size());
        for (ChunkTarget target : chunks) {
            RegionTasks.onChunk(plugin, target.world, target.x, target.z, () -> {
                try {
                    cleanseChunk(target);
                } finally {
                    if (remaining.decrementAndGet() == 0) {
                        finish(sender);
                    }
                }
            });
        }
    }

    private void cleanseChunk(ChunkTarget target) {
        if (!plugin.isEnabled() || !target.world.isChunkLoaded(target.x, target.z)) {
            return;
        }
        for (Entity entity : target.world.getChunkAt(target.x, target.z).getEntities()) {
            if (entity instanceof Villager villager) {
                RegionTasks.onEntity(plugin, villager, () -> VillagerUtilities.CleanseTheVillagers(villager, plugin));
            }
        }
    }

    private void finish(CommandSender sender) {
        if (sender instanceof Player player) {
            RegionTasks.onEntity(plugin, player, () -> {
                if (player.isOnline()) {
                    player.sendMessage("All villagers have been removed.");
                }
            });
            return;
        }
        RegionTasks.onGlobal(plugin, () -> sender.sendMessage("All changes have been removed."));
    }

    private record ChunkTarget(World world, int x, int z) {
    }
}
