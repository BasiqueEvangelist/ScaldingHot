package me.basiqueevangelist.scaldinghot.impl.instrument;

import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener;
import net.minecraft.resource.ResourceReloader;
import net.minecraft.resource.ResourceType;
import net.minecraft.util.Identifier;

import java.util.HashSet;
import java.util.Set;
import java.util.WeakHashMap;

public class ReloaderData {
    private static final boolean LOG_ALL_ACCESSES = false;

    public static final WeakHashMap<ResourceReloader, ReloaderData> RELOADER_TO_DATA = new WeakHashMap<>();

    public final String reloaderName;
    public final ResourceType type;
    public final Set<Identifier> accessedResources = new HashSet<>();

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

    public void markAccessed(Identifier id) {
        accessedResources.add(id);
    }

    public boolean isRelevant(Identifier id) {
        for (var accessed : accessedResources) {
            if (!accessed.getNamespace().equals(id.getNamespace())) continue;

            if (id.equals(accessed) || id.getPath().startsWith(accessed.getPath())) return true;
        }

        return false;
    }
}
