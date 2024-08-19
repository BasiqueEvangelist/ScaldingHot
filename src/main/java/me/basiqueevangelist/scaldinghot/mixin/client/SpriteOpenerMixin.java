package me.basiqueevangelist.scaldinghot.mixin.client;

import me.basiqueevangelist.scaldinghot.pond.ResourceAccess;
import me.basiqueevangelist.scaldinghot.pond.SpriteContentsAccess;
import net.minecraft.client.texture.SpriteContents;
import net.minecraft.client.texture.SpriteOpener;
import net.minecraft.resource.Resource;
import net.minecraft.resource.metadata.ResourceMetadataReader;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Collection;

@Mixin(SpriteOpener.class)
public interface SpriteOpenerMixin {
    @Inject(method = "method_52851", at = @At(value = "RETURN"))
    private static void addOriginalId(Collection<ResourceMetadataReader<?>> metadatas, Identifier id, Resource resource, CallbackInfoReturnable<SpriteContents> cir) {
        var ret = cir.getReturnValue();
        if (ret == null) return;

        ((SpriteContentsAccess) ret).scaldinghot$setOriginalId(((ResourceAccess) resource).scaldinghot$id());
    }
}
