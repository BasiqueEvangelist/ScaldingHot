package me.basiqueevangelist.scaldinghot.mixin.client.owo;

import com.moulberry.mixinconstraints.annotations.IfModLoaded;
import io.wispforest.owo.ui.base.BaseOwoHandledScreen;
import io.wispforest.owo.ui.base.BaseUIModelHandledScreen;
import io.wispforest.owo.ui.core.ParentComponent;
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
@Mixin(BaseUIModelHandledScreen.class)
public abstract class BaseUIModelHandledScreenMixin<R extends ParentComponent, S extends AbstractContainerMenu> extends BaseOwoHandledScreen<R, S> implements OwoModelScreenAccess {
    @Shadow @Final @Nullable protected Identifier modelId;

    @Mutable
    @Shadow @Final protected UIModel model;

    protected BaseUIModelHandledScreenMixin(S handler, Inventory inventory, Component title) {
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
