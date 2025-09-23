package me.basiqueevangelist.scaldinghot.mixin;

import net.minecraft.server.packs.resources.PreparableReloadListener;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Map;

@Mixin(PreparableReloadListener.SharedState.class)
public interface PreparableReloadListenerSharedStateAccessor {
    @Accessor
    Map<PreparableReloadListener.StateKey<?>, Object> getState();

    @Mutable
    @Accessor
    void setState(Map<PreparableReloadListener.StateKey<?>, Object> state);
}
