package me.basiqueevangelist.scaldinghot.instrument;

import me.basiqueevangelist.scaldinghot.ScaldingHot;
import net.minecraft.resource.ResourceType;

import java.io.IOException;
import java.nio.file.*;
import java.util.*;

public class ResourceWatcher {

    public static final ResourceWatcher CLIENT_RESOURCES = new ResourceWatcher(ResourceType.CLIENT_RESOURCES);
    public static final ResourceWatcher SERVER_DATA = new ResourceWatcher(ResourceType.SERVER_DATA);

    private final WatchService watchService;
    private final ResourceType type;
    private final Map<Path, WatchKey> REGISTERED_KEYS = new HashMap<>();

    private ResourceWatcher(ResourceType type) {
        this.type = type;

        try {
            this.watchService = FileSystems.getDefault().newWatchService();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static ResourceWatcher get(ResourceType type) {
        return switch (type) {
            case CLIENT_RESOURCES -> CLIENT_RESOURCES;
            case SERVER_DATA -> SERVER_DATA;
        };
    }

    public void init() {
        Thread.ofPlatform()
            .name("Scalding Hot! Resource Watcher Thread [" + type.getDirectory() + "]")
            .daemon()
            .start(this::thread);
    }

    public void reset() {
        REGISTERED_KEYS.values().forEach(WatchKey::cancel);
        REGISTERED_KEYS.clear();
    }

    private void thread() {
        //noinspection InfiniteLoopStatement
        while (true) {
            try {
                WatchKey key = watchService.take();
                Path basePath = (Path) key.watchable();

                for (var event : key.pollEvents()) {
                    if (!(event.context() instanceof Path)) continue; // mfw

                    Path filePath = basePath.resolve((Path) event.context());

                    if (filePath.getFileName().toString().endsWith("~")) continue; // editor file

                    if (REGISTERED_KEYS.containsKey(filePath)) {
                        REGISTERED_KEYS.remove(filePath).cancel();
                    }

//                    ScaldingHot.LOGGER.info("watch event: {} {}", event.kind().name(), filePath);

                    HotReloadBatch.get(this.type).addChanged(filePath);
                }

                key.reset();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public void registerPath(Path path) {
        if (path.getFileSystem() != FileSystems.getDefault()) return;
        if (REGISTERED_KEYS.containsKey(path)) return;

        if (Files.isSymbolicLink(path)) return;

        if (Files.isDirectory(path)) {
            try (var strem = Files.newDirectoryStream(path)) {
                for (var subdir : strem) {
                    if (Files.isDirectory(subdir)) registerPath(subdir);
                }
            } catch (IOException e) {
                ScaldingHot.LOGGER.error("Couldn't register watches for subdirectories of {}", path, e);
            }
        }

        if (Files.isRegularFile(path)) path = path.getParent();

        try {
            var key = path.register(watchService, StandardWatchEventKinds.ENTRY_CREATE, StandardWatchEventKinds.ENTRY_MODIFY, StandardWatchEventKinds.ENTRY_DELETE);
            REGISTERED_KEYS.put(path, key);
        } catch (IOException e) {
            ScaldingHot.LOGGER.error("Couldn't register watch for {}", path, e);
        }
    }
}
