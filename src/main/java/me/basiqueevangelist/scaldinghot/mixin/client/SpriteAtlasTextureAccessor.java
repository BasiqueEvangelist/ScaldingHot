package me.basiqueevangelist.scaldinghot.mixin.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Map;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.ResourceLocation;

@Mixin(TextureAtlas.class)
public interface SpriteAtlasTextureAccessor {
    @Accessor
    Map<ResourceLocation, TextureAtlasSprite> getSprites();
}
