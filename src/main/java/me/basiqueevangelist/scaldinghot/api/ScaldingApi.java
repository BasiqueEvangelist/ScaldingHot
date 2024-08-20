package me.basiqueevangelist.scaldinghot.api;

import me.basiqueevangelist.scaldinghot.impl.ScaldingRegistry;
import net.minecraft.resource.ResourceReloader;
import net.minecraft.resource.ResourceType;

public final class ScaldingApi {
    private ScaldingApi() { }

    /**
     * Enables fully automatic hot reloading for all reloaders of this class. All resources this reloader uses will be
     * tracked, and if any of them changes, it will be invoked.
     * @param reloaderClass the class to enable hot reloading for
     */
    public static <T extends ResourceReloader> void enableAutomaticHotReloading(Class<T> reloaderClass) {
        ScaldingRegistry.enableAutomaticHotReloading(reloaderClass);
    }

    /**
     * Registers a freestanding hot reload plugin for this resource type.
     *
     * @param type the pack type to register for
     * @param plugin the plugin to register
     */
    public static void addPlugin(ResourceType type, HotReloadPlugin plugin) {
        ScaldingRegistry.addPlugin(type, plugin);
    }
}
