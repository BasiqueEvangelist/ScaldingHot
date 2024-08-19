package me.basiqueevangelist.scaldinghot.mixin;

import me.basiqueevangelist.scaldinghot.pond.ResourceAccess;
import net.minecraft.resource.InputSupplier;
import net.minecraft.resource.NamespaceResourceManager;
import net.minecraft.resource.Resource;
import net.minecraft.resource.ResourcePack;
import net.minecraft.resource.metadata.ResourceMetadata;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.io.InputStream;
import java.util.List;

@Mixin(NamespaceResourceManager.class)
public class NamespaceResourceManagerMixin {
    @Inject(method = "createResource", at = @At("RETURN"))
    private static void addId(ResourcePack pack, Identifier id, InputSupplier<InputStream> supplier, InputSupplier<ResourceMetadata> metadataSupplier, CallbackInfoReturnable<Resource> cir) {
        ((ResourceAccess) cir.getReturnValue()).scaldinghot$setId(id);
    }

    @Inject(method = "getAllResources", at = @At("RETURN"))
    private void addId(Identifier id, CallbackInfoReturnable<List<Resource>> cir) {
        cir.getReturnValue().forEach(x -> ((ResourceAccess) x).scaldinghot$setId(id));
    }
}
