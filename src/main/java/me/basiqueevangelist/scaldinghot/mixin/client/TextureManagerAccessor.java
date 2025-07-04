package me.basiqueevangelist.scaldinghot.mixin.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Map;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.resources.ResourceLocation;

@Mixin(TextureManager.class)
public interface TextureManagerAccessor {
    @Accessor
    Map<ResourceLocation, AbstractTexture> getTextures();
}
