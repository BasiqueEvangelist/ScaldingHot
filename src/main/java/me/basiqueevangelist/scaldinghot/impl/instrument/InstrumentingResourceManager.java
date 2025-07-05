package me.basiqueevangelist.scaldinghot.impl.instrument;

import me.basiqueevangelist.scaldinghot.impl.ScaldingHot;
import me.basiqueevangelist.scaldinghot.impl.ScaldingRegistry;
import me.basiqueevangelist.scaldinghot.impl.pond.ResourceManagerAccess;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Stream;

public class InstrumentingResourceManager implements ResourceManager {
    private static final boolean LOG_ALL_ACCESSES = false;

    private final ResourceManager delegate;
    private final PreparableReloadListener reloader;
    private final PackType type;

    public InstrumentingResourceManager(ResourceManager delegate, PreparableReloadListener reloader, PackType type) {
        this.delegate = delegate;
        this.reloader = reloader;
        this.type = type;
    }

    public static ResourceManager wrap(ResourceManager manager, PreparableReloadListener reloader) {
        if (!(manager instanceof ResourceManagerAccess access)) return manager;
        if (!ScaldingRegistry.isHotReloadable(reloader)) return manager;

        return new InstrumentingResourceManager(manager, reloader, access.scaldinghot$type());
    }

    @Override
    public Set<String> getNamespaces() {
        return delegate.getNamespaces();
    }

    @Override
    public List<Resource> getResourceStack(ResourceLocation id) {
        if (LOG_ALL_ACCESSES) ScaldingHot.LOGGER.info("{}: getAllResources {}", reloader.getName(), id);

        List<Resource> resources = delegate.getResourceStack(id);
        markPath(id);
        return resources;
    }

    @Override
    public Map<ResourceLocation, Resource> listResources(String startingPath, Predicate<ResourceLocation> allowedPathPredicate) {
        if (LOG_ALL_ACCESSES) ScaldingHot.LOGGER.info("{}: findResources {}", reloader.getName(), startingPath);

        markAllFrom(startingPath);

        Map<ResourceLocation, Resource> res = delegate.listResources(startingPath, allowedPathPredicate);
        markAllPaths(res.keySet());
        return res;
    }

    @Override
    public Map<ResourceLocation, List<Resource>> listResourceStacks(String startingPath, Predicate<ResourceLocation> allowedPathPredicate) {
        if (LOG_ALL_ACCESSES) ScaldingHot.LOGGER.info("{}: findAllResources {}", reloader.getName(), startingPath);

        markAllFrom(startingPath);

        Map<ResourceLocation, List<Resource>> res = delegate.listResourceStacks(startingPath, allowedPathPredicate);
        markAllPaths(res.keySet());
        return res;
    }

    @Override
    public Stream<PackResources> listPacks() {
        return delegate.listPacks();
    }

    @Override
    public Optional<Resource> getResource(ResourceLocation id) {
        if (LOG_ALL_ACCESSES) ScaldingHot.LOGGER.info("{}: getResource {}", reloader.getName(), id);

        markPath(id);

        return delegate.getResource(id);
    }

    private void markAllPaths(Collection<ResourceLocation> ids) {
        ReloaderData data = ReloaderData.getForReloader(reloader, type);

        for (var id : ids) {
            data.markAccessed(id);
        }
    }

    private void markPath(ResourceLocation id) {
        ReloaderData data = ReloaderData.getForReloader(reloader, type);

        data.markAccessed(id);
    }

    private void markAllFrom(String startingPath) {
        ReloaderData data = ReloaderData.getForReloader(reloader, type);

        for (var pack : (Iterable<PackResources>) delegate.listPacks()::iterator) {
            for (var namespace : pack.getNamespaces(this.type)) {
                data.markAccessed(ResourceLocation.fromNamespaceAndPath(namespace, startingPath));
            }
        }
    }
}
