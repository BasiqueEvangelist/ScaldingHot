package me.basiqueevangelist.scaldinghot.mixin;

import me.basiqueevangelist.scaldinghot.impl.pond.ResourceManagerAccess;
import net.minecraft.resource.LifecycledResourceManagerImpl;
import net.minecraft.resource.NamespaceResourceManager;
import net.minecraft.resource.ResourcePack;
import net.minecraft.resource.ResourceType;
import net.minecraft.resource.metadata.ResourceFilter;
import net.minecraft.util.Identifier;
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

@Mixin(LifecycledResourceManagerImpl.class)
public abstract class LifecycledResourceManagerImplMixin implements ResourceManagerAccess {
    @Shadow @Final private List<ResourcePack> packs;

    @Shadow @Nullable protected abstract ResourceFilter parseResourceFilter(ResourcePack pack);

    @Mutable
    @Shadow @Final private Map<String, NamespaceResourceManager> subManagers;
    @Unique private ResourceType scaldinghot$type;

    @Inject(method = "<init>", at = @At("TAIL"))
    private void init(ResourceType resourceType, List<ResourcePack> list, CallbackInfo ci) {
        this.scaldinghot$type = resourceType;
    }

    @Override
    public ResourceType scaldinghot$type() {
        return scaldinghot$type;
    }

    public void scaldinghot$recreate() {
        Map<String, NamespaceResourceManager> map = new HashMap<>();
        ResourceType type = scaldinghot$type;

        // Copied from the constructor.
        List<String> list = packs.stream().flatMap(pack -> pack.getNamespaces(type).stream()).distinct().toList();

        for (ResourcePack resourcePack : packs) {
            ResourceFilter resourceFilter = this.parseResourceFilter(resourcePack);
            Set<String> set = resourcePack.getNamespaces(type);
            Predicate<Identifier> predicate = resourceFilter != null ? id -> resourceFilter.isPathBlocked(id.getPath()) : null;

            for (String string : list) {
                boolean bl = set.contains(string);
                boolean bl2 = resourceFilter != null && resourceFilter.isNamespaceBlocked(string);
                if (bl || bl2) {
                    NamespaceResourceManager namespaceResourceManager = map.get(string);
                    if (namespaceResourceManager == null) {
                        namespaceResourceManager = new NamespaceResourceManager(type, string);
                        map.put(string, namespaceResourceManager);
                    }

                    if (bl && bl2) {
                        namespaceResourceManager.addPack(resourcePack, predicate);
                    } else if (bl) {
                        namespaceResourceManager.addPack(resourcePack);
                    } else {
                        namespaceResourceManager.addPack(resourcePack.getId(), predicate);
                    }
                }
            }
        }

        this.subManagers = map;
    }
}
