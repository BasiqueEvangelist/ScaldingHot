package me.basiqueevangelist.scaldinghot.impl;

import me.basiqueevangelist.scaldinghot.api.HotReloadPlugin;
import net.minecraft.resource.ResourceReloader;
import net.minecraft.resource.ResourceType;

import java.util.*;

public class ScaldingRegistry {
    private static final boolean FULL_AUTO_HOT_RELOADING = Boolean.getBoolean("scaldinghot.fullAutomaticHotReloading");

    private static final Set<Class<? extends ResourceReloader>> AUTOMATIC_RELOADERS = new HashSet<>();
    private static final Map<ResourceType, List<HotReloadPlugin>> PLUGINS = new EnumMap<>(ResourceType.class);

    public static <T extends ResourceReloader> void enableAutomaticHotReloading(Class<T> reloaderClass) {
        AUTOMATIC_RELOADERS.add(reloaderClass);
    }

    public static void addPlugin(ResourceType type, HotReloadPlugin plugin) {
        PLUGINS.computeIfAbsent(type, unused -> new ArrayList<>()).add(plugin);
    }

    public static boolean isHotReloadable(ResourceReloader reloader) {
        if (FULL_AUTO_HOT_RELOADING) return true;
        if (reloader instanceof HotReloadPlugin) return true;

        return AUTOMATIC_RELOADERS.contains(reloader.getClass());
    }

    public static List<HotReloadPlugin> listPlugins(ResourceType type) {
        return PLUGINS.get(type);
    }
}
