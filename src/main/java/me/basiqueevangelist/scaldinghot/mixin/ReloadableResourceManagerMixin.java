package me.basiqueevangelist.scaldinghot.mixin;

import me.basiqueevangelist.scaldinghot.impl.pond.ResourceManagerAccess;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.CloseableResourceManager;
import net.minecraft.server.packs.resources.ReloadableResourceManager;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(ReloadableResourceManager.class)
public class ReloadableResourceManagerMixin implements ResourceManagerAccess {
    @Shadow @Final private PackType type;

    @Shadow private CloseableResourceManager resources;

    @Override
    public PackType scaldinghot$type() {
        return type;
    }

    @Override
    public void scaldinghot$recreate() {
        ((ResourceManagerAccess) resources).scaldinghot$recreate();
    }
}
