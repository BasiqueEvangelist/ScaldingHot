package me.basiqueevangelist.scaldinghot.client;

import me.basiqueevangelist.scaldinghot.ScaldingHot;
import me.basiqueevangelist.scaldinghot.instrument.ResourceWatcher;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.resource.language.LanguageManager;
import net.minecraft.client.texture.TextureManager;
import net.minecraft.resource.ResourceManager;

import java.util.concurrent.Executor;

public class ScaldingHotClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        ClientCommandRegistrationCallback.EVENT.register(AddPathCommand::register);

        ResourceWatcher.CLIENT_RESOURCES.init();

        ScaldingHot.markAsHotReloadable(LanguageManager.class);
        ScaldingHot.markAsHotReloadable(TextureManager.class);
    }

    public static Executor getClientExecutor() {
        return MinecraftClient.getInstance();
    }

    public static ResourceManager getClientResourceManager() {
        return MinecraftClient.getInstance().getResourceManager();
    }
}
