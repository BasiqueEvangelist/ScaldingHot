package me.basiqueevangelist.scaldinghot.api;

import net.minecraft.resource.ResourcePack;
import net.minecraft.resource.ResourceType;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

import java.nio.file.Path;
import java.util.List;

/**
 * An optional interface for {@link ResourcePack} implementation to add support for hot reloading.
 */
public interface ScaldingResourcePack {
    /**
     * Lists the root paths for a given pack type.
     * @param type the pack type use
     * @return all NIO paths that are used by this pack for this pack type
     */
    List<Path> getRootPaths(ResourceType type);

    /**
     * Converts an NIO path into a resource identifier.
     * @param type the pack type to query by
     * @param path the path to convert
     * @return the corresponding resource identifier, or {@code null} if there is none
     */
    @Nullable Identifier pathToResourceId(ResourceType type, Path path);
}
