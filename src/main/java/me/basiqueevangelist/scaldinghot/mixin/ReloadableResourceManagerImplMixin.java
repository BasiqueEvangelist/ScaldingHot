package me.basiqueevangelist.scaldinghot.mixin;

import me.basiqueevangelist.scaldinghot.impl.pond.ResourceManagerAccess;
import net.minecraft.resource.LifecycledResourceManager;
import net.minecraft.resource.ReloadableResourceManagerImpl;
import net.minecraft.resource.ResourceType;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(ReloadableResourceManagerImpl.class)
public class ReloadableResourceManagerImplMixin implements ResourceManagerAccess {
    @Shadow @Final private ResourceType type;

    @Shadow private LifecycledResourceManager activeManager;

    @Override
    public ResourceType scaldinghot$type() {
        return type;
    }

    @Override
    public void scaldinghot$recreate() {
        ((ResourceManagerAccess) activeManager).scaldinghot$recreate();
    }
}
