package me.basiqueevangelist.scaldinghot.mixin.client;

import net.minecraft.client.renderer.texture.SpriteContents;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(TextureAtlasSprite.class)
public interface SpriteAccessor {
    @Mutable
    @Accessor
    void setContents(SpriteContents contents);
}
