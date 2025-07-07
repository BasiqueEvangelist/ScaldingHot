package me.basiqueevangelist.scaldinghot.impl.client;

import com.mojang.brigadier.CommandDispatcher;
import me.basiqueevangelist.scaldinghot.impl.ScaldingHot;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.commands.CommandBuildContext;

import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal;

public class ReloadConfigCommand {
    public static void register(CommandDispatcher<FabricClientCommandSource> dispatcher, CommandBuildContext registries) {
        dispatcher.register(
            literal("scaldinghot")
                .then(literal("reload_config")
                    .executes(ctx -> {
                        ScaldingHot.CONFIG.load();

                        return 0;
                    })));
    }
}
