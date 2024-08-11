package me.basiqueevangelist.scaldinghot.instrument;

import net.minecraft.resource.InputSupplier;

import java.io.IOException;
import java.nio.file.Path;

public interface PathProvidingInputSupplier<T> extends InputSupplier<T> {
    static <T> PathProvidingInputSupplier<T> wrap(InputSupplier<T> original, Path path) {
        return new PathProvidingInputSupplier<>() {
            @Override
            public Path getPath() {
                return path;
            }

            @Override
            public T get() throws IOException {
                return original.get();
            }
        };
    }

    Path getPath();
}
