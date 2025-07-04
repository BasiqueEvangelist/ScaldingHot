package me.basiqueevangelist.scaldinghot.impl.client.compat.owo;

import io.wispforest.owo.ui.parsing.UIModelLoader;
import me.basiqueevangelist.scaldinghot.api.ScaldingApi;
import net.minecraft.server.packs.PackType;

public class OwoCompat {
    public static void init() {
        ScaldingApi.enableAutomaticHotReloading(UIModelLoader.class);
        ScaldingApi.addPlugin(PackType.CLIENT_RESOURCES, new UiModelReloadPlugin());
    }
}
