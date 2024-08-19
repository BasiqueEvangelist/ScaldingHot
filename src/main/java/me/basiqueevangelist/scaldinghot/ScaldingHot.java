package me.basiqueevangelist.scaldinghot;

import me.basiqueevangelist.scaldinghot.config.ConfigManager;
import me.basiqueevangelist.scaldinghot.instrument.ResourceWatcher;
import net.fabricmc.api.ModInitializer;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.recipe.RecipeManager;
import net.minecraft.resource.ResourceReloader;
import net.minecraft.resource.ResourceType;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.ServerAdvancementLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class ScaldingHot implements ModInitializer {
    public static final Logger LOGGER = LoggerFactory.getLogger("Scalding Hot!");
	public static final ConfigManager CONFIG = new ConfigManager();
	public static MinecraftServer SERVER;

	private static final Set<Class<? extends ResourceReloader>> HOT_RELOADERS = new HashSet<>();
	private static final Map<ResourceType, List<HotReloadPlugin>> PLUGINS = new EnumMap<>(ResourceType.class);

	private static final boolean INSTRUMENT_EVERYTHING = false;

	@Override
	public void onInitialize() {
		ResourceWatcher.SERVER_DATA.init();

		markAsHotReloadable(RecipeManager.class);
		markAsHotReloadable(ServerAdvancementLoader.class);

		ServerLifecycleEvents.SERVER_STARTING.register(server -> SERVER = server);
		ServerLifecycleEvents.SERVER_STOPPED.register(server -> SERVER = null);
	}

	public static void markAsHotReloadable(Class<? extends ResourceReloader> klass) {
		HOT_RELOADERS.add(klass);
	}

	public static void addHotReloadPlugin(ResourceType type, HotReloadPlugin plugin) {
		PLUGINS.computeIfAbsent(type, ignored -> new ArrayList<>()).add(plugin);
	}

	public static boolean isHotReloadable(ResourceReloader reloader) {
		if (INSTRUMENT_EVERYTHING) return true;

        return HOT_RELOADERS.contains(reloader.getClass());
	}

	public static List<HotReloadPlugin> listPlugins(ResourceType type) {
		return PLUGINS.get(type);
	}
}