package me.basiqueevangelist.scaldinghot.mixin.client;

import com.mojang.blaze3d.platform.NativeImage;
import me.basiqueevangelist.scaldinghot.impl.pond.SpriteContentsAccess;
import net.minecraft.client.renderer.texture.SpriteContents;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

@Mixin(SpriteContents.class)
public class SpriteContentsMixin implements SpriteContentsAccess {
    @Shadow NativeImage[] byMipLevel;
    @Unique private Identifier scaldinghot$originalId = null;

    @Override
    public @Nullable Identifier scaldinghot$originalId() {
        return scaldinghot$originalId;
    }

    @Override
    public void scaldinghot$setOriginalId(Identifier originalId) {
        scaldinghot$originalId = originalId;
    }

    @Override
    public int scaldinghot$getMipLevel() {
        return byMipLevel.length - 1;
    }
}
