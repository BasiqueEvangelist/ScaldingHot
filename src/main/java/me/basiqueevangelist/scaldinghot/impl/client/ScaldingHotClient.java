package me.basiqueevangelist.scaldinghot.impl.client;

import me.basiqueevangelist.scaldinghot.api.ScaldingApi;
import me.basiqueevangelist.scaldinghot.impl.client.compat.lavender.LavenderCompatPlugin;
import me.basiqueevangelist.scaldinghot.impl.client.compat.owo.OwoCompat;
import me.basiqueevangelist.scaldinghot.impl.instrument.ResourceWatcher;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.font.FontManager;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.resources.language.LanguageManager;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.ResourceManager;

import java.util.concurrent.Executor;

public class ScaldingHotClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        if (FabricLoader.getInstance().isDevelopmentEnvironment())
            ClientCommandRegistrationCallback.EVENT.register(AddPathCommand::register);

        ClientCommandRegistrationCallback.EVENT.register(ReloadConfigCommand::register);

        ResourceWatcher.CLIENT_RESOURCES.init();

        ScaldingApi.enableAutomaticHotReloading(LanguageManager.class);
        ScaldingApi.enableAutomaticHotReloading(TextureManager.class);
        ScaldingApi.enableAutomaticHotReloading(SoundManager.class);
        ScaldingApi.enableAutomaticHotReloading(FontManager.class);

        ScaldingApi.addPlugin(PackType.CLIENT_RESOURCES, new SpriteReloadPlugin());
        ScaldingApi.addPlugin(PackType.CLIENT_RESOURCES, new ModelReloadPlugin());

        if (FabricLoader.getInstance().isModLoaded("owo"))
            OwoCompat.init();

/*        if (FabricLoader.getInstance().isModLoaded("lavender"))
            LavenderCompatPlugin.init();*/
    }

    public static Executor getClientExecutor() {
        return Minecraft.getInstance();
    }

    public static ResourceManager getClientResourceManager() {
        return Minecraft.getInstance().getResourceManager();
    }
}
