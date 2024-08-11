package me.basiqueevangelist.scaldinghot.mixin;

import me.basiqueevangelist.scaldinghot.pond.ResourceManagerAccess;
import net.minecraft.resource.LifecycledResourceManagerImpl;
import net.minecraft.resource.ResourcePack;
import net.minecraft.resource.ResourceType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(LifecycledResourceManagerImpl.class)
public class LifecycledResourceManagerImplMixin implements ResourceManagerAccess {
    @Unique
    private ResourceType scaldinghot$type;

    @Inject(method = "<init>", at = @At("TAIL"))
    private void init(ResourceType resourceType, List<ResourcePack> list, CallbackInfo ci) {
        this.scaldinghot$type = resourceType;
    }

    @Override
    public ResourceType scaldinghot$type() {
        return scaldinghot$type;
    }
}
