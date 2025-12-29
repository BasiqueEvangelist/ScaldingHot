package me.basiqueevangelist.scaldinghot.api;

import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.server.packs.resources.ResourceManager;

import java.util.Collection;

/**
 * A batch of modified resources.
 */
public interface HotReloadBatch {
    /**
     * @return the pack type of this hot reload
     */
    PackType type();

    /**
     * @return the resource manager for this hot reload
     */
    ResourceManager resourceManager();

    /**
     * @return all resource IDs added, modified or removed in this hot reload
     */
    Collection<Identifier> changedResources();

    /**
     * Marks a reloader as needing to be reloaded during this hot reload.
     * @param reloader the reloader to reload
     */
    void markNeedsReload(Class<? extends PreparableReloadListener> reloader);

    /**
     * Queues a task to be run when this hot reload is about to finish.
     * @param task the task to queue.
     * @apiNote Tasks will not be run if the hot reload fails.
     */
    void queueFinishTask(Runnable task);
}
