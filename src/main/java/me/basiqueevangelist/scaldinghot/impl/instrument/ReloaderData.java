package me.basiqueevangelist.scaldinghot.impl.instrument;

import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.PreparableReloadListener;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.WeakHashMap;

public class ReloaderData {
    private static final boolean LOG_ALL_ACCESSES = false;

    public static final WeakHashMap<PreparableReloadListener, ReloaderData> RELOADER_TO_DATA = new WeakHashMap<>();

    public static final ReloaderData RELOADABLE_REGISTRIES = new ReloaderData(null, PackType.SERVER_DATA);

    public final String reloaderName;
    public final PackType type;
    public final Set<Identifier> accessedResources = new LinkedHashSet<>();

    public ReloaderData(PreparableReloadListener reloader, PackType type) {
        this.type = type;
        if (reloader instanceof IdentifiableResourceReloadListener identifiable) {
            reloaderName = identifiable.getFabricId().toString();
        } else if (reloader == null) {
            reloaderName = "Reloadable Registries";
        } else {
            reloaderName = reloader.getName();
        }
    }

    public static ReloaderData getForReloader(PreparableReloadListener reloader, PackType type) {
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
