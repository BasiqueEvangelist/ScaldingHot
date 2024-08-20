package me.basiqueevangelist.scaldinghot.impl.client;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import me.basiqueevangelist.scaldinghot.impl.ScaldingHot;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.command.CommandSource;
import net.minecraft.text.Text;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.concurrent.CompletableFuture;

import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.argument;
import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal;

public class AddPathCommand {
    private static final SimpleCommandExceptionType INVALID_MOD = new SimpleCommandExceptionType(Text.literal("No such mod"));

    public static void register(CommandDispatcher<FabricClientCommandSource> dispatcher, CommandRegistryAccess registries) {
        dispatcher.register(
            literal("scaldinghot")
                .then(literal("add_path")
                    .then(argument("modid", StringArgumentType.string())
                        .suggests(AddPathCommand::suggestMods)
                        .executes(ctx -> {
                            String modid = StringArgumentType.getString(ctx, "modid");

                            if (!FabricLoader.getInstance().isModLoaded(modid)) throw INVALID_MOD.create();

                            DialogUtil.selectFolderDialogAsync("Add resource directory for mod " + modid, Path.of(".").toAbsolutePath().toString())
                                .thenAccept(path -> {
                                    if (path == null) return;

                                    ScaldingHot.CONFIG.get().modResourcePaths.computeIfAbsent(modid, unused -> new ArrayList<>()).add(path);
                                    ScaldingHot.CONFIG.save();

                                    ctx.getSource().sendFeedback(Text.literal("Added path `" + path + "` to `" + modid + "`'s resources"));
                                })
                                .exceptionally(e -> {
                                    ScaldingHot.LOGGER.error("Failed to open select folder dialog", e);
                                    return null;
                                });

                            return 0;
                        }))));
    }

    private static CompletableFuture<Suggestions> suggestMods(CommandContext<FabricClientCommandSource> ctx, SuggestionsBuilder builder) {
        return CommandSource.suggestMatching(FabricLoader.getInstance().getAllMods().stream().map(x -> x.getMetadata().getId()), builder);
    }
}
