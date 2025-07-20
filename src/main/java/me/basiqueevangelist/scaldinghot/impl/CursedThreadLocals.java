package me.basiqueevangelist.scaldinghot.impl;

public class CursedThreadLocals {
    public static final ThreadLocal<Boolean> IN_HOT_RELOAD = ThreadLocal.withInitial(() -> false);
}
