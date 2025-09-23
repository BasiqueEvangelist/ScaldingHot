package me.basiqueevangelist.scaldinghot.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import me.basiqueevangelist.scaldinghot.impl.CursedThreadLocals;
import me.basiqueevangelist.scaldinghot.impl.instrument.InstrumentingResourceManager;
import me.basiqueevangelist.scaldinghot.impl.instrument.ReloaderShed;
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
public class SimpleReloadInstanceMixin {
    @ModifyArg(method = "prepareTasks", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/packs/resources/SimpleReloadInstance$StateFactory;create(Lnet/minecraft/server/packs/resources/PreparableReloadListener$PreparationBarrier;Lnet/minecraft/server/packs/resources/ResourceManager;Lnet/minecraft/server/packs/resources/PreparableReloadListener;Ljava/util/concurrent/Executor;Ljava/util/concurrent/Executor;)Ljava/util/concurrent/CompletableFuture;"))
    private ResourceManager instrument(ResourceManager manager, @Local PreparableReloadListener reloader) {
        return InstrumentingResourceManager.wrap(manager, reloader);
    }

    @Inject(method = "create", at = @At("HEAD"))
    private static void clearWatches(ResourceManager manager, List<PreparableReloadListener> reloaders, Executor prepareExecutor, Executor applyExecutor, CompletableFuture<Unit> initialStage, boolean profiled, CallbackInfoReturnable<ReloadInstance> cir) {
        if (CursedThreadLocals.IN_HOT_RELOAD.get()) return;

        if (manager instanceof ResourceManagerAccess access) {
            ResourceWatcher.get(access.scaldinghot$type()).start(manager.listPacks().toList());

            ReloaderShed.stowReloaders(access.scaldinghot$type(), reloaders);
        }
    }
}
