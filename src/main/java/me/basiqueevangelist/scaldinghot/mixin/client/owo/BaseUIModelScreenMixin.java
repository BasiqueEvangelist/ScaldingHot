package me.basiqueevangelist.scaldinghot.mixin.client.owo;

import com.moulberry.mixinconstraints.annotations.IfModLoaded;
import io.wispforest.owo.ui.base.BaseOwoScreen;
import io.wispforest.owo.ui.base.BaseUIModelScreen;
import io.wispforest.owo.ui.core.ParentComponent;
import io.wispforest.owo.ui.parsing.UIModel;
import me.basiqueevangelist.scaldinghot.impl.pond.OwoModelScreenAccess;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;

@IfModLoaded("owo")
@Mixin(BaseUIModelScreen.class)
public abstract class BaseUIModelScreenMixin<R extends ParentComponent> extends BaseOwoScreen<R> implements OwoModelScreenAccess {
    @Shadow
    @Final
    @Nullable protected Identifier modelId;

    @Mutable
    @Shadow @Final protected UIModel model;

    @Override
    public void scaldinghot$clearAdapter() {
        if (this.uiAdapter != null) this.uiAdapter.dispose();
        this.uiAdapter = null;
    }

    @Override
    public @Nullable Identifier scaldinghot$modelId() {
        return this.modelId;
    }

    @Override
    public void scaldinghot$setModel(UIModel model) {
        this.model = model;
    }
}
