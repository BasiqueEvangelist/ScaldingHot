package me.basiqueevangelist.scaldinghot.impl.client.compat.owo;

import io.wispforest.owo.ui.parsing.UIModelLoader;
import me.basiqueevangelist.scaldinghot.api.HotReloadBatch;
import me.basiqueevangelist.scaldinghot.api.HotReloadPlugin;
import me.basiqueevangelist.scaldinghot.impl.pond.OwoModelScreenAccess;
import me.basiqueevangelist.scaldinghot.mixin.client.ScreenAccessor;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;

public class UiModelReloadPlugin implements HotReloadPlugin {
    @Override
    public void onHotReload(HotReloadBatch batch) {
        batch.queueFinishTask(() -> {
            var client = Minecraft.getInstance();

            if (client.screen instanceof OwoModelScreenAccess access) {
                Identifier modelId = access.scaldinghot$modelId();
                if (modelId == null) return;

                Identifier resourceId = Identifier.fromNamespaceAndPath(modelId.getNamespace(), "owo_ui/" + modelId.getPath() + ".xml");

                if (!batch.changedResources().contains(resourceId)) return;

                access.scaldinghot$clearAdapter();
                access.scaldinghot$setModel(UIModelLoader.get(modelId));
                ((ScreenAccessor) client.screen).callRebuildWidgets();
            }
        });
    }
}
