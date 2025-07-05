package me.basiqueevangelist.scaldinghot.impl;

import me.basiqueevangelist.scaldinghot.api.HotReloadPlugin;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.PreparableReloadListener;

import java.util.*;

public class ScaldingRegistry {
    private static final boolean FULL_AUTO_HOT_RELOADING = Boolean.getBoolean("scaldinghot.fullAutomaticHotReloading");

    private static final Set<Class<? extends PreparableReloadListener>> AUTOMATIC_RELOADERS = new HashSet<>();
    private static final Map<PackType, List<HotReloadPlugin>> PLUGINS = new EnumMap<>(PackType.class);

    public static <T extends PreparableReloadListener> void enableAutomaticHotReloading(Class<T> reloaderClass) {
        AUTOMATIC_RELOADERS.add(reloaderClass);
    }

    public static void addPlugin(PackType type, HotReloadPlugin plugin) {
        PLUGINS.computeIfAbsent(type, unused -> new ArrayList<>()).add(plugin);
    }

    public static boolean isHotReloadable(PreparableReloadListener reloader) {
        if (FULL_AUTO_HOT_RELOADING) return true;
        if (reloader instanceof HotReloadPlugin) return true;

        return AUTOMATIC_RELOADERS.contains(reloader.getClass());
    }

    public static List<HotReloadPlugin> listPlugins(PackType type) {
        return PLUGINS.get(type);
    }
}
