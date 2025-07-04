package me.basiqueevangelist.scaldinghot.impl.pond;

import io.wispforest.owo.ui.parsing.UIModel;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

public interface OwoModelScreenAccess {
    void scaldinghot$clearAdapter();

    @Nullable ResourceLocation scaldinghot$modelId();

    void scaldinghot$setModel(UIModel model);
}
