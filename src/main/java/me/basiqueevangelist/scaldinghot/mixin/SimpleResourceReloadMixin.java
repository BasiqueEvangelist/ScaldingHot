package me.basiqueevangelist.scaldinghot.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import me.basiqueevangelist.scaldinghot.impl.instrument.InstrumentingResourceManager;
import me.basiqueevangelist.scaldinghot.impl.instrument.ResourceWatcher;
import me.basiqueevangelist.scaldinghot.impl.pond.ResourceManagerAccess;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.server.packs.resources.ReloadInstance;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleReloadInstance;
import net.minecraft.util.Unit;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

@Mixin(SimpleReloadInstance.class)
public class SimpleResourceReloadMixin {
    @ModifyArg(method = "method_18368", at = @At(value = "INVOKE", target = "Lnet/minecraft/resource/ResourceReloader;reload(Lnet/minecraft/resource/ResourceReloader$Synchronizer;Lnet/minecraft/resource/ResourceManager;Lnet/minecraft/util/profiler/Profiler;Lnet/minecraft/util/profiler/Profiler;Ljava/util/concurrent/Executor;Ljava/util/concurrent/Executor;)Ljava/util/concurrent/CompletableFuture;"))
    private static ResourceManager instrument(ResourceManager manager, @Local(argsOnly = true) PreparableReloadListener reloader) {
        return InstrumentingResourceManager.wrap(manager, reloader);
    }

    @Inject(method = "start", at = @At("HEAD"))
    private static void clearWatches(ResourceManager manager, List<PreparableReloadListener> reloaders, Executor prepareExecutor, Executor applyExecutor, CompletableFuture<Unit> initialStage, boolean profiled, CallbackInfoReturnable<ReloadInstance> cir) {
        if (manager instanceof ResourceManagerAccess access) {
            ResourceWatcher.get(access.scaldinghot$type()).start(manager.listPacks().toList());
        }
    }
}
