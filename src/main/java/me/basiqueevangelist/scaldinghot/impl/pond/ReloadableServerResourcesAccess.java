package me.basiqueevangelist.scaldinghot.impl.pond;

import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;

import java.util.List;

public interface ReloadableServerResourcesAccess {
    void scaldinghot$insertRegistries(RegistryAccess.Frozen newRegistries);

    List<Registry.PendingTags<?>> scaldinghot$getPostponedTags();
}
