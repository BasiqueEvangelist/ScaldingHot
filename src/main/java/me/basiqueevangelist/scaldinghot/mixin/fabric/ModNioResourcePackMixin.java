package me.basiqueevangelist.scaldinghot.mixin.fabric;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.sugar.Local;
import me.basiqueevangelist.scaldinghot.ScaldingHot;
import me.basiqueevangelist.scaldinghot.ScaldingResourcePack;
import me.basiqueevangelist.scaldinghot.instrument.PathProvidingInputSupplier;
import net.fabricmc.fabric.api.resource.ResourcePackActivationType;
import net.fabricmc.fabric.impl.resource.loader.ModNioResourcePack;
import net.fabricmc.loader.api.ModContainer;
import net.minecraft.resource.InputSupplier;
import net.minecraft.resource.ResourceType;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

import java.io.InputStream;
import java.nio.file.Path;
import java.util.*;

@SuppressWarnings("UnstableApiUsage")
@Mixin(ModNioResourcePack.class)
public abstract class ModNioResourcePackMixin implements ScaldingResourcePack {
    @Shadow @Final private Map<ResourceType, Set<String>> namespaces;

    @Shadow @Final private List<Path> basePaths;

    @Shadow
    private static boolean exists(Path path) {
        throw new UnsupportedOperationException();
    }

    @ModifyExpressionValue(method = "create", at = @At(value = "INVOKE", target = "Lnet/fabricmc/loader/api/ModContainer;getRootPaths()Ljava/util/List;"))
    private static List<Path> injectPaths(List<Path> original, String id, ModContainer mod, String subPath, ResourceType type, ResourcePackActivationType activationType, boolean modBundled) {
        String modid = mod.getMetadata().getId();
        List<String> additionalPaths = ScaldingHot.CONFIG.get().modResourcePaths.get(modid);

        if (additionalPaths == null) return original;

        List<Path> newList = new ArrayList<>();

        for (var additional : additionalPaths) {
            newList.add(Path.of(additional));
        }

        return newList;
    }

    @ModifyReturnValue(method = "openFile", at = @At(value = "RETURN", ordinal = 0))
    private InputSupplier<InputStream> wrap(InputSupplier<InputStream> original, @Local Path path) {
        return PathProvidingInputSupplier.wrap(original, path);
    }

    // Basically copied from ModNioResourcePack#findResources
    @Override
    public List<Path> getPathsForSearch(ResourceType type, String namespace, String path) {
        if (!namespaces.getOrDefault(type, Collections.emptySet()).contains(namespace)) {
            return List.of();
        }

        List<Path> paths = new ArrayList<>();
        
        for (Path basePath : basePaths) {
            String separator = basePath.getFileSystem().getSeparator();
            Path nsPath = basePath.resolve(type.getDirectory()).resolve(namespace);
            Path searchPath = nsPath.resolve(path.replace("/", separator)).normalize();
            if (!exists(searchPath)) continue;

            paths.add(searchPath);
        }
        
        return paths;
    }
}
