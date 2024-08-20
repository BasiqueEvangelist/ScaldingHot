package me.basiqueevangelist.scaldinghot.impl.pond;

import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

public interface SpriteContentsAccess {
    @Nullable Identifier scaldinghot$originalId();

    void scaldinghot$setOriginalId(Identifier originalId);
}
