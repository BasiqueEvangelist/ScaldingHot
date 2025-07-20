package me.basiqueevangelist.scaldinghot.impl.instrument;

import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class ReloaderShed {
    public static @Nullable List<PreparableReloadListener> SERVER = null;
    public static @Nullable List<PreparableReloadListener> CLIENT = null;

    public static void stowReloaders(PackType type, List<PreparableReloadListener> reloaders) {
        if (type == PackType.SERVER_DATA) SERVER = reloaders;
        else CLIENT = reloaders;
    }

    public static List<PreparableReloadListener> getFor(PackType type) {
        if (type == PackType.SERVER_DATA) return SERVER;
        else return CLIENT;
    }
}
