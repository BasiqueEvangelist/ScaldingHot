package me.basiqueevangelist.scaldinghot.impl.client;

import me.basiqueevangelist.scaldinghot.api.HotReloadBatch;
import me.basiqueevangelist.scaldinghot.api.HotReloadPlugin;
import me.basiqueevangelist.scaldinghot.impl.ScaldingHot;
import me.basiqueevangelist.scaldinghot.impl.pond.SpriteContentsAccess;
import me.basiqueevangelist.scaldinghot.mixin.client.TextureAtlasAccessor;
import me.basiqueevangelist.scaldinghot.mixin.client.TextureAtlasSpriteAccessor;
import me.basiqueevangelist.scaldinghot.mixin.client.TextureManagerAccessor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.SpriteLoader;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.atlas.SpriteResourceLoader;

import java.io.IOException;
import java.util.Set;

public class SpriteReloadPlugin implements HotReloadPlugin {
    @Override
    public void onHotReload(HotReloadBatch batch) {
        var client = Minecraft.getInstance();
        var textures = client.getTextureManager();
        var opener = SpriteResourceLoader.create(Set.of() /* TODO: figure out what to put here */);

        for (var entry : ((TextureManagerAccessor) textures).getByPath().entrySet()) {
            if (!(entry.getValue() instanceof TextureAtlas atlas)) continue;

            for (var spriteEntry : ((TextureAtlasAccessor) atlas).getTexturesByName().entrySet()) {
                var contents = spriteEntry.getValue().contents();
                var originalId = ((SpriteContentsAccess) contents).scaldinghot$originalId();
                if (originalId == null) continue;
                if (!batch.changedResources().contains(originalId)) continue;

                try {
                    var newSprite = opener.loadSprite(spriteEntry.getKey(), batch.resourceManager().getResourceOrThrow(originalId));

                    if (newSprite == null) continue;
                    if (newSprite.height() != contents.height() || newSprite.width() != contents.height()) continue;

                    newSprite.increaseMipLevel(((SpriteContentsAccess) contents).scaldinghot$getMipLevel());

                    ((TextureAtlasSpriteAccessor) spriteEntry.getValue()).setContents(newSprite);

                    spriteEntry.getValue().uploadFirstFrame(atlas.getTexture());
                } catch (RuntimeException | IOException e) {
                    ScaldingHot.LOGGER.error("Couldn't hot reload sprite {} of {}", spriteEntry.getKey(), atlas.location(), e);
                }
            }
        }
    }
}
