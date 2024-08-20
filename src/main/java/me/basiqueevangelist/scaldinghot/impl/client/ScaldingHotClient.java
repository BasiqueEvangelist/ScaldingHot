package me.basiqueevangelist.scaldinghot.impl.client;

import me.basiqueevangelist.scaldinghot.api.ScaldingApi;
import me.basiqueevangelist.scaldinghot.impl.instrument.ResourceWatcher;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.resource.language.LanguageManager;
import net.minecraft.client.texture.TextureManager;
import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.ResourceType;

import java.util.concurrent.Executor;

public class ScaldingHotClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        ClientCommandRegistrationCallback.EVENT.register(AddPathCommand::register);

        ResourceWatcher.CLIENT_RESOURCES.init();

        ScaldingApi.enableAutomaticHotReloading(LanguageManager.class);
        ScaldingApi.enableAutomaticHotReloading(TextureManager.class);

        ScaldingApi.addPlugin(ResourceType.CLIENT_RESOURCES, new SpriteReloadPlugin());
    }

    public static Executor getClientExecutor() {
        return MinecraftClient.getInstance();
    }

    public static ResourceManager getClientResourceManager() {
        return MinecraftClient.getInstance().getResourceManager();
    }
}
