package me.basiqueevangelist.scaldinghot.mixin.client;

import net.minecraft.client.texture.Sprite;
import net.minecraft.client.texture.SpriteContents;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(Sprite.class)
public interface SpriteAccessor {
    @Mutable
    @Accessor
    void setContents(SpriteContents contents);
}
