package me.basiqueevangelist.scaldinghot.instrument;

import me.basiqueevangelist.scaldinghot.ScaldingHot;
import me.basiqueevangelist.scaldinghot.ScaldingResourcePack;
import net.minecraft.resource.ResourcePack;
import net.minecraft.resource.ResourceType;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;

public class ResourceWatcher {

    public static final ResourceWatcher CLIENT_RESOURCES = new ResourceWatcher(ResourceType.CLIENT_RESOURCES);
    public static final ResourceWatcher SERVER_DATA = new ResourceWatcher(ResourceType.SERVER_DATA);

    private final WatchService watchService;
    private final ResourceType type;
    private final Map<Path, WatchKey> registeredKeys = new HashMap<>();

    private final Set<Path> existingPaths = new HashSet<>();

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

    public void start(List<ResourcePack> packs) {
        registeredKeys.values().forEach(WatchKey::cancel);
        registeredKeys.clear();

        for (var pack : packs) {
            if (pack instanceof ScaldingResourcePack scalding) {
                for (var path : scalding.getRootPaths(this.type)) {
                    registerPath(path);
                }
            }
        }
    }

    private void thread() {
        //noinspection InfiniteLoopStatement
        while (true) {
            try {
                WatchKey key = watchService.take();
                Path basePath = (Path) key.watchable();

                for (var event : key.pollEvents()) {
                    ScaldingHot.LOGGER.debug("{}: {} {} ({})", basePath, event.kind(), event.context(), event.count());

                    if (event.kind() == StandardWatchEventKinds.OVERFLOW) {
                        ScaldingHot.LOGGER.warn("Missed {} watch events for {}", event.count(), basePath);
                        continue;
                    }

                    if (!(event.context() instanceof Path)) continue; // mfw

                    Path filePath = basePath.resolve((Path) event.context());

                    if (filePath.getFileName().toString().endsWith("~")) continue; // editor file

                    if (event.kind() == StandardWatchEventKinds.ENTRY_CREATE) {
                        if (existingPaths.contains(filePath) && Files.isRegularFile(filePath)) {
                            // thanks for lying to me, java

                            HotReloadBatchImpl.get(this.type).fileModified(filePath);
                        } else if (Files.isDirectory(filePath)) {
                            registerPath(filePath);

                            try {
                                Files.walkFileTree(filePath, new SimpleFileVisitor<>() {
                                    @Override
                                    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                                        if (attrs.isRegularFile())
                                            HotReloadBatchImpl.get(ResourceWatcher.this.type).fileAdded(file);

                                        return FileVisitResult.CONTINUE;
                                    }
                                });
                            } catch (IOException e) {
                                ScaldingHot.LOGGER.error("Couldn't walk directory tree of {}", filePath, e);
                            }
                        } else if (Files.isRegularFile(filePath)) {
                            existingPaths.add(filePath);
                            HotReloadBatchImpl.get(this.type).fileAdded(filePath);
                        }
                    } else if (event.kind() == StandardWatchEventKinds.ENTRY_MODIFY) {
                        HotReloadBatchImpl.get(this.type).fileModified(filePath);
                    } else if (event.kind() == StandardWatchEventKinds.ENTRY_DELETE) {
                        if (Files.isDirectory(filePath))
                            registeredKeys.remove(filePath).cancel();
                        else {
                            existingPaths.remove(filePath);
                            HotReloadBatchImpl.get(this.type).fileRemoved(filePath);
                        }
                    }
                }

                key.reset();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private void registerPath(Path path) {
        if (path.getFileSystem() != FileSystems.getDefault()) return;

        if (Files.isSymbolicLink(path)) return;

        if (Files.isDirectory(path)) {
            try (var strem = Files.newDirectoryStream(path)) {
                for (var subdir : strem) {
                    if (Files.isDirectory(subdir)) registerPath(subdir);
                    else existingPaths.add(subdir);
                }
            } catch (IOException e) {
                ScaldingHot.LOGGER.error("Couldn't register watches for subdirectories of {}", path, e);
            }
        }

        if (Files.isRegularFile(path)) path = path.getParent();

        if (registeredKeys.containsKey(path)) return;

        try {
            var key = path.register(watchService, StandardWatchEventKinds.ENTRY_CREATE, StandardWatchEventKinds.ENTRY_MODIFY, StandardWatchEventKinds.ENTRY_DELETE);
            registeredKeys.put(path, key);
        } catch (IOException e) {
            ScaldingHot.LOGGER.error("Couldn't register watch for {}", path, e);
        }
    }
}
