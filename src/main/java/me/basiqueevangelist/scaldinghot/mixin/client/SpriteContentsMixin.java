package me.basiqueevangelist.scaldinghot.mixin.client;

import me.basiqueevangelist.scaldinghot.impl.pond.SpriteContentsAccess;
import net.minecraft.client.texture.SpriteContents;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(SpriteContents.class)
public class SpriteContentsMixin implements SpriteContentsAccess {
    @Unique private Identifier scaldinghot$originalId = null;

    @Override
    public @Nullable Identifier scaldinghot$originalId() {
        return scaldinghot$originalId;
    }

    @Override
    public void scaldinghot$setOriginalId(Identifier originalId) {
        scaldinghot$originalId = originalId;
    }
}
