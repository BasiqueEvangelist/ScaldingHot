package me.basiqueevangelist.scaldinghot.mixin.client;

import me.basiqueevangelist.scaldinghot.impl.pond.SpriteContentsAccess;
import net.minecraft.client.renderer.texture.SpriteContents;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(SpriteContents.class)
public class SpriteContentsMixin implements SpriteContentsAccess {
    @Unique private ResourceLocation scaldinghot$originalId = null;

    @Override
    public @Nullable ResourceLocation scaldinghot$originalId() {
        return scaldinghot$originalId;
    }

    @Override
    public void scaldinghot$setOriginalId(ResourceLocation originalId) {
        scaldinghot$originalId = originalId;
    }
}
