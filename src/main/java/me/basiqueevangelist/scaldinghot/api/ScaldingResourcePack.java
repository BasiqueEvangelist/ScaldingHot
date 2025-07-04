package me.basiqueevangelist.scaldinghot.api;

import org.jetbrains.annotations.Nullable;

import java.nio.file.Path;
import java.util.List;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.PackType;

/**
 * An optional interface for {@link PackResources} implementation to add support for hot reloading.
 */
public interface ScaldingResourcePack {
    /**
     * Lists the root paths for a given pack type.
     * @param type the pack type use
     * @return all NIO paths that are used by this pack for this pack type
     */
    List<Path> getRootPaths(PackType type);

    /**
     * Converts an NIO path into a resource identifier.
     * @param type the pack type to query by
     * @param path the path to convert
     * @return the corresponding resource identifier, or {@code null} if there is none
     */
    @Nullable ResourceLocation pathToResourceId(PackType type, Path path);
}
