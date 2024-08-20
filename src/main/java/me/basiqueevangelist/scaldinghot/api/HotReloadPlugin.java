package me.basiqueevangelist.scaldinghot.api;

import net.minecraft.resource.ResourceReloader;
import net.minecraft.resource.ResourceType;

/**
 * An extension that plugs into the hot reload mechanism.
 * <p>
 * If a {@link ResourceReloader} implements this class, it will be automatically discovered, while freestanding plugins
 * need to be manually registered.
 * 
 * @see ScaldingApi#addPlugin(ResourceType, HotReloadPlugin) 
 */
public interface HotReloadPlugin {
    /**
     * Invoked when hot reloading occurs.
     *
     * @param batch the current hot reload batch
     */
    void onHotReload(HotReloadBatch batch);
}
