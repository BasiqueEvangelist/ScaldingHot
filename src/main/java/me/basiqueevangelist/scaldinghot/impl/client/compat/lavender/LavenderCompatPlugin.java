package me.basiqueevangelist.scaldinghot.impl.client.compat.lavender;

import io.wispforest.lavender.book.BookContentLoader;
import io.wispforest.lavender.book.BookLoader;
import io.wispforest.lavender.client.LavenderBookScreen;
import me.basiqueevangelist.scaldinghot.api.HotReloadBatch;
import me.basiqueevangelist.scaldinghot.api.HotReloadPlugin;
import me.basiqueevangelist.scaldinghot.api.ScaldingApi;
import net.minecraft.client.MinecraftClient;
import net.minecraft.resource.ResourceType;

public class LavenderCompatPlugin implements HotReloadPlugin {
    public static void init() {
        ScaldingApi.addPlugin(ResourceType.CLIENT_RESOURCES, new LavenderCompatPlugin());
    }

    @Override
    public void onHotReload(HotReloadBatch batch) {
        BookLoader.reload(batch.resourceManager());
        BookContentLoader.reloadContents(batch.resourceManager());

        batch.queueFinishTask(() -> {
            var screen = MinecraftClient.getInstance().currentScreen;

            if (screen instanceof LavenderBookScreen bookScreen) {
                var newBook = BookLoader.get(bookScreen.book.id());

                if (newBook != null) {
                    MinecraftClient.getInstance().setScreen(new LavenderBookScreen(newBook));
                } else {
                    MinecraftClient.getInstance().setScreen(null);
                }
            }
        });
    }
}
