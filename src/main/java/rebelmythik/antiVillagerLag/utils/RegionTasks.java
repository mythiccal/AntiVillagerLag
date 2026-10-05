package rebelmythik.antiVillagerLag.utils;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

public final class RegionTasks {

    private RegionTasks() {
    }

    public static boolean owns(Entity entity) {
        return Bukkit.isOwnedByCurrentRegion(entity);
    }

    public static boolean ownsChunk(World world, int chunkX, int chunkZ) {
        return Bukkit.isOwnedByCurrentRegion(world, chunkX, chunkZ);
    }

    public static boolean ownsChunkArea(World world, int minChunkX, int minChunkZ, int maxChunkX, int maxChunkZ) {
        return Bukkit.isOwnedByCurrentRegion(world, minChunkX, minChunkZ, maxChunkX, maxChunkZ);
    }

    public static void onEntity(Plugin plugin, Entity entity, Runnable action) {
        if (!plugin.isEnabled() || entity == null) {
            return;
        }
        if (Bukkit.isOwnedByCurrentRegion(entity)) {
            if (usable(entity)) {
                action.run();
            }
            return;
        }
        entity.getScheduler().run(plugin, task -> {
            if (!plugin.isEnabled() || !usable(entity)) {
                return;
            }
            action.run();
        }, null);
    }

    public static void onChunk(Plugin plugin, World world, int chunkX, int chunkZ, Runnable action) {
        if (!plugin.isEnabled() || world == null) {
            return;
        }
        if (Bukkit.isOwnedByCurrentRegion(world, chunkX, chunkZ)) {
            action.run();
            return;
        }
        Bukkit.getRegionScheduler().execute(plugin, world, chunkX, chunkZ, () -> {
            if (!plugin.isEnabled()) {
                return;
            }
            action.run();
        });
    }

    public static void onGlobal(Plugin plugin, Runnable action) {
        if (!plugin.isEnabled()) {
            return;
        }
        if (Bukkit.isGlobalTickThread()) {
            action.run();
            return;
        }
        Bukkit.getGlobalRegionScheduler().run(plugin, task -> {
            if (!plugin.isEnabled()) {
                return;
            }
            action.run();
        });
    }

    public static void sendMessage(Plugin plugin, Player player, String message) {
        onEntity(plugin, player, () -> player.sendMessage(message));
    }

    private static boolean usable(Entity entity) {
        if (entity instanceof Player player) {
            return player.isOnline();
        }
        return entity.isValid();
    }
}
