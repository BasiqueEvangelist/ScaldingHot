package me.basiqueevangelist.scaldinghot.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import me.basiqueevangelist.scaldinghot.impl.instrument.InstrumentingResourceManager;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.server.packs.resources.ProfiledReloadInstance;
import net.minecraft.server.packs.resources.ResourceManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(ProfiledReloadInstance.class)
public class ProfiledResourceReloadMixin {
    @ModifyArg(method = "method_18355", at = @At(value = "INVOKE", target = "Lnet/minecraft/resource/ResourceReloader;reload(Lnet/minecraft/resource/ResourceReloader$Synchronizer;Lnet/minecraft/resource/ResourceManager;Lnet/minecraft/util/profiler/Profiler;Lnet/minecraft/util/profiler/Profiler;Ljava/util/concurrent/Executor;Ljava/util/concurrent/Executor;)Ljava/util/concurrent/CompletableFuture;"))
    private static ResourceManager instrument(ResourceManager manager, @Local(argsOnly = true) PreparableReloadListener reloader) {
        return InstrumentingResourceManager.wrap(manager, reloader);
    }
}
