package me.basiqueevangelist.scaldinghot.mixin.client;

import me.basiqueevangelist.scaldinghot.impl.pond.ResourceAccess;
import me.basiqueevangelist.scaldinghot.impl.pond.SpriteContentsAccess;
import net.minecraft.client.renderer.texture.SpriteContents;
import net.minecraft.client.renderer.texture.atlas.SpriteResourceLoader;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.metadata.MetadataSectionSerializer;
import net.minecraft.server.packs.resources.Resource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Collection;

@Mixin(SpriteResourceLoader.class)
public interface SpriteOpenerMixin {
    @Inject(method = "method_52851", at = @At(value = "RETURN"))
    private static void addOriginalId(Collection<MetadataSectionSerializer<?>> metadatas, ResourceLocation id, Resource resource, CallbackInfoReturnable<SpriteContents> cir) {
        var ret = cir.getReturnValue();
        if (ret == null) return;

        ((SpriteContentsAccess) ret).scaldinghot$setOriginalId(((ResourceAccess) resource).scaldinghot$id());
    }
}
