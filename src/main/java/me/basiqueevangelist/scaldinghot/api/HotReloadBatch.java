package me.basiqueevangelist.scaldinghot.api;

import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;

import java.util.Collection;

public interface HotReloadBatch {
    ResourceManager resourceManager();

    Collection<Identifier> changedResources();

    Collection<Identifier> addedResources();

    Collection<Identifier> modifiedResources();

    Collection<Identifier> removedResources();
}
