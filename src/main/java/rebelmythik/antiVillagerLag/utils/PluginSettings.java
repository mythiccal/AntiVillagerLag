package rebelmythik.antiVillagerLag.utils;

import org.bukkit.Material;
import org.bukkit.configuration.Configuration;

import java.util.ArrayList;
import java.util.List;

public final class PluginSettings {

    private final List<String> disablingNames;
    private final List<Material> standingBlocks;
    private final List<Material> workstationBlocks;
    private final List<Long> restockTimes;
    private final boolean preventTrading;
    private final boolean useNametags;
    private final boolean useRenaming;
    private final boolean useBlocks;
    private final boolean useWorkstations;
    private final int workstationCheckRadius;
    private final boolean silenceVillagers;
    private final long aiToggleCooldown;
    private final int radiusLimit;
    private final int radiusDefault;
    private final String noPermission;
    private final String reloadMessage;
    private final String cooldownAiMessage;
    private final String cooldownLevelupMessage;
    private final String nextRestock;
    private final String radiusLimitMessage;
    private final String mustBeDisabled;
    private final String searchingRadius;
    private final String radiusInvalid;

    private PluginSettings(
            List<String> disablingNames,
            List<Material> standingBlocks,
            List<Material> workstationBlocks,
            List<Long> restockTimes,
            boolean preventTrading,
            boolean useNametags,
            boolean useRenaming,
            boolean useBlocks,
            boolean useWorkstations,
            int workstationCheckRadius,
            boolean silenceVillagers,
            long aiToggleCooldown,
            int radiusLimit,
            int radiusDefault,
            String noPermission,
            String reloadMessage,
            String cooldownAiMessage,
            String cooldownLevelupMessage,
            String nextRestock,
            String radiusLimitMessage,
            String mustBeDisabled,
            String searchingRadius,
            String radiusInvalid
    ) {
        this.disablingNames = List.copyOf(disablingNames);
        this.standingBlocks = List.copyOf(standingBlocks);
        this.workstationBlocks = List.copyOf(workstationBlocks);
        this.restockTimes = List.copyOf(restockTimes);
        this.preventTrading = preventTrading;
        this.useNametags = useNametags;
        this.useRenaming = useRenaming;
        this.useBlocks = useBlocks;
        this.useWorkstations = useWorkstations;
        this.workstationCheckRadius = workstationCheckRadius;
        this.silenceVillagers = silenceVillagers;
        this.aiToggleCooldown = aiToggleCooldown;
        this.radiusLimit = radiusLimit;
        this.radiusDefault = radiusDefault;
        this.noPermission = noPermission;
        this.reloadMessage = reloadMessage;
        this.cooldownAiMessage = cooldownAiMessage;
        this.cooldownLevelupMessage = cooldownLevelupMessage;
        this.nextRestock = nextRestock;
        this.radiusLimitMessage = radiusLimitMessage;
        this.mustBeDisabled = mustBeDisabled;
        this.searchingRadius = searchingRadius;
        this.radiusInvalid = radiusInvalid;
    }

    public static PluginSettings empty() {
        return new PluginSettings(
                List.of(), List.of(), List.of(), List.of(),
                false, false, false, false, false,
                0, false, 0L, 0, 0,
                null, null, null, null, null, null, null, null, null
        );
    }

    /**
     * Names, standing blocks, and workstation blocks are replaced only when their
     * toggle is on. Turning a toggle off leaves the previous list in place.
     */
    public PluginSettings merge(Configuration config) {
        List<String> names = disablingNames;
        if (config.getBoolean("toggleableoptions.userenaming")) {
            names = new ArrayList<>();
            for (String name : config.getStringList("NamesThatDisable")) {
                names.add(name.toLowerCase());
            }
        }

        List<Material> standing = standingBlocks;
        if (config.getBoolean("toggleableoptions.useblocks")) {
            standing = materials(config.getStringList("BlocksThatDisable"));
        }

        List<Material> stations = workstationBlocks;
        if (config.getBoolean("toggleableoptions.useworkstations")) {
            stations = materials(config.getStringList("WorkstationsThatDisable"));
        }

        return new PluginSettings(
                names,
                standing,
                stations,
                new ArrayList<>(config.getLongList("RestockTimes.times")),
                config.getBoolean("toggleableoptions.preventtrading"),
                config.getBoolean("toggleableoptions.usenametags"),
                config.getBoolean("toggleableoptions.userenaming"),
                config.getBoolean("toggleableoptions.useblocks"),
                config.getBoolean("toggleableoptions.useworkstations"),
                config.getInt("toggleableoptions.workstationcheckradius"),
                config.getBoolean("toggleableoptions.silence_villagers"),
                config.getLong("ai-toggle-cooldown"),
                config.getInt("RadiusLimit"),
                config.getInt("RadiusDefault"),
                config.getString("messages.no-permission"),
                config.getString("messages.reload-message"),
                config.getString("messages.cooldown-ai-message"),
                config.getString("messages.cooldown-levelup-message"),
                config.getString("messages.next-restock"),
                config.getString("messages.radius-limit"),
                config.getString("messages.VillagerMustBeDisabled"),
                config.getString("messages.searching-radius"),
                config.getString("messages.radius-invalid")
        );
    }

    private static List<Material> materials(List<String> names) {
        List<Material> materials = new ArrayList<>();
        for (String blockName : names) {
            Material block = Material.getMaterial(blockName.toUpperCase());
            if (block != null) {
                materials.add(block);
            }
        }
        return materials;
    }

    public List<String> disablingNames() {
        return disablingNames;
    }

    public List<Material> standingBlocks() {
        return standingBlocks;
    }

    public List<Material> workstationBlocks() {
        return workstationBlocks;
    }

    public List<Long> restockTimes() {
        return restockTimes;
    }

    public boolean preventTrading() {
        return preventTrading;
    }

    public boolean useNametags() {
        return useNametags;
    }

    public boolean useRenaming() {
        return useRenaming;
    }

    public boolean useBlocks() {
        return useBlocks;
    }

    public boolean useWorkstations() {
        return useWorkstations;
    }

    public int workstationCheckRadius() {
        return workstationCheckRadius;
    }

    public boolean silenceVillagers() {
        return silenceVillagers;
    }

    public long aiToggleCooldown() {
        return aiToggleCooldown;
    }

    public int radiusLimit() {
        return radiusLimit;
    }

    public int radiusDefault() {
        return radiusDefault;
    }

    public String noPermission() {
        return noPermission;
    }

    public String reloadMessage() {
        return reloadMessage;
    }

    public String cooldownAiMessage() {
        return cooldownAiMessage;
    }

    public String cooldownLevelupMessage() {
        return cooldownLevelupMessage;
    }

    public String nextRestock() {
        return nextRestock;
    }

    public String radiusLimitMessage() {
        return radiusLimitMessage;
    }

    public String mustBeDisabled() {
        return mustBeDisabled;
    }

    public String searchingRadius() {
        return searchingRadius;
    }

    public String radiusInvalid() {
        return radiusInvalid;
    }
}
