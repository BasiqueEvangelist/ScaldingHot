package me.basiqueevangelist.scaldinghot.impl.client.compat.owo;

import io.wispforest.owo.ui.parsing.UIModelLoader;
import me.basiqueevangelist.scaldinghot.api.ScaldingApi;
import net.minecraft.resource.ResourceType;

public class OwoCompat {
    public static void init() {
        ScaldingApi.enableAutomaticHotReloading(UIModelLoader.class);
        ScaldingApi.addPlugin(ResourceType.CLIENT_RESOURCES, new UiModelReloadPlugin());
    }
}
