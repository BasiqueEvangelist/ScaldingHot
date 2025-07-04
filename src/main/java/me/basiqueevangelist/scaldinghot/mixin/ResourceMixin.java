package me.basiqueevangelist.scaldinghot.mixin;

import me.basiqueevangelist.scaldinghot.impl.pond.ResourceAccess;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(Resource.class)
public class ResourceMixin implements ResourceAccess {
    @Unique private ResourceLocation scaldinghot$id = null;

    @Override
    public @Nullable ResourceLocation scaldinghot$id() {
        return scaldinghot$id;
    }

    @Override
    public void scaldinghot$setId(ResourceLocation id) {
        scaldinghot$id = id;
    }
}
