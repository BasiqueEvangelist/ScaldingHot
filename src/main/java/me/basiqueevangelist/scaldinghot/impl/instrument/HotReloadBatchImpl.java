package me.basiqueevangelist.scaldinghot.impl.instrument;

import me.basiqueevangelist.scaldinghot.api.HotReloadBatch;
import me.basiqueevangelist.scaldinghot.api.HotReloadPlugin;
import me.basiqueevangelist.scaldinghot.api.ScaldingPackResources;
import me.basiqueevangelist.scaldinghot.impl.CursedThreadLocals;
import me.basiqueevangelist.scaldinghot.impl.ScaldingHot;
import me.basiqueevangelist.scaldinghot.impl.ScaldingRegistry;
import me.basiqueevangelist.scaldinghot.impl.ServerReloadPlugin;
import me.basiqueevangelist.scaldinghot.impl.client.ScaldingHotClient;
import me.basiqueevangelist.scaldinghot.impl.pond.ReloadableServerResourcesAccess;
import me.basiqueevangelist.scaldinghot.impl.pond.ResourceManagerAccess;
import me.basiqueevangelist.scaldinghot.mixin.MinecraftServerAccessor;
import net.minecraft.core.Registry;
import net.minecraft.resources.Identifier;
import net.minecraft.server.ReloadableServerRegistries;
import net.minecraft.server.ReloadableServerResources;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleReloadInstance;
import net.minecraft.tags.TagLoader;
import net.minecraft.util.Unit;
import net.minecraft.util.Util;
import org.jetbrains.annotations.Nullable;

