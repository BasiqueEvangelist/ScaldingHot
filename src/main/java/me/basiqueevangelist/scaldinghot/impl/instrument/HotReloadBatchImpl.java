package me.basiqueevangelist.scaldinghot.impl.instrument;

import me.basiqueevangelist.scaldinghot.api.HotReloadBatch;
import me.basiqueevangelist.scaldinghot.api.HotReloadPlugin;
import me.basiqueevangelist.scaldinghot.api.ScaldingPackResources;
import me.basiqueevangelist.scaldinghot.impl.ScaldingHot;
import me.basiqueevangelist.scaldinghot.impl.ScaldingRegistry;
import me.basiqueevangelist.scaldinghot.impl.client.ScaldingHotClient;
import me.basiqueevangelist.scaldinghot.impl.pond.ResourceManagerAccess;
import net.minecraft.Util;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleReloadInstance;
import net.minecraft.util.Unit;
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

    private final Set<ResourceLocation> addedResources = new HashSet<>();
    private final Set<ResourceLocation> modifiedResources = new HashSet<>();
    private final Set<ResourceLocation> removedResources = new HashSet<>();

    private final List<Runnable> pendingTasks = new ArrayList<>();

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
    public Collection<ResourceLocation> changedResources() {
        // TODO: make this good.
        Set<ResourceLocation> changed = new HashSet<>();
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
    public void queueFinishTask(Runnable task) {
        this.pendingTasks.add(task);
    }

    public void fileAdded(Path path) {
        ResourceLocation id = tryConvert(path);
        if (id == null) return;

        removedResources.remove(id);
        addedResources.add(id);

        sendSettleIfNeeded();
    }

    public void fileModified(Path path) {
        ResourceLocation id = tryConvert(path);
        if (id == null) return;

        if (addedResources.contains(id)) return;

        modifiedResources.add(id);

        sendSettleIfNeeded();
    }

    public void fileRemoved(Path path) {
        ResourceLocation id = tryConvert(path);
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

            new CompletableFuture<Void>()
                .completeOnTimeout(null, 500, TimeUnit.MILLISECONDS)
                .thenRunAsync(this::settle, getExecutor());
        }
    }

    private @Nullable ResourceLocation tryConvert(Path path) {
        for (var pack : (Iterable<PackResources>) resourceManager().listPacks()::iterator) {
            if (!(pack instanceof ScaldingPackResources scalding)) continue;

            ResourceLocation id = scalding.pathToResourceId(this.type, path);

            if (id == null) continue;

            return id;
        }

//        ScaldingHot.LOGGER.warn("{} wasn't picked up by any resource packs", path);

        return null;
    }

    private void settle() {
        CompletableFuture.completedFuture(null)
            .thenCompose(ignored -> {
                Set<ResourceLocation> changedIds = new HashSet<>();

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
                for (var data : ReloaderData.RELOADER_TO_DATA.entrySet()) {
                    if (data.getValue().type != this.type) continue;

                    if (data.getKey() instanceof HotReloadPlugin scalding) {
                        try {
                            scalding.onHotReload(HotReloadBatchImpl.this);
                        } catch (Exception e) {
                            reloadFailed.addSuppressed(e);
                            fail = true;
                        }
                    }

                    for (var id : changedIds) {
                        if (data.getValue().isRelevant(id)) {
                            neededReloaders.add(data.getKey());
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

                return SimpleReloadInstance.of(
                        resourceManager(),
                        automaticReloaders,
                        Util.backgroundExecutor(),
                        getExecutor(),
                        CompletableFuture.completedFuture(Unit.INSTANCE)
                    )
                    .done();
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
            .whenComplete((i1, i2) -> {
                settleSent = false;

                addedResources.clear();
                modifiedResources.clear();
                removedResources.clear();
                pendingTasks.clear();
            });
    }
}
