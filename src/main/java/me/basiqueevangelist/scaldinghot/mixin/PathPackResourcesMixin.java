package me.basiqueevangelist.scaldinghot.mixin;

import me.basiqueevangelist.scaldinghot.api.ScaldingPackResources;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.PathPackResources;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.nio.file.Path;
import java.util.List;

@Mixin(PathPackResources.class)
public class PathPackResourcesMixin implements ScaldingPackResources {
    @Shadow @Final private Path root;

    @Override
    public List<Path> getRootPaths(PackType type) {
        return List.of(root.resolve(type.getDirectory()));
    }

    @Override
    public @Nullable Identifier pathToResourceId(PackType type, Path path) {
        String separator = this.root.getFileSystem().getSeparator();
        Path typePath = this.root.resolve(type.getDirectory());

        if (!path.startsWith(typePath)) return null;

        Path relPath = typePath.relativize(path);
        String namespace = relPath.getName(0).toString();
        Path nsPath = typePath.resolve(namespace);

        String filename = nsPath.relativize(path).toString().replace(separator, "/");
        return Identifier.tryBuild(namespace, filename);
    }
}