import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class HotReloadBatchImpl implements HotReloadBatch {
    public static final HotReloadBatchImpl CLIENT_RESOURCES = new HotReloadBatchImpl(PackType.CLIENT_RESOURCES);
    public static final HotReloadBatchImpl SERVER_DATA = new HotReloadBatchImpl(PackType.SERVER_DATA);

    private final PackType type;
    private boolean settleSent = false;

    private final Set<Identifier> addedResources = new HashSet<>();
    private final Set<Identifier> modifiedResources = new HashSet<>();
    private final Set<Identifier> removedResources = new HashSet<>();

    private final List<Runnable> pendingTasks = new ArrayList<>();

    private final Set<Class<? extends PreparableReloadListener>> reloadersToReload = new HashSet<>();

    private HotReloadBatchImpl(PackType type) {
        this.type = type;
    }

    public static HotReloadBatchImpl get(PackType type) {
        return switch (type) {
            case CLIENT_RESOURCES -> CLIENT_RESOURCES;
            case SERVER_DATA -> SERVER_DATA;
        };
    }

    @Override
    public Collection<Identifier> changedResources() {
        // TODO: make this good.
        Set<Identifier> changed = new HashSet<>();
        changed.addAll(addedResources);
        changed.addAll(modifiedResources);
        changed.addAll(removedResources);
        return changed;
    }

    private Executor getExecutor() {
        return switch (this.type) {
            case CLIENT_RESOURCES -> ScaldingHotClient.getClientExecutor();
            case SERVER_DATA -> ScaldingHot.SERVER;
        };
    }

    @Override
    public ResourceManager resourceManager() {
        return switch (this.type) {
            case CLIENT_RESOURCES -> ScaldingHotClient.getClientResourceManager();
            case SERVER_DATA -> ScaldingHot.SERVER.getResourceManager();
        };
    }

    @Override
    public PackType type() {
        return this.type;
    }

    @Override
    public void markNeedsReload(Class<? extends PreparableReloadListener> reloader) {
        reloadersToReload.add(reloader);
    }

    @Override
    public void queueFinishTask(Runnable task) {
        this.pendingTasks.add(task);
    }

    public void fileAdded(Path path) {
        Identifier id = tryConvert(path);
        if (id == null) return;

        removedResources.remove(id);
        addedResources.add(id);

        sendSettleIfNeeded();
    }

    public void fileModified(Path path) {
        Identifier id = tryConvert(path);
        if (id == null) return;

        if (addedResources.contains(id)) return;

        modifiedResources.add(id);

        sendSettleIfNeeded();
    }

    public void fileRemoved(Path path) {
        Identifier id = tryConvert(path);
        if (id == null) return;

        if (addedResources.contains(id)) {
            addedResources.remove(id);
            return;
        }

        modifiedResources.remove(id);
        removedResources.add(id);

        sendSettleIfNeeded();
    }

    private void sendSettleIfNeeded() {
        if (!settleSent) {
            settleSent = true;

            // TODO: make this a proper debounce
            new CompletableFuture<Void>()
                .completeOnTimeout(null, ScaldingHot.CONFIG.get().reloadDebounceMillis, TimeUnit.MILLISECONDS)
                .thenRunAsync(this::settle, getExecutor());
        }
    }

    private @Nullable Identifier tryConvert(Path path) {
        for (var pack : (Iterable<PackResources>) resourceManager().listPacks()::iterator) {
            if (!(pack instanceof ScaldingPackResources scalding)) continue;

            Identifier id = scalding.pathToResourceId(this.type, path);

            if (id == null) continue;

            return id;
        }

//        ScaldingHot.LOGGER.warn("{} wasn't picked up by any resource packs", path);

        return null;
    }

    private void settle() {
        Set<Identifier> changedIds = new HashSet<>();

        changedIds.addAll(addedResources);
        changedIds.addAll(modifiedResources);
        changedIds.addAll(removedResources);

        StringBuilder sb = new StringBuilder();

        for (var id : addedResources) {
            sb.append("\n+ ").append(id);
        }

        for (var id : modifiedResources) {
            sb.append("\n~ ").append(id);
        }

        for (var id : removedResources) {
            sb.append("\n- ").append(id);
        }

        ScaldingHot.LOGGER.info("commiting changes: {}", sb);

        if (resourceManager() instanceof ResourceManagerAccess access) {
            access.scaldinghot$recreate();
        }

        if (type == PackType.SERVER_DATA) {
            ServerReloadPlugin.beforeHotReload();
        }

        CompletableFuture.completedFuture(null)
            .thenCompose(ignored -> {
                boolean needRegistryReload = false;

                for (var id : changedIds) {
                    if (id.getPath().startsWith("tags/")) {
                        needRegistryReload = true;
                        break;
                    }

                    if (ReloaderData.RELOADABLE_REGISTRIES.isRelevant(id)) {
                        needRegistryReload = true;
                        break;
                    }
                }

                if (!needRegistryReload) return CompletableFuture.completedFuture(null);

                List<Registry.PendingTags<?>> pendingTags = TagLoader.loadTagsForExistingRegistries(resourceManager(), ScaldingHot.SERVER.registries().compositeAccess());

                return ReloadableServerRegistries.reload(
                    ScaldingHot.SERVER.registries(),
                    pendingTags,
                    resourceManager(),
                    Util.backgroundExecutor()
                )
                    .thenApply(x -> {
                        var registryAccess = x.layers().compositeAccess();
                        ReloadableServerResources resources = ((MinecraftServerAccessor) ScaldingHot.SERVER).getResources().managers();

                        ((ReloadableServerResourcesAccess) resources).scaldinghot$insertRegistries(registryAccess);
                        ((ReloadableServerResourcesAccess) resources).scaldinghot$insertPostponedTags(pendingTags);

                        return null;
                    });
            })
            .thenCompose(_ -> {
                RuntimeException reloadFailed = new RuntimeException("Hot reload plugins failed to reload");
                boolean fail = false;

                List<PreparableReloadListener> neededReloaders = new ArrayList<>();

                for (var plugin : ScaldingRegistry.listPlugins(this.type)) {
                    try {
                        plugin.onHotReload(this);
                    } catch (Exception e) {
                        reloadFailed.addSuppressed(e);
                        fail = true;
                    }
                }

                outer:
                for (var reloader : ReloaderShed.getFor(this.type)) {
                    if (reloadersToReload.contains(reloader.getClass())) {
                        neededReloaders.add(reloader);
                        continue;
                    }

                    if (reloader instanceof HotReloadPlugin scalding) {
                        try {
                            scalding.onHotReload(HotReloadBatchImpl.this);
                        } catch (Exception e) {
                            reloadFailed.addSuppressed(e);
                            fail = true;
                        }
                    }

                    var data = ReloaderData.RELOADER_TO_DATA.get(reloader);

                    if (data == null) continue;

                    for (var id : changedIds) {
                        if (data.isRelevant(id)) {
                            neededReloaders.add(reloader);
                            continue outer;
                        }
                    }
                }

                if (neededReloaders.isEmpty()) {
                    return CompletableFuture.completedFuture(null);
                }

                if (fail) {
                    throw reloadFailed;
                }

                ScaldingHot.LOGGER.info("Reloading {}", neededReloaders.stream().map(PreparableReloadListener::getName).collect(Collectors.joining(", ")));

                List<PreparableReloadListener> automaticReloaders = new ArrayList<>(neededReloaders);

                automaticReloaders.removeIf(x -> x instanceof HotReloadPlugin);

                CursedThreadLocals.IN_HOT_RELOAD.set(true);
                try {
                    return SimpleReloadInstance.of(
                            resourceManager(),
                            automaticReloaders,
                            Util.backgroundExecutor(),
                            getExecutor(),
                            CompletableFuture.completedFuture(Unit.INSTANCE)
                        )
                        .done();
                } finally {
                    CursedThreadLocals.IN_HOT_RELOAD.remove();
                }
            })
            .thenRunAsync(() -> {
                for (var task : pendingTasks) {
                    try {
                        task.run();
                    } catch (Exception e) {
                        ScaldingHot.LOGGER.error("Finish-up hot reload task failed!", e);
                    }
                }
            }, getExecutor())
            .exceptionally(e -> {
                ScaldingHot.LOGGER.error("Hot reload failed", e);

                return null;
            })
            .whenComplete((_, _) -> {
                settleSent = false;

                addedResources.clear();
                modifiedResources.clear();
                removedResources.clear();
                pendingTasks.clear();
                reloadersToReload.clear();
            });
    }
}
