package me.basiqueevangelist.scaldinghot.instrument;

import me.basiqueevangelist.scaldinghot.ScaldingHot;
import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener;
import net.minecraft.resource.ResourceReloader;
import net.minecraft.resource.ResourceType;

import java.nio.file.Path;
import java.util.*;

public class ReloaderData {
    private static final boolean LOG_ALL_ACCESSES = false;

    static final WeakHashMap<ResourceReloader, ReloaderData> RELOADER_TO_DATA = new WeakHashMap<>();

    public final String reloaderName;
    public final ResourceType type;
    public final Set<Path> accessedPaths = new HashSet<>();

    public ReloaderData(ResourceReloader reloader, ResourceType type) {
        this.type = type;
        if (reloader instanceof IdentifiableResourceReloadListener identifiable) {
            reloaderName = identifiable.getFabricId().toString();
        } else {
            reloaderName = reloader.getName();
        }
    }

    public static ReloaderData getForReloader(ResourceReloader reloader, ResourceType type) {
        return RELOADER_TO_DATA.computeIfAbsent(reloader, r -> new ReloaderData(r, type));
    }

    public void markAccessed(Path path) {
        accessedPaths.add(path);

        if (LOG_ALL_ACCESSES) ScaldingHot.LOGGER.info("{} accessed {}", reloaderName, path);

        ResourceWatcher.get(type).registerPath(path);
    }

    public boolean isRelevant(Path path) {
        for (var accessed : accessedPaths) {
            if (path.equals(accessed) || path.startsWith(accessed)) return true;
        }

        return false;
    }
}
