package me.basiqueevangelist.scaldinghot.mixin.client.owo;

import com.moulberry.mixinconstraints.annotations.IfModLoaded;
import io.wispforest.owo.ui.base.BaseOwoContainerScreen;
import io.wispforest.owo.ui.base.BaseUIModelContainerScreen;
import io.wispforest.owo.ui.core.ParentUIComponent;
import io.wispforest.owo.ui.parsing.UIModel;
import me.basiqueevangelist.scaldinghot.impl.pond.OwoModelScreenAccess;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;

@IfModLoaded("owo")
@Mixin(BaseUIModelContainerScreen.class)
public abstract class BaseUIModelContainerScreenMixin<R extends ParentUIComponent, S extends AbstractContainerMenu> extends BaseOwoContainerScreen<R, S> implements OwoModelScreenAccess {
    @Shadow @Final @Nullable protected Identifier modelId;

    @Mutable
    @Shadow @Final protected UIModel model;

    protected BaseUIModelContainerScreenMixin(S handler, Inventory inventory, Component title) {
        super(handler, inventory, title);
    }

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
