package me.basiqueevangelist.scaldinghot.impl.pond;

import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

public interface ResourceAccess {
    @Nullable Identifier scaldinghot$id();

    void scaldinghot$setId(Identifier id);
}
