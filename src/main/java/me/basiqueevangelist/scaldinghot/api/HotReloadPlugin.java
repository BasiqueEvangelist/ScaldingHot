package me.basiqueevangelist.scaldinghot.api;

import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.PreparableReloadListener;

/**
 * An extension that plugs into the hot reload mechanism.
 * <p>
 * If a {@link PreparableReloadListener} implements this class, it will be automatically discovered, while freestanding plugins
 * need to be manually registered.
 * 
 * @see ScaldingApi#addPlugin(PackType, HotReloadPlugin) 
 */
public interface HotReloadPlugin {
    /**
     * Invoked when hot reloading occurs.
     *
     * @param batch the current hot reload batch
     */
    void onHotReload(HotReloadBatch batch);
}
