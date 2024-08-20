package me.basiqueevangelist.scaldinghot.impl.client;

import org.lwjgl.util.tinyfd.TinyFileDialogs;

import java.util.concurrent.CompletableFuture;

public class DialogUtil {
    public static CompletableFuture<String> selectFolderDialogAsync(String title, String defaultPath) {
        CompletableFuture<String> result = new CompletableFuture<>();

        Thread taskThread = new Thread(() -> {
            try {
                result.complete(TinyFileDialogs.tinyfd_selectFolderDialog(title, defaultPath));
            } catch (Throwable t) {
                result.completeExceptionally(t);
            }
        }, "File dialog thread");
        taskThread.start();

        return result;
    }
}
