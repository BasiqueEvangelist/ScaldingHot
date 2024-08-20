package me.basiqueevangelist.scaldinghot.impl.pond;

import io.wispforest.owo.ui.parsing.UIModel;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

public interface OwoModelScreenAccess {
    void scaldinghot$clearAdapter();

    @Nullable Identifier scaldinghot$modelId();

    void scaldinghot$setModel(UIModel model);
}
