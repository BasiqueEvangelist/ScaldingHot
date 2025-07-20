package me.basiqueevangelist.scaldinghot.impl.pond;

import net.minecraft.core.RegistryAccess;

public interface ReloadableServerResourcesAccess {
    void scaldinghot$insertRegistries(RegistryAccess.Frozen newRegistries);
}
