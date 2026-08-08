package me.basiqueevangelist.scaldinghot.mixin;

import me.basiqueevangelist.scaldinghot.impl.pond.ResourceManagerAccess;
import net.minecraft.resources.Identifier;
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
public abstract class MultiPackResourceManagerMixin implements ResourceManagerAccess {
    @Shadow @Final private List<PackResources> packs;

    @Mutable
    @Shadow @Final private Map<String, FallbackResourceManager> namespacedManagers;

    @Shadow @Nullable protected abstract ResourceFilterSection getPackFilterSection(PackResources pack);

    @Unique private PackType scaldinghot$type;

    @Inject(method = "<init>", at = @At("TAIL"))
    private void init(PackType type, List<PackResources> packs, CallbackInfo ci) {
        this.scaldinghot$type = type;
    }

    @Override
    public PackType scaldinghot$type() {
        return scaldinghot$type;
    }

    public void scaldinghot$recreate() {
        Map<String, FallbackResourceManager> map = new HashMap<>();
        PackType type = scaldinghot$type;

        // Copied from the constructor.
        List<String> list = packs.stream().flatMap(packResourcesx -> packResourcesx.getNamespaces(type).stream()).distinct().toList();

        for (PackResources packResources : packs) {
            ResourceFilterSection resourceFilterSection = this.getPackFilterSection(packResources);
            Set<String> set = packResources.getNamespaces(type);
            Predicate<Identifier> predicate = resourceFilterSection != null
                ? resourceLocation -> resourceFilterSection.isPathFiltered(resourceLocation.getPath())
                : null;

            for (String string : list) {
                boolean bl = set.contains(string);
                boolean bl2 = resourceFilterSection != null && resourceFilterSection.isNamespaceFiltered(string);
                if (bl || bl2) {
                    FallbackResourceManager fallbackResourceManager = (FallbackResourceManager)map.get(string);
                    if (fallbackResourceManager == null) {
                        fallbackResourceManager = new FallbackResourceManager(type, string);
                        map.put(string, fallbackResourceManager);
                    }

                    if (bl && bl2) {
                        fallbackResourceManager.push(packResources, predicate);
                    } else if (bl) {
                        fallbackResourceManager.push(packResources);
                    } else {
                        fallbackResourceManager.pushFilterOnly(packResources.packId(), predicate);
                    }
                }
            }
        }

        this.namespacedManagers = map;
    }
}
