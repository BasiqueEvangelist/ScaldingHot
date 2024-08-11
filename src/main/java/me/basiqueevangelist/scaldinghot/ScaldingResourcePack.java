package me.basiqueevangelist.scaldinghot;

import net.minecraft.resource.ResourceType;

import java.nio.file.Path;
import java.util.List;

public interface ScaldingResourcePack {
    List<Path> getPathsForSearch(ResourceType type, String namespace, String path);
}
