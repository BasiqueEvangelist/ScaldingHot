package me.basiqueevangelist.scaldinghot.impl;

import me.basiqueevangelist.scaldinghot.api.HotReloadBatch;
import me.basiqueevangelist.scaldinghot.api.HotReloadPlugin;
import me.basiqueevangelist.scaldinghot.mixin.MinecraftServerAccessor;
import me.basiqueevangelist.scaldinghot.mixin.PlayerListAccessor;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.network.protocol.common.ClientboundUpdateTagsPacket;
import net.minecraft.network.protocol.game.ClientboundUpdateRecipesPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.PlayerAdvancements;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;
import net.minecraft.tags.TagNetworkSerialization;

public class ServerReloadPlugin implements HotReloadPlugin {
    @Override
    public void onHotReload(HotReloadBatch batch) {
        batch.queueFinishTask(() -> {
            MinecraftServer server = ScaldingHot.SERVER;

            boolean tagsChanged = wasFolderChanged("tags/", batch);

            if (tagsChanged) {
                ((MinecraftServerAccessor) server).getResources().managers().updateRegistryTags();
            }

            PlayerList playerList = server.getPlayerList();

            playerList.saveAll();

            if (wasFolderChanged("advancement/", batch)) {
                for (PlayerAdvancements playerAdvancements : ((PlayerListAccessor) playerList).getAdvancements().values()) {
                    playerAdvancements.reload(server.getAdvancements());
                }
            }

            for (ServerPlayer player : playerList.getPlayers()) {
                ServerLifecycleEvents.SYNC_DATA_PACK_CONTENTS.invoker().onSyncDataPackContents(player, false);
            }

            boolean recipesChanged = wasFolderChanged("recipe/", batch);

            // EMI requires both tags and recipes to be synced to reload, so we send both if one is reloaded.
            if (tagsChanged || recipesChanged) {
                playerList.broadcastAll(new ClientboundUpdateTagsPacket(TagNetworkSerialization.serializeTagsToNetwork(server.registries())));

                ClientboundUpdateRecipesPacket clientboundUpdateRecipesPacket = new ClientboundUpdateRecipesPacket(server.getRecipeManager().getOrderedRecipes());

                for (ServerPlayer serverPlayer : playerList.getPlayers()) {
                    serverPlayer.connection.send(clientboundUpdateRecipesPacket);
                    serverPlayer.getRecipeBook().sendInitialRecipeBook(serverPlayer);
                }
            }

            if (wasFolderChanged("tags/function/", batch) || wasFolderChanged("function/", batch)) {
                server.getFunctions().replaceLibrary(((MinecraftServerAccessor) server).getResources().managers().getFunctionLibrary());
            }
        });
    }

    private boolean wasFolderChanged(String folder, HotReloadBatch batch) {
        for (var id : batch.changedResources()) {
            if (id.getPath().startsWith(folder))
                return true;
        }

        return false;
    }
}
