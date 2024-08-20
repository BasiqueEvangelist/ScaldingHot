package me.basiqueevangelist.scaldinghot.api;

import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.ResourceType;
import net.minecraft.util.Identifier;

import java.util.Collection;

/**
 * A batch of modified resources.
 */
public interface HotReloadBatch {
    /**
     * @return the pack type of this hot reload
     */
    ResourceType type();

    /**
     * @return the resource manager for this hot reload
     */
    ResourceManager resourceManager();

    /**
     * @return all resource IDs added, modified or removed in this hot reload
     */
    Collection<Identifier> changedResources();

    /**
     * @return all resource IDs added in this hot reload
     */
    Collection<Identifier> addedResources();

    /**
     * @return all resource IDs modified in this hot reload
     */
    Collection<Identifier> modifiedResources();

    /**
     * @return all resource IDs removed in this hot reload
     */
    Collection<Identifier> removedResources();

    /**
     * Queues a task to be run when this hot reload is about to finish.
     * @param task the task to queue.
     * @apiNote Tasks will not be run if the hot reload fails.
     */
    void queueFinishTask(Runnable task);
}
