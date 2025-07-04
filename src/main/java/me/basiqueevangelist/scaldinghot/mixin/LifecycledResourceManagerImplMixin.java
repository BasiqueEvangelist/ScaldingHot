package me.basiqueevangelist.scaldinghot.mixin;

import me.basiqueevangelist.scaldinghot.impl.pond.ResourceManagerAccess;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.FallbackResourceManager;
import net.minecraft.server.packs.resources.MultiPackResourceManager;
import net.minecraft.server.packs.resources.ResourceFilterSection;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;

@Mixin(MultiPackResourceManager.class)
public abstract class LifecycledResourceManagerImplMixin implements ResourceManagerAccess {
    @Shadow @Final private List<PackResources> packs;

    @Shadow @Nullable protected abstract ResourceFilterSection parseResourceFilter(PackResources pack);

    @Mutable
    @Shadow @Final private Map<String, FallbackResourceManager> subManagers;
    @Unique private PackType scaldinghot$type;

    @Inject(method = "<init>", at = @At("TAIL"))
    private void init(PackType resourceType, List<PackResources> list, CallbackInfo ci) {
        this.scaldinghot$type = resourceType;
    }

    @Override
    public PackType scaldinghot$type() {
        return scaldinghot$type;
    }

    public void scaldinghot$recreate() {
        Map<String, FallbackResourceManager> map = new HashMap<>();
        PackType type = scaldinghot$type;

        // Copied from the constructor.
        List<String> list = packs.stream().flatMap(pack -> pack.getNamespaces(type).stream()).distinct().toList();

        for (PackResources resourcePack : packs) {
            ResourceFilterSection resourceFilter = this.parseResourceFilter(resourcePack);
            Set<String> set = resourcePack.getNamespaces(type);
            Predicate<ResourceLocation> predicate = resourceFilter != null ? id -> resourceFilter.isPathFiltered(id.getPath()) : null;

            for (String string : list) {
                boolean bl = set.contains(string);
                boolean bl2 = resourceFilter != null && resourceFilter.isNamespaceFiltered(string);
                if (bl || bl2) {
                    FallbackResourceManager namespaceResourceManager = map.get(string);
                    if (namespaceResourceManager == null) {
                        namespaceResourceManager = new FallbackResourceManager(type, string);
                        map.put(string, namespaceResourceManager);
                    }

                    if (bl && bl2) {
                        namespaceResourceManager.push(resourcePack, predicate);
                    } else if (bl) {
                        namespaceResourceManager.push(resourcePack);
                    } else {
                        namespaceResourceManager.pushFilterOnly(resourcePack.packId(), predicate);
                    }
                }
            }
        }

        this.subManagers = map;
    }
}
