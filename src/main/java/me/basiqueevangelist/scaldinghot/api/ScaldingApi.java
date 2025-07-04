package me.basiqueevangelist.scaldinghot.api;

import me.basiqueevangelist.scaldinghot.impl.ScaldingRegistry;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.PreparableReloadListener;

public final class ScaldingApi {
    private ScaldingApi() { }

    /**
     * Enables fully automatic hot reloading for all reloaders of this class. All resources this reloader uses will be
     * tracked, and if any of them changes, it will be invoked.
     * @param reloaderClass the class to enable hot reloading for
     */
    public static <T extends PreparableReloadListener> void enableAutomaticHotReloading(Class<T> reloaderClass) {
        ScaldingRegistry.enableAutomaticHotReloading(reloaderClass);
    }

    /**
     * Registers a freestanding hot reload plugin for this resource type.
     *
     * @param type the pack type to register for
     * @param plugin the plugin to register
     */
    public static void addPlugin(PackType type, HotReloadPlugin plugin) {
        ScaldingRegistry.addPlugin(type, plugin);
    }
}
