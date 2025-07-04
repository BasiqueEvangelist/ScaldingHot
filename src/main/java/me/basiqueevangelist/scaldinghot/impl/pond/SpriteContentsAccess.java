package me.basiqueevangelist.scaldinghot.impl.pond;

import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

public interface SpriteContentsAccess {
    @Nullable ResourceLocation scaldinghot$originalId();

    void scaldinghot$setOriginalId(ResourceLocation originalId);
}
