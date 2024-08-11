package me.basiqueevangelist.scaldinghot.instrument;

import me.basiqueevangelist.scaldinghot.ScaldingHot;
import me.basiqueevangelist.scaldinghot.ScaldingResourcePack;
import me.basiqueevangelist.scaldinghot.mixin.ResourceAccessor;
import me.basiqueevangelist.scaldinghot.pond.ResourceManagerAccess;
import net.minecraft.resource.*;
import net.minecraft.util.Identifier;

import java.io.InputStream;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Stream;

public class InstrumentingResourceManager implements ResourceManager {
    private static final boolean LOG_ALL_ACCESSES = false;

    private final ResourceManager delegate;
    private final ResourceReloader reloader;
    private final ResourceType type;

    public InstrumentingResourceManager(ResourceManager delegate, ResourceReloader reloader, ResourceType type) {
        this.delegate = delegate;
        this.reloader = reloader;
        this.type = type;
    }

    public static ResourceManager wrap(ResourceManager manager, ResourceReloader reloader) {
        if (!(manager instanceof ResourceManagerAccess access)) return manager;
        if (!ScaldingHot.isHotReloadable(reloader)) return manager;

        return new InstrumentingResourceManager(manager, reloader, access.scaldinghot$type());
    }

    @Override
    public Set<String> getAllNamespaces() {
        return delegate.getAllNamespaces();
    }

    @Override
    public List<Resource> getAllResources(Identifier id) {
        if (LOG_ALL_ACCESSES) ScaldingHot.LOGGER.info("{}: getAllResources {}", reloader.getName(), id);

        List<Resource> resources = delegate.getAllResources(id);
        markAllPaths(resources);
        return resources;
    }

    @Override
    public Map<Identifier, Resource> findResources(String startingPath, Predicate<Identifier> allowedPathPredicate) {
        if (LOG_ALL_ACCESSES) ScaldingHot.LOGGER.info("{}: findResources {}", reloader.getName(), startingPath);

        markAllFrom(startingPath);

        Map<Identifier, Resource> res = delegate.findResources(startingPath, allowedPathPredicate);
        markAllPaths(res.values());
        return res;
    }

    @Override
    public Map<Identifier, List<Resource>> findAllResources(String startingPath, Predicate<Identifier> allowedPathPredicate) {
        if (LOG_ALL_ACCESSES) ScaldingHot.LOGGER.info("{}: findAllResources {}", reloader.getName(), startingPath);

        markAllFrom(startingPath);

        Map<Identifier, List<Resource>> res = delegate.findAllResources(startingPath, allowedPathPredicate);
        res.values().forEach(this::markAllPaths);
        return res;
    }

    @Override
    public Stream<ResourcePack> streamResourcePacks() {
        return delegate.streamResourcePacks();
    }

    @Override
    public Optional<Resource> getResource(Identifier id) {
        if (LOG_ALL_ACCESSES) ScaldingHot.LOGGER.info("{}: getResource {}", reloader.getName(), id);

        Optional<Resource> res = delegate.getResource(id);
        res.ifPresent(this::markPath);
        return res;
    }

    private void markAllPaths(Collection<Resource> resources) {
        ReloaderData data = ReloaderData.getForReloader(reloader, type);

        for (var resource : resources) {
            if (((ResourceAccessor) resource).getInputSupplier() instanceof PathProvidingInputSupplier<InputStream> providing) {
                data.markAccessed(providing.getPath());
            }
        }
    }

    private void markPath(Resource resource) {
        ReloaderData data = ReloaderData.getForReloader(reloader, type);

        if (((ResourceAccessor) resource).getInputSupplier() instanceof PathProvidingInputSupplier<InputStream> providing) {
            data.markAccessed(providing.getPath());
        }
    }

    private void markAllFrom(String startingPath) {
        ReloaderData data = ReloaderData.getForReloader(reloader, type);

        for (var pack : (Iterable<ResourcePack>) delegate.streamResourcePacks()::iterator) {
            if (pack instanceof ScaldingResourcePack scalding) {
                for (var namespace : pack.getNamespaces(this.type)) {
                    for (var path : scalding.getPathsForSearch(this.type, namespace, startingPath)) {
                        data.markAccessed(path);
                    }
                }
            }
        }
    }
}
