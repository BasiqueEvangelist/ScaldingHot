package me.basiqueevangelist.scaldinghot.instrument;

import me.basiqueevangelist.scaldinghot.ScaldingHot;
import me.basiqueevangelist.scaldinghot.ScaldingResourceReloader;
import me.basiqueevangelist.scaldinghot.client.ScaldingHotClient;
import net.minecraft.resource.*;
import net.minecraft.util.Unit;
import net.minecraft.util.Util;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class HotReloadBatch {
    public static final HotReloadBatch CLIENT_RESOURCES = new HotReloadBatch(ResourceType.CLIENT_RESOURCES);
    public static final HotReloadBatch SERVER_DATA = new HotReloadBatch(ResourceType.SERVER_DATA);

    private final ResourceType type;
    private boolean settleSent = false;
    private final List<Path> changedPaths = new ArrayList<>();

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

    public void addChanged(Path path) {
        changedPaths.add(path);

        if (!settleSent) {
            settleSent = true;

            new CompletableFuture<Void>()
                .completeOnTimeout(null, 250, TimeUnit.MILLISECONDS)
                .thenRunAsync(this::settle);
        }
    }

    private void settle() {
        try {
            List<ResourceReloader> neededReloaders = new ArrayList<>();

            outer:
            for (var data : ReloaderData.RELOADER_TO_DATA.entrySet()) {
                if (data.getValue().type != this.type) continue;

                for (var path : changedPaths) {
                    if (data.getValue().isRelevant(path)) {
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
            );

            for (var reloader : neededReloaders) {
                if (reloader instanceof ScaldingResourceReloader scalding) {
                    scalding.onHotReloadFinished();
                }
            }

            if (type == ResourceType.SERVER_DATA) {
                ScaldingHot.SERVER.getPlayerManager().saveAllPlayerData();
                ScaldingHot.SERVER.getPlayerManager().onDataPacksReloaded();
            }
        } finally {
            settleSent = false;
            changedPaths.clear();
        }
    }
}
