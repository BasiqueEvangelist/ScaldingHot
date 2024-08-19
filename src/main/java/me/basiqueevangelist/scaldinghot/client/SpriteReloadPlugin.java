package me.basiqueevangelist.scaldinghot.client;

import me.basiqueevangelist.scaldinghot.HotReloadPlugin;
import me.basiqueevangelist.scaldinghot.ScaldingHot;
import me.basiqueevangelist.scaldinghot.api.HotReloadBatch;
import me.basiqueevangelist.scaldinghot.mixin.client.SpriteAccessor;
import me.basiqueevangelist.scaldinghot.mixin.client.SpriteAtlasTextureAccessor;
import me.basiqueevangelist.scaldinghot.mixin.client.TextureManagerAccessor;
import me.basiqueevangelist.scaldinghot.pond.SpriteContentsAccess;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.texture.SpriteAtlasTexture;
import net.minecraft.client.texture.SpriteLoader;
import net.minecraft.client.texture.SpriteOpener;

import java.io.FileNotFoundException;
import java.io.IOException;

public class SpriteReloadPlugin implements HotReloadPlugin {
    @Override
    public void onHotReload(HotReloadBatch batch) {
        var client = MinecraftClient.getInstance();
        var textures = client.getTextureManager();
        var opener = SpriteOpener.create(SpriteLoader.METADATA_READERS);

        for (var entry : ((TextureManagerAccessor) textures).getTextures().entrySet()) {
            if (!(entry.getValue() instanceof SpriteAtlasTexture atlas)) continue;

            for (var spriteEntry : ((SpriteAtlasTextureAccessor) atlas).getSprites().entrySet()) {
                var contents = spriteEntry.getValue().getContents();
                var originalId = ((SpriteContentsAccess) contents).scaldinghot$originalId();
                if (originalId == null) continue;
                if (!batch.changedResources().contains(originalId)) continue;

                try {
                    var newSprite = opener.loadSprite(spriteEntry.getKey(), batch.resourceManager().getResourceOrThrow(originalId));

                    if (newSprite == null) continue;
                    if (newSprite.getHeight() != contents.getHeight() || newSprite.getWidth() != contents.getHeight()) continue;

                    ((SpriteAccessor) spriteEntry.getValue()).setContents(newSprite);

                    atlas.bindTexture();
                    spriteEntry.getValue().upload();
                } catch (IOException e) {
                    ScaldingHot.LOGGER.error("Couldn't hot reload sprite {} of {}", spriteEntry.getKey(), atlas.getId(), e);
                    throw new RuntimeException(e);
                }
            }
        }
    }
}
