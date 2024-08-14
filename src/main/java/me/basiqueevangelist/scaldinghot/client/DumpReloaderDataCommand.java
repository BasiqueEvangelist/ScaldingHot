package me.basiqueevangelist.scaldinghot.client;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import me.basiqueevangelist.scaldinghot.instrument.ReloaderData;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.text.Text;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;

import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal;

public class DumpReloaderDataCommand {
    public static void register(CommandDispatcher<FabricClientCommandSource> dispatcher, CommandRegistryAccess registries) {
        dispatcher.register(
            literal("scaldinghot")
                .then(literal("dump_reloader_data")
                    .executes(DumpReloaderDataCommand::dump))
        );
    }

    private static int dump(CommandContext<FabricClientCommandSource> ctx) {
        StringBuilder sb = new StringBuilder();

        for (var data : ReloaderData.RELOADER_TO_DATA.values()) {
            sb.append(data.reloaderName).append(" (").append(data.type.getDirectory()).append(")").append(":\n");

            for (var id : data.accessedResources) {
                sb.append("  ").append(id).append("\n");
            }
        }

        try {
            Files.writeString(Path.of("reloader-data-dump.txt"), sb.toString(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return 0;
    }
}
