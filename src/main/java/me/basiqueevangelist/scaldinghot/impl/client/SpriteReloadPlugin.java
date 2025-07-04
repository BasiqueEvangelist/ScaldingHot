package me.basiqueevangelist.scaldinghot.impl.client;

import me.basiqueevangelist.scaldinghot.api.HotReloadPlugin;
import me.basiqueevangelist.scaldinghot.impl.ScaldingHot;
import me.basiqueevangelist.scaldinghot.api.HotReloadBatch;
import me.basiqueevangelist.scaldinghot.mixin.client.SpriteAccessor;
import me.basiqueevangelist.scaldinghot.mixin.client.SpriteAtlasTextureAccessor;
import me.basiqueevangelist.scaldinghot.mixin.client.TextureManagerAccessor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.SpriteContents;
import net.minecraft.client.renderer.texture.SpriteLoader;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.renderer.texture.atlas.SpriteResourceLoader;
import net.minecraft.resources.ResourceLocation;
import me.basiqueevangelist.scaldinghot.impl.pond.SpriteContentsAccess;
import java.io.IOException;
import java.util.Map.Entry;

public class SpriteReloadPlugin implements HotReloadPlugin {
    @Override
    public void onHotReload(HotReloadBatch batch) {
        var client = Minecraft.getInstance();
        var textures = client.getTextureManager();
        var opener = SpriteResourceLoader.create(SpriteLoader.DEFAULT_METADATA_SECTIONS);

        for (var entry : ((TextureManagerAccessor) textures).getTextures().entrySet()) {
            if (!(entry.getValue() instanceof TextureAtlas atlas)) continue;

            for (var spriteEntry : ((SpriteAtlasTextureAccessor) atlas).getSprites().entrySet()) {
                var contents = spriteEntry.getValue().contents();
                var originalId = ((SpriteContentsAccess) contents).scaldinghot$originalId();
                if (originalId == null) continue;
                if (!batch.changedResources().contains(originalId)) continue;

                try {
                    var newSprite = opener.loadSprite(spriteEntry.getKey(), batch.resourceManager().getResourceOrThrow(originalId));

                    if (newSprite == null) continue;
                    if (newSprite.height() != contents.height() || newSprite.width() != contents.height()) continue;

                    ((SpriteAccessor) spriteEntry.getValue()).setContents(newSprite);

                    atlas.bind();
                    spriteEntry.getValue().uploadFirstFrame();
                } catch (IOException e) {
                    ScaldingHot.LOGGER.error("Couldn't hot reload sprite {} of {}", spriteEntry.getKey(), atlas.location(), e);
                }
            }
        }
    }
}
