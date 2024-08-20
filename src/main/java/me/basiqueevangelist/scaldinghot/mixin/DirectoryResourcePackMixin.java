package me.basiqueevangelist.scaldinghot.mixin;

import me.basiqueevangelist.scaldinghot.api.ScaldingResourcePack;
import net.minecraft.resource.DirectoryResourcePack;
import net.minecraft.resource.ResourceType;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.nio.file.Path;
import java.util.List;

@Mixin(DirectoryResourcePack.class)
public class DirectoryResourcePackMixin implements ScaldingResourcePack {
    @Shadow @Final private Path root;

    @Override
    public List<Path> getRootPaths(ResourceType type) {
        return List.of(root.resolve(type.getDirectory()));
    }

    @Override
    public @Nullable Identifier pathToResourceId(ResourceType type, Path path) {
        String separator = this.root.getFileSystem().getSeparator();
        Path typePath = this.root.resolve(type.getDirectory());

        if (!path.startsWith(typePath)) return null;

        Path relPath = typePath.relativize(path);
        String namespace = relPath.getName(0).toString();
        Path nsPath = typePath.resolve(namespace);

        String filename = nsPath.relativize(path).toString().replace(separator, "/");
        return Identifier.tryParse(namespace, filename);
    }
}
