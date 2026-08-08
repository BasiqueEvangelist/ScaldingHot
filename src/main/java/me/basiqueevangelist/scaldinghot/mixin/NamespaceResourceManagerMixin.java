package me.basiqueevangelist.scaldinghot.mixin;

import me.basiqueevangelist.scaldinghot.impl.pond.ResourceAccess;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.resources.FallbackResourceManager;
import net.minecraft.server.packs.resources.IoSupplier;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceMetadata;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.io.InputStream;
import java.util.List;

@Mixin(FallbackResourceManager.class)
public class NamespaceResourceManagerMixin {
    @Inject(method = "createResource", at = @At("RETURN"))
    private static void addId(PackResources source, Identifier location, IoSupplier<InputStream> resource, IoSupplier<ResourceMetadata> metadata, CallbackInfoReturnable<Resource> cir) {
        ((ResourceAccess) cir.getReturnValue()).scaldinghot$setId(location);
    }

    @Inject(method = "getResourceStack", at = @At("RETURN"))
    private void addId(Identifier location, CallbackInfoReturnable<List<Resource>> cir) {
        cir.getReturnValue().forEach(x -> ((ResourceAccess) x).scaldinghot$setId(location));
    }
}
