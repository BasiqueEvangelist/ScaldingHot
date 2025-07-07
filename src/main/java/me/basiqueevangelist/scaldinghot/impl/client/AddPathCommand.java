package me.basiqueevangelist.scaldinghot.impl.client;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import me.basiqueevangelist.scaldinghot.impl.ScaldingHot;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.network.chat.Component;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.concurrent.CompletableFuture;

import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.argument;
import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal;

public class AddPathCommand {
    private static final SimpleCommandExceptionType INVALID_MOD = new SimpleCommandExceptionType(Component.literal("No such mod"));
    private static final SimpleCommandExceptionType INVALID_PATH = new SimpleCommandExceptionType(Component.literal("Invalid path"));

    public static void register(CommandDispatcher<FabricClientCommandSource> dispatcher, CommandBuildContext registries) {
        dispatcher.register(
            literal("scaldinghot")
                .then(literal("add_path")
                    .then(argument("modid", StringArgumentType.string())
                        .suggests(AddPathCommand::suggestMods)
                        .executes(AddPathCommand::addPathDialog)
                        .then(argument("path", StringArgumentType.greedyString())
                            .executes(AddPathCommand::addPathPath)))));
    }

    private static int addPathPath(CommandContext<FabricClientCommandSource> ctx) throws CommandSyntaxException {
        String modid = StringArgumentType.getString(ctx, "modid");

        if (!FabricLoader.getInstance().isModLoaded(modid)) throw INVALID_MOD.create();

        String path = StringArgumentType.getString(ctx, "path");

        if (!Files.exists(Path.of(path))) throw INVALID_PATH.create();

        addPath(ctx.getSource(), modid, path);

        return 0;
    }

    private static int addPathDialog(CommandContext<FabricClientCommandSource> ctx) throws CommandSyntaxException {
        String modid = StringArgumentType.getString(ctx, "modid");

        if (!FabricLoader.getInstance().isModLoaded(modid)) throw INVALID_MOD.create();

        DialogUtil.selectFolderDialogAsync("Add resource directory for mod " + modid, Path.of(".").toAbsolutePath().toString())
            .thenAccept(path -> addPath(ctx.getSource(), modid, path))
            .exceptionally(e -> {
                ScaldingHot.LOGGER.error("Failed to open select folder dialog", e);
                return null;
            });

        return 0;
    }

    private static void addPath(FabricClientCommandSource src, String modid, String path) {
        if (path == null) return;
        if (!Files.exists(Path.of(path))) return;

        ScaldingHot.CONFIG.get().modResourcePaths.computeIfAbsent(modid, unused -> new ArrayList<>()).add(path);
        ScaldingHot.CONFIG.save();

        src.sendFeedback(Component.literal("Added path `" + path + "` to `" + modid + "`'s resources"));
    }

    private static CompletableFuture<Suggestions> suggestMods(CommandContext<FabricClientCommandSource> ctx, SuggestionsBuilder builder) {
        return SharedSuggestionProvider.suggest(FabricLoader.getInstance().getAllMods().stream().map(x -> x.getMetadata().getId()), builder);
    }
}
