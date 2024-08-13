package me.basiqueevangelist.scaldinghot;

import me.basiqueevangelist.scaldinghot.api.HotReloadBatch;

public interface ScaldingResourceReloader {
    void onHotReload(HotReloadBatch batch);
}
