package me.basiqueevangelist.scaldinghot.instrument;

import me.basiqueevangelist.scaldinghot.ScaldingHot;
import me.basiqueevangelist.scaldinghot.ScaldingResourcePack;
import me.basiqueevangelist.scaldinghot.ScaldingResourceReloader;
import me.basiqueevangelist.scaldinghot.api.HotReloadBatch;
import me.basiqueevangelist.scaldinghot.client.ScaldingHotClient;
import net.minecraft.resource.*;
import net.minecraft.util.Identifier;
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
    public static final HotReloadBatchImpl CLIENT_RESOURCES = new HotReloadBatchImpl(ResourceType.CLIENT_RESOURCES);
    public static final HotReloadBatchImpl SERVER_DATA = new HotReloadBatchImpl(ResourceType.SERVER_DATA);

    private final ResourceType type;
    private boolean settleSent = false;

    private final Set<Identifier> addedResources = new HashSet<>();
    private final Set<Identifier> modifiedResources = new HashSet<>();
    private final Set<Identifier> removedResources = new HashSet<>();

    private HotReloadBatchImpl(ResourceType type) {
        this.type = type;
    }

    public static HotReloadBatchImpl get(ResourceType type) {
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

    @Override
    public Set<Identifier> addedResources() {
        return addedResources;
    }

    @Override
    public Set<Identifier> modifiedResources() {
        return modifiedResources;
    }

    @Override
    public Set<Identifier> removedResources() {
        return removedResources;
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

    public void fileAdded(Path path) {
        ScaldingHot.LOGGER.info("+{}", path);

        Identifier id = tryConvert(path);
        if (id == null) return;

        removedResources.remove(id);
        addedResources.add(id);

        sendSettleIfNeeded();
    }

    public void fileModified(Path path) {
        ScaldingHot.LOGGER.info("~{}", path);

        Identifier id = tryConvert(path);
        if (id == null) return;

        if (addedResources.contains(id)) return;

        modifiedResources.add(id);

        sendSettleIfNeeded();
    }

    public void fileRemoved(Path path) {
        ScaldingHot.LOGGER.info("-{}", path);

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

            new CompletableFuture<Void>()
                .completeOnTimeout(null, 250, TimeUnit.MILLISECONDS)
                .thenRunAsync(this::settle, getExecutor());
        }
    }

    private @Nullable Identifier tryConvert(Path path) {
        for (var pack : (Iterable<ResourcePack>) resourceManager().streamResourcePacks()::iterator) {
            if (!(pack instanceof ScaldingResourcePack scalding)) continue;

            Identifier id = scalding.pathToResourceId(this.type, path);

            if (id == null) continue;

            return id;
        }

        ScaldingHot.LOGGER.warn("{} wasn't picked up by any resource packs", path);

        return null;
    }

    private void settle() {
        CompletableFuture.completedFuture(null)
            .thenCompose(ignored -> {
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

                List<ResourceReloader> neededReloaders = new ArrayList<>();

                outer:
                for (var data : ReloaderData.RELOADER_TO_DATA.entrySet()) {
                    if (data.getValue().type != this.type) continue;

                    if (data.getKey() instanceof ScaldingResourceReloader scalding) {
                        scalding.onHotReload(HotReloadBatchImpl.this);
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

                ScaldingHot.LOGGER.info("Reloading {}", neededReloaders.stream().map(ResourceReloader::getName).collect(Collectors.joining(", ")));

                List<ResourceReloader> automaticReloaders = new ArrayList<>(neededReloaders);

                automaticReloaders.removeIf(x -> x instanceof ScaldingResourceReloader);

                return SimpleResourceReload.create(
                        resourceManager(),
                        automaticReloaders,
                        Util.getMainWorkerExecutor(),
                        getExecutor(),
                        CompletableFuture.completedFuture(Unit.INSTANCE)
                    )
                    .whenComplete()
                    .thenRunAsync(() -> {
                        if (type == ResourceType.SERVER_DATA) {
                            ScaldingHot.SERVER.getPlayerManager().saveAllPlayerData();
                            ScaldingHot.SERVER.getPlayerManager().onDataPacksReloaded();
                        }
                    });
            })
            .exceptionally(e -> {
                ScaldingHot.LOGGER.error("Hot reload failed", e);

                return null;
            })
            .whenComplete((i1, i2) -> {
                settleSent = false;

                addedResources.clear();
                modifiedResources.clear();
                removedResources.clear();
            });
    }
}
