package me.basiqueevangelist.scaldinghot;

import me.basiqueevangelist.scaldinghot.api.HotReloadBatch;

public interface HotReloadPlugin {
    void onHotReload(HotReloadBatch batch);
}
