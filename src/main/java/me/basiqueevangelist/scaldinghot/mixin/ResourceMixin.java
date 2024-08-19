package me.basiqueevangelist.scaldinghot.mixin;

import me.basiqueevangelist.scaldinghot.pond.ResourceAccess;
import net.minecraft.resource.Resource;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(Resource.class)
public class ResourceMixin implements ResourceAccess {
    @Unique private Identifier scaldinghot$id = null;

    @Override
    public @Nullable Identifier scaldinghot$id() {
        return scaldinghot$id;
    }

    @Override
    public void scaldinghot$setId(Identifier id) {
        scaldinghot$id = id;
    }
}
