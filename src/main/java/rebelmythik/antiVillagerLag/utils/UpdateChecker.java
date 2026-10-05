package rebelmythik.antiVillagerLag.utils;

import org.bukkit.Bukkit;
import rebelmythik.antiVillagerLag.AntiVillagerLag;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Scanner;
import java.util.function.Consumer;

// From: https://www.spigotmc.org/wiki/creating-an-update-checker-that-checks-for-updates
public class UpdateChecker {

    private final AntiVillagerLag plugin;
    private final int resourceId;

    public UpdateChecker(AntiVillagerLag plugin, int resourceId) {
        this.plugin = plugin;
        this.resourceId = resourceId;
    }

    public void getVersion(final Consumer<String> consumer) {
        plugin.track(Bukkit.getAsyncScheduler().runNow(plugin, task -> {
            if (!plugin.isEnabled()) {
                return;
            }
            try (InputStream is = new URL("https://api.spigotmc.org/legacy/update.php?resource=" + this.resourceId + "/~").openStream(); Scanner scann = new Scanner(is)) {
                if (scann.hasNext()) {
                    String version = scann.next();
                    if (plugin.isEnabled()) {
                        consumer.accept(version);
                    }
                }
            } catch (IOException e) {
                plugin.getLogger().info("Unable to check for updates: " + e.getMessage());
            }
        }));
    }
}
