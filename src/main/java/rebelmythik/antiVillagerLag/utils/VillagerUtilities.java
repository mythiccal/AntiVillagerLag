package rebelmythik.antiVillagerLag.utils;

import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Villager;
import org.bukkit.inventory.MerchantRecipe;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import rebelmythik.antiVillagerLag.AntiVillagerLag;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class VillagerUtilities {

    private static final String MARKER_KEY = "Marker";
    private static final String AI_COOLDOWN_KEY = "cooldown";
    private static final String LEVEL_COOLDOWN_KEY = "levelCooldown";
    private static final String LAST_RESTOCK_KEY = "time";
    public static final ColorCode colorcodes = new ColorCode();

    private static final SettingsStore SETTINGS = new SettingsStore(PluginSettings.empty());
    private static final ConcurrentHashMap<UUID, Mirror> MIRRORS = new ConcurrentHashMap<>();

    public static PluginSettings settings() {
        return SETTINGS.get();
    }

    public static void publish(AntiVillagerLag plugin) {
        SETTINGS.publish(SETTINGS.get().merge(plugin.getConfig()));
    }

    /**
     * Thread-safe copy of the marker and cooldowns. Written only while the
     * villager's region is owned. Other regions may read it to decide a
     * synchronous cancel without touching the entity.
     */
    public static final class View {
        private final boolean present;
        private final boolean marker;
        private final long aiCooldown;
        private final long levelCooldown;
        private final long lastRestock;

        private View(boolean present, boolean marker, long aiCooldown, long levelCooldown, long lastRestock) {
            this.present = present;
            this.marker = marker;
            this.aiCooldown = aiCooldown;
            this.levelCooldown = levelCooldown;
            this.lastRestock = lastRestock;
        }

        public boolean present() {
            return present;
        }

        public boolean marker() {
            return marker;
        }

        public long aiCooldown() {
            return aiCooldown;
        }

        public long levelCooldown() {
            return levelCooldown;
        }

        public long lastRestock() {
            return lastRestock;
        }
    }

    private static final class Mirror {
        volatile boolean present;
        volatile boolean marker;
        volatile long aiCooldown;
        volatile long levelCooldown;
        volatile long lastRestock;

        View view() {
            return new View(present, marker, aiCooldown, levelCooldown, lastRestock);
        }
    }

    public static View peek(UUID villagerId) {
        Mirror mirror = MIRRORS.get(villagerId);
        return mirror == null ? null : mirror.view();
    }

    private static Mirror mirror(Villager villager) {
        return MIRRORS.computeIfAbsent(villager.getUniqueId(), id -> new Mirror());
    }

    public static void setMarker(Villager v, AntiVillagerLag plugin, boolean val) {
        PersistentDataContainer container = v.getPersistentDataContainer();
        NamespacedKey key = new NamespacedKey(plugin, MARKER_KEY);
        container.set(key, PersistentDataType.BOOLEAN, val);
        Mirror stored = mirror(v);
        stored.present = true;
        stored.marker = val;
    }

    public static boolean hasMarker(Villager v, AntiVillagerLag plugin) {
        PersistentDataContainer container = v.getPersistentDataContainer();
        NamespacedKey key = new NamespacedKey(plugin, MARKER_KEY);
        boolean has = container.has(key, PersistentDataType.BOOLEAN);
        mirror(v).present = has;
        return has;
    }

    // If false, then villager is disabled
    public static boolean getMarker(Villager v, AntiVillagerLag plugin) {
        PersistentDataContainer container = v.getPersistentDataContainer();
        NamespacedKey key = new NamespacedKey(plugin, MARKER_KEY);
        boolean marker = container.get(key, PersistentDataType.BOOLEAN);
        Mirror stored = mirror(v);
        stored.present = true;
        stored.marker = marker;
        return marker;
    }

    public static void removeMarker(Villager v, AntiVillagerLag plugin) {
        PersistentDataContainer container = v.getPersistentDataContainer();
        NamespacedKey key = new NamespacedKey(plugin, MARKER_KEY);
        container.remove(key);
        Mirror stored = mirror(v);
        stored.present = false;
    }

    public static void createData(Villager v, AntiVillagerLag plugin, String key_name, long data) {
        PersistentDataContainer container = v.getPersistentDataContainer();
        NamespacedKey key = new NamespacedKey(plugin, key_name);
        container.set(key, PersistentDataType.LONG, data);
        remember(v, key_name, data);
    }

    public static long getData(Villager v, AntiVillagerLag plugin, String key_name) {
        PersistentDataContainer container = v.getPersistentDataContainer();
        NamespacedKey key = new NamespacedKey(plugin, key_name);
        long data = container.get(key, PersistentDataType.LONG);
        remember(v, key_name, data);
        return data;
    }

    public static void removeData(Villager v, AntiVillagerLag plugin, String key_name) {
        PersistentDataContainer container = v.getPersistentDataContainer();
        NamespacedKey key = new NamespacedKey(plugin, key_name);
        container.remove(key);
    }

    private static void remember(Villager villager, String keyName, long data) {
        Mirror stored = mirror(villager);
        if (AI_COOLDOWN_KEY.equals(keyName)) {
            stored.aiCooldown = data;
        } else if (LEVEL_COOLDOWN_KEY.equals(keyName)) {
            stored.levelCooldown = data;
        } else if (LAST_RESTOCK_KEY.equals(keyName)) {
            stored.lastRestock = data;
        }
    }

    public static void setAiCooldown(Villager v, AntiVillagerLag plugin, long cooldown) {
        createData(v, plugin, AI_COOLDOWN_KEY, (System.currentTimeMillis() / 1000) + cooldown);
    }

    public static void setLevelCooldown(Villager v, AntiVillagerLag plugin, long cooldown) {
        createData(v, plugin, LEVEL_COOLDOWN_KEY, (System.currentTimeMillis() / 1000) + cooldown);
    }

    public static void setLastRestock(Villager v, AntiVillagerLag plugin) {
        // World clock read. Paper 26.2 CraftWorld#getFullTime only returns the clock;
        // it does not touch region-owned block or entity state.
        createData(v, plugin, LAST_RESTOCK_KEY, Bukkit.getWorlds().getFirst().getFullTime());
    }

    public static long getAiCooldown(Villager v, AntiVillagerLag plugin) {
        return getData(v, plugin, AI_COOLDOWN_KEY);
    }

    public static long getLevelCooldown(Villager v, AntiVillagerLag plugin) {
        return getData(v, plugin, LEVEL_COOLDOWN_KEY);
    }

    public static long getLastRestock(Villager v, AntiVillagerLag plugin) {
        return getData(v, plugin, LAST_RESTOCK_KEY);
    }

    public static void initialize(Villager villager, AntiVillagerLag plugin) {
        if (hasMarker(villager, plugin)) {
            return;
        }
        setAiCooldown(villager, plugin, 0L);
        setLevelCooldown(villager, plugin, 0L);
        setLastRestock(villager, plugin);
        setMarker(villager, plugin, true);
    }

    public static void CleanseTheVillagers(Villager v, AntiVillagerLag plugin) {
        if (!hasMarker(v, plugin)) return;
        v.setAware(true);
        removeMarker(v, plugin);
        removeData(v, plugin, AI_COOLDOWN_KEY);
        removeData(v, plugin, LEVEL_COOLDOWN_KEY);
        removeData(v, plugin, LAST_RESTOCK_KEY);
        MIRRORS.remove(v.getUniqueId());
    }

    public static void restock(Villager v) {
        List<MerchantRecipe> recipes = v.getRecipes();
        for (MerchantRecipe r : recipes) {
            r.setUses(0);
        }
    }
}
