package me.basiqueevangelist.scaldinghot.instrument;

import me.basiqueevangelist.scaldinghot.ScaldingHot;
import me.basiqueevangelist.scaldinghot.ScaldingResourcePack;
import me.basiqueevangelist.scaldinghot.ScaldingResourceReloader;
import me.basiqueevangelist.scaldinghot.client.ScaldingHotClient;
import net.minecraft.resource.*;
import net.minecraft.util.Identifier;
import net.minecraft.util.Unit;
import net.minecraft.util.Util;
import org.jetbrains.annotations.Nullable;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class HotReloadBatch {
    public static final HotReloadBatch CLIENT_RESOURCES = new HotReloadBatch(ResourceType.CLIENT_RESOURCES);
    public static final HotReloadBatch SERVER_DATA = new HotReloadBatch(ResourceType.SERVER_DATA);

    private final ResourceType type;
    private boolean settleSent = false;

    private final Set<Path> addedPaths = new HashSet<>();
    private final Set<Path> modifiedPaths = new HashSet<>();
    private final Set<Path> removedPaths = new HashSet<>();

    private HotReloadBatch(ResourceType type) {
        this.type = type;
    }

    public static HotReloadBatch get(ResourceType type) {
        return switch (type) {
            case CLIENT_RESOURCES -> CLIENT_RESOURCES;
            case SERVER_DATA -> SERVER_DATA;
        };
    }

    private Executor getExecutor() {
        return switch (this.type) {
            case CLIENT_RESOURCES -> ScaldingHotClient.getClientExecutor();
            case SERVER_DATA -> ScaldingHot.SERVER;
        };
    }

    private ResourceManager getResourceManager() {
        return switch (this.type) {
            case CLIENT_RESOURCES -> ScaldingHotClient.getClientResourceManager();
            case SERVER_DATA -> ScaldingHot.SERVER.getResourceManager();
        };
    }

    public void fileAdded(Path path) {
        removedPaths.remove(path);
        addedPaths.add(path);

        sendSettleIfNeeded();
    }

    public void fileModified(Path path) {
        if (addedPaths.contains(path)) return;

        modifiedPaths.add(path);

        sendSettleIfNeeded();
    }

    public void fileRemoved(Path path) {
        if (addedPaths.contains(path)) {
            addedPaths.remove(path);
            return;
        }

        modifiedPaths.remove(path);
        removedPaths.add(path);

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

    private Set<Identifier> toIdSet(Set<Path> paths, List<ScaldingResourcePack> packs) {
        Set<Identifier> ids = new HashSet<>();

        outer:
        for (var path : paths) {
            for (var pack : packs) {
                Identifier id = pack.pathToResourceId(this.type, path);

                if (id == null) continue;

                ids.add(id);

                continue outer;
            }

            ScaldingHot.LOGGER.warn("{} wasn't picked up by any resource packs", path);
        }

        return ids;
    }

    private void settle() {
        try {
            var packs = getResourceManager().streamResourcePacks().filter(x -> x instanceof ScaldingResourcePack).map(x -> (ScaldingResourcePack) x).toList();

            var addedIds = toIdSet(addedPaths, packs);
            var modifiedIds = toIdSet(modifiedPaths, packs);
            var removedIds = toIdSet(removedPaths, packs);

            Set<Identifier> changedIds = new HashSet<>();

            changedIds.addAll(addedIds);
            changedIds.addAll(modifiedIds);
            changedIds.addAll(removedIds);

            StringBuilder sb = new StringBuilder();

            for (var id : addedIds) {
                sb.append("\n+ ").append(id);
            }

            for (var id : modifiedIds) {
                sb.append("\n~ ").append(id);
            }

            for (var id : removedIds) {
                sb.append("\n- ").append(id);
            }

            ScaldingHot.LOGGER.info("commiting changes: {}", sb);

            List<ResourceReloader> neededReloaders = new ArrayList<>();

            outer:
            for (var data : ReloaderData.RELOADER_TO_DATA.entrySet()) {
                if (data.getValue().type != this.type) continue;

                for (var id : changedIds) {
                    if (data.getValue().isRelevant(id)) {
                        neededReloaders.add(data.getKey());
                        continue outer;
                    }
                }
            }

            if (neededReloaders.isEmpty()) return;

            ScaldingHot.LOGGER.info("Reloading {}", neededReloaders.stream().map(ResourceReloader::getName).collect(Collectors.joining(", ")));

            SimpleResourceReload.create(
                getResourceManager(),
                neededReloaders,
                Util.getMainWorkerExecutor(),
                getExecutor(),
                CompletableFuture.completedFuture(Unit.INSTANCE)
            ).whenComplete().thenRunAsync(() -> {
                for (var reloader : neededReloaders) {
                    if (reloader instanceof ScaldingResourceReloader scalding) {
                        scalding.onHotReloadFinished();
                    }
                }

                if (type == ResourceType.SERVER_DATA) {
                    ScaldingHot.SERVER.getPlayerManager().saveAllPlayerData();
                    ScaldingHot.SERVER.getPlayerManager().onDataPacksReloaded();
                }
            })
            .exceptionally(e -> {
                ScaldingHot.LOGGER.error("Hot reload failed", e);

                return null;
            });
        } finally {
            settleSent = false;

            addedPaths.clear();
            modifiedPaths.clear();
            removedPaths.clear();
        }
    }
}
