package me.basiqueevangelist.scaldinghot;

import net.minecraft.resource.ResourceType;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

import java.nio.file.Path;
import java.util.List;

public interface ScaldingResourcePack {
    List<Path> getRootPaths(ResourceType type);

    @Nullable Identifier pathToResourceId(ResourceType type, Path path);
}
