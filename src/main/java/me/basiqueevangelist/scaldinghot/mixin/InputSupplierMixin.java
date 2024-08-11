package me.basiqueevangelist.scaldinghot.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import me.basiqueevangelist.scaldinghot.instrument.PathProvidingInputSupplier;
import net.minecraft.resource.InputSupplier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.io.InputStream;
import java.nio.file.Path;

@Mixin(InputSupplier.class)
public interface InputSupplierMixin {
    @ModifyReturnValue(method = "create(Ljava/nio/file/Path;)Lnet/minecraft/resource/InputSupplier;", at = @At("RETURN"))
    private static InputSupplier<InputStream> wrap(InputSupplier<InputStream> original, Path path) {
        return PathProvidingInputSupplier.wrap(original, path);
    }
}
