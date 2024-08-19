package me.basiqueevangelist.scaldinghot.mixin.fabric;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import me.basiqueevangelist.scaldinghot.ScaldingHot;
import me.basiqueevangelist.scaldinghot.ScaldingResourcePack;
import net.fabricmc.fabric.api.resource.ResourcePackActivationType;
import net.fabricmc.fabric.impl.resource.loader.ModNioResourcePack;
import net.fabricmc.loader.api.ModContainer;
import net.minecraft.resource.ResourceType;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("UnstableApiUsage")
@Mixin(ModNioResourcePack.class)
public abstract class ModNioResourcePackMixin implements ScaldingResourcePack {
    @Shadow @Final private List<Path> basePaths;

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

    @Override
    public List<Path> getRootPaths(ResourceType type) {
        List<Path> paths = new ArrayList<>();

        for (Path basePath : basePaths) {
            Path path = basePath.resolve(type.getDirectory());
            if (!Files.exists(path)) continue;
            paths.add(path);
        }

        return paths;
    }

    @Override
    public @Nullable Identifier pathToResourceId(ResourceType type, Path path) {
        for (Path basePath : this.basePaths) {
            String separator = basePath.getFileSystem().getSeparator();

            Path typePath = basePath.resolve(type.getDirectory());

            if (!path.startsWith(typePath)) continue;

            Path relPath = typePath.relativize(path);
            String namespace = relPath.getName(0).toString();
            Path nsPath = typePath.resolve(namespace);

            String filename = nsPath.relativize(path).toString().replace(separator, "/");
            return Identifier.tryParse(namespace, filename);
        }

        return null;
    }
}
