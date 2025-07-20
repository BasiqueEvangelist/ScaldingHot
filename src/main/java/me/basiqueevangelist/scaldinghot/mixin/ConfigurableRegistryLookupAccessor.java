package me.basiqueevangelist.scaldinghot.mixin;

import net.minecraft.core.RegistryAccess;
import net.minecraft.server.ReloadableServerResources;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ReloadableServerResources.ConfigurableRegistryLookup.class)
public interface ConfigurableRegistryLookupAccessor {
    @Accessor
    @Mutable
    void setRegistryAccess(RegistryAccess registryAccess);
}
