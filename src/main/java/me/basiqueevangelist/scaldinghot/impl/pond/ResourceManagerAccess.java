package me.basiqueevangelist.scaldinghot.impl.pond;

import net.minecraft.server.packs.PackType;

public interface ResourceManagerAccess {
    PackType scaldinghot$type();

    void scaldinghot$recreate();
}
