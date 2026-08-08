package me.basiqueevangelist.scaldinghot.impl.instrument;

import me.basiqueevangelist.scaldinghot.impl.ScaldingRegistry;
import me.basiqueevangelist.scaldinghot.impl.pond.ResourceManagerAccess;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NullMarked;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Stream;

@NullMarked
public class InstrumentingResourceManager implements ResourceManager {
    private final ResourceManager delegate;
    private final @Nullable PreparableReloadListener reloader;
    private final PackType type;

    public InstrumentingResourceManager(ResourceManager delegate, @Nullable PreparableReloadListener reloader, PackType type) {
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
    public List<Resource> getResourceStack(Identifier id) {
        List<Resource> resources = delegate.getResourceStack(id);
        markPath(id);
        return resources;
    }

    @Override
    public Map<Identifier, Resource> listResources(String startingPath, Predicate<Identifier> allowedPathPredicate) {
        markAllFrom(startingPath);

        Map<Identifier, Resource> res = delegate.listResources(startingPath, allowedPathPredicate);
        markAllPaths(res.keySet());
        return res;
    }

    @Override
    public Map<Identifier, List<Resource>> listResourceStacks(String startingPath, Predicate<Identifier> allowedPathPredicate) {
        markAllFrom(startingPath);

        Map<Identifier, List<Resource>> res = delegate.listResourceStacks(startingPath, allowedPathPredicate);
        markAllPaths(res.keySet());
        return res;
    }

    @Override
    public Stream<PackResources> listPacks() {
        return delegate.listPacks();
    }

    @Override
    public Optional<Resource> getResource(Identifier id) {
        markPath(id);

        return delegate.getResource(id);
    }

    private ReloaderData getReloaderData() {
        if (reloader == null)
            return ReloaderData.RELOADABLE_REGISTRIES;
        else
            return ReloaderData.getForReloader(reloader, type);
    }

    private void markAllPaths(Collection<Identifier> ids) {
        ReloaderData data = getReloaderData();

        for (var id : ids) {
            data.markAccessed(id);
        }
    }

    private void markPath(Identifier id) {
        ReloaderData data = getReloaderData();

        data.markAccessed(id);
    }

    private void markAllFrom(String startingPath) {
        ReloaderData data = getReloaderData();

        for (var pack : (Iterable<PackResources>) delegate.listPacks()::iterator) {
            for (var namespace : pack.getNamespaces(this.type)) {
                data.markAccessed(Identifier.fromNamespaceAndPath(namespace, startingPath));
            }
        }
    }
}
