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
public class ProfiledReloadInstanceMixin {
    @ModifyArg(method = "method_18355", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/packs/resources/PreparableReloadListener;reload(Lnet/minecraft/server/packs/resources/PreparableReloadListener$PreparationBarrier;Lnet/minecraft/server/packs/resources/ResourceManager;Lnet/minecraft/util/profiling/ProfilerFiller;Lnet/minecraft/util/profiling/ProfilerFiller;Ljava/util/concurrent/Executor;Ljava/util/concurrent/Executor;)Ljava/util/concurrent/CompletableFuture;"))
    private static ResourceManager instrument(ResourceManager manager, @Local(argsOnly = true) PreparableReloadListener reloader) {
        return InstrumentingResourceManager.wrap(manager, reloader);
    }
}
