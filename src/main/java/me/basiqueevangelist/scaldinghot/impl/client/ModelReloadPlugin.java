package me.basiqueevangelist.scaldinghot.impl.client;

import me.basiqueevangelist.scaldinghot.api.HotReloadBatch;
import me.basiqueevangelist.scaldinghot.api.HotReloadPlugin;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.resources.model.AtlasManager;
import net.minecraft.client.resources.model.ModelManager;

public class ModelReloadPlugin implements HotReloadPlugin {
    @Override
    public void onHotReload(HotReloadBatch batch) {
        if (!wasFolderChanged("models/", batch) && !wasFolderChanged("textures/item/", batch)) return;

        batch.markNeedsReload(AtlasManager.class); // TODO: don't remake atlases unless it's absolutely necessary
        batch.markNeedsReload(ModelManager.class);

        batch.queueFinishTask(() -> {
            Minecraft.getInstance().levelRenderer.allChanged();
        });
    }

    private boolean wasFolderChanged(String folder, HotReloadBatch batch) {
        for (var id : batch.changedResources()) {
            if (id.getPath().startsWith(folder))
                return true;
        }

        return false;
    }
}
