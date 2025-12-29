package me.basiqueevangelist.scaldinghot.impl.pond;

import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.Nullable;

public interface SpriteContentsAccess {
    @Nullable Identifier scaldinghot$originalId();

    void scaldinghot$setOriginalId(Identifier originalId);

    int scaldinghot$getMipLevel();
}
