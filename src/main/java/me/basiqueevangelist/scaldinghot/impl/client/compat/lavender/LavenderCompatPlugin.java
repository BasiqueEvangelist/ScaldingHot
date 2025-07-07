package me.basiqueevangelist.scaldinghot.impl.client.compat.lavender;

import io.wispforest.lavender.book.BookContentLoader;
import io.wispforest.lavender.book.BookLoader;
import io.wispforest.lavender.client.LavenderBookScreen;
import me.basiqueevangelist.scaldinghot.api.HotReloadBatch;
import me.basiqueevangelist.scaldinghot.api.HotReloadPlugin;
import me.basiqueevangelist.scaldinghot.api.ScaldingApi;
import net.minecraft.client.Minecraft;
import net.minecraft.server.packs.PackType;

public class LavenderCompatPlugin implements HotReloadPlugin {
    public static void init() {
        ScaldingApi.addPlugin(PackType.CLIENT_RESOURCES, new LavenderCompatPlugin());
    }

    @Override
    public void onHotReload(HotReloadBatch batch) {
        if (Minecraft.getInstance().level == null) return;

        BookLoader.reload(batch.resourceManager());
        BookContentLoader.reloadContents(batch.resourceManager());

        batch.queueFinishTask(() -> {
            var screen = Minecraft.getInstance().screen;

            if (screen instanceof LavenderBookScreen bookScreen) {
                var newBook = BookLoader.get(bookScreen.book.id());

                if (newBook != null) {
                    Minecraft.getInstance().setScreen(new LavenderBookScreen(newBook));
                } else {
                    Minecraft.getInstance().setScreen(null);
                }
            }
        });
    }
}
