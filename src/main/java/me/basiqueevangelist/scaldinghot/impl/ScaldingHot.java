package me.basiqueevangelist.scaldinghot.impl;

import me.basiqueevangelist.scaldinghot.api.ScaldingApi;
import me.basiqueevangelist.scaldinghot.impl.config.ConfigManager;
import me.basiqueevangelist.scaldinghot.impl.instrument.ResourceWatcher;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.ServerAdvancementManager;
import net.minecraft.server.ServerFunctionLibrary;
import net.minecraft.server.packs.PackType;
import net.minecraft.world.item.crafting.RecipeManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ScaldingHot implements ModInitializer {
    public static final Logger LOGGER = LoggerFactory.getLogger("Scalding Hot!");
	public static final ConfigManager CONFIG = new ConfigManager();
	public static MinecraftServer SERVER;

	@Override
	public void onInitialize() {
		ResourceWatcher.SERVER_DATA.init();

		ScaldingApi.enableAutomaticHotReloading(RecipeManager.class);
		ScaldingApi.enableAutomaticHotReloading(ServerAdvancementManager.class);
		ScaldingApi.enableAutomaticHotReloading(ServerFunctionLibrary.class);

		ScaldingApi.addPlugin(PackType.SERVER_DATA, new ServerReloadPlugin());

		ServerLifecycleEvents.SERVER_STARTING.register(server -> SERVER = server);
		ServerLifecycleEvents.SERVER_STOPPED.register(server -> SERVER = null);
	}
}