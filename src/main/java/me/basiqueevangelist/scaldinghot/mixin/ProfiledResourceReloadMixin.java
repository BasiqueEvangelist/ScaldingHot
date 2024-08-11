package me.basiqueevangelist.scaldinghot.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import me.basiqueevangelist.scaldinghot.instrument.InstrumentingResourceManager;
import net.minecraft.resource.ProfiledResourceReload;
import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.ResourceReloader;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(ProfiledResourceReload.class)
public class ProfiledResourceReloadMixin {
    @ModifyArg(method = "method_18355", at = @At(value = "INVOKE", target = "Lnet/minecraft/resource/ResourceReloader;reload(Lnet/minecraft/resource/ResourceReloader$Synchronizer;Lnet/minecraft/resource/ResourceManager;Lnet/minecraft/util/profiler/Profiler;Lnet/minecraft/util/profiler/Profiler;Ljava/util/concurrent/Executor;Ljava/util/concurrent/Executor;)Ljava/util/concurrent/CompletableFuture;"))
    private static ResourceManager instrument(ResourceManager manager, @Local(argsOnly = true) ResourceReloader reloader) {
        return InstrumentingResourceManager.wrap(manager, reloader);
    }
}
