package me.basiqueevangelist.scaldinghot.mixin.client;

import me.basiqueevangelist.scaldinghot.ScaldingHot;
import me.basiqueevangelist.scaldinghot.HotReloadPlugin;
import me.basiqueevangelist.scaldinghot.api.HotReloadBatch;
import net.minecraft.client.texture.AbstractTexture;
import net.minecraft.client.texture.TextureManager;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.io.IOException;
import java.util.Map;

@Mixin(TextureManager.class)
public class TextureManagerMixin implements HotReloadPlugin {
    @Shadow @Final private Map<Identifier, AbstractTexture> textures;

    @Override
    public void onHotReload(HotReloadBatch batch) {
        for (var id : batch.changedResources()) {
            AbstractTexture texture = textures.get(id);
            if (texture == null) continue;

            try {
                texture.load(batch.resourceManager());
            } catch (IOException e) {
                ScaldingHot.LOGGER.error("Couldn't hot reload texture {}", id, e);
            }
        }
    }
}
