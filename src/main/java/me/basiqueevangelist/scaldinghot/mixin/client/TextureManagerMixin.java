package me.basiqueevangelist.scaldinghot.mixin.client;

import me.basiqueevangelist.scaldinghot.api.HotReloadBatch;
import me.basiqueevangelist.scaldinghot.api.HotReloadPlugin;
import me.basiqueevangelist.scaldinghot.impl.ScaldingHot;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.client.renderer.texture.ReloadableTexture;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.resources.Identifier;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.io.IOException;
import java.util.Map;

@Mixin(TextureManager.class)
public class TextureManagerMixin implements HotReloadPlugin {
    @Shadow @Final private Map<Identifier, AbstractTexture> byPath;

    @Override
    public void onHotReload(HotReloadBatch batch) {
        for (var id : batch.changedResources()) {
            AbstractTexture texture = byPath.get(id);
            if (texture == null) continue;

            if (texture instanceof ReloadableTexture reloadable) {
                try {
                    reloadable.loadContents(batch.resourceManager());
                } catch (IOException e) {
                    ScaldingHot.LOGGER.error("Couldn't hot reload texture {}", id, e);
                }
            }
        }
    }
}
