package rebelmythik.antiVillagerLag.utils;

/**
 * Publishes an immutable settings object. Readers keep the instance they
 * already loaded while a reload stores the next one.
 */
public final class SettingsStore {

    private volatile PluginSettings current;

    public SettingsStore(PluginSettings initial) {
        this.current = initial;
    }

    public PluginSettings get() {
        return current;
    }

    public void publish(PluginSettings next) {
        this.current = next;
    }
}
