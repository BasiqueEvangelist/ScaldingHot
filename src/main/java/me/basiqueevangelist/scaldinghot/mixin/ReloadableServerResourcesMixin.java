package me.basiqueevangelist.scaldinghot.mixin;

import me.basiqueevangelist.scaldinghot.impl.instrument.InstrumentingResourceManager;
import me.basiqueevangelist.scaldinghot.impl.pond.ReloadableServerResourcesAccess;
import net.minecraft.core.RegistryAccess;
import net.minecraft.server.ReloadableServerRegistries;
import net.minecraft.server.ReloadableServerResources;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.tags.TagManager;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(ReloadableServerResources.class)
public class ReloadableServerResourcesMixin implements ReloadableServerResourcesAccess {
    @Mutable
    @Shadow @Final private ReloadableServerRegistries.Holder fullRegistryHolder;

    @Shadow @Final private ReloadableServerResources.ConfigurableRegistryLookup registryLookup;

    @Shadow @Final private TagManager tagManager;

    @ModifyArg(method = "loadResources", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/ReloadableServerRegistries;reload(Lnet/minecraft/core/LayeredRegistryAccess;Lnet/minecraft/server/packs/resources/ResourceManager;Ljava/util/concurrent/Executor;)Ljava/util/concurrent/CompletableFuture;"))
    private static ResourceManager wrapResourceManager(ResourceManager manager) {
        return new InstrumentingResourceManager(manager, null, PackType.SERVER_DATA);
    }

    @Override
    public void scaldinghot$insertRegistries(RegistryAccess.Frozen newRegistries) {
        this.fullRegistryHolder = new ReloadableServerRegistries.Holder(newRegistries);
        ((ConfigurableRegistryLookupAccessor) this.registryLookup).setRegistryAccess(newRegistries);
        ((TagManagerAccessor) tagManager).setRegistryAccess(newRegistries);
    }
}
