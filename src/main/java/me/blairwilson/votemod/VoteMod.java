package me.blairwilson.votemod;

import me.blairwilson.votemod.commands.BaseCommand;
import me.blairwilson.votemod.config.ConfigHandler;
import me.blairwilson.votemod.data.ConfigVote;
import me.blairwilson.votemod.data.Vote;
import net.minecraft.network.chat.ChatType;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.IExtensionPoint;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.NetworkConstants;
import net.minecraftforge.server.ServerLifecycleHooks;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Mod("votemod")
public class VoteMod {
    public static List<Vote> voteList = new ArrayList<>();
    public static List<ConfigVote> configVoteList = new ArrayList<>();
    public static ConfigHandler CONFIG;
    private int voteCounter = 0;

    public VoteMod() {
        MinecraftForge.EVENT_BUS.register(this);
        ModLoadingContext.get().registerExtensionPoint(IExtensionPoint.DisplayTest.class, () -> new IExtensionPoint.DisplayTest(() -> NetworkConstants.IGNORESERVERONLY, (a, b) -> true));
    }

    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {
        CONFIG = ConfigHandler.sync(ConfigHandler.getSaveFile());
        CONFIG.handleConfigVotes();
        BaseCommand.register(event.getServer().getCommands().getDispatcher());
    }

    @SubscribeEvent
    public void onServerTick(TickEvent.ServerTickEvent event) {
        if (!voteList.isEmpty()) {

            Vote vote = voteList.get(0);
            voteCounter++;

            if (voteCounter == 1200) {
                int yes = 0;
                int no = 0;

                for (Boolean pVote : vote.votes.values()) {
                    if (pVote)
                        yes++;
                    else
                        no++;
                }

                if (yes >= no) {
                    ServerLifecycleHooks.getCurrentServer().getPlayerList().broadcastSystemMessage(Component.literal("The vote started by " + vote.startedBy + " has been successful.").withStyle(Style.EMPTY.withColor(9633635)), false);
                    ServerLifecycleHooks.getCurrentServer().getPlayerList().getPlayers().forEach(serverPlayer -> serverPlayer.playNotifySound(SoundEvents.NOTE_BLOCK_PLING.get(), SoundSource.MASTER, 0.5f, 1f));
                    vote.runnable.run();
                } else {
                    ServerLifecycleHooks.getCurrentServer().getPlayerList().broadcastSystemMessage(Component.literal("The vote started by " + vote.startedBy + " has failed.").withStyle(Style.EMPTY.withColor(15218733)), false);
                }

                voteCounter = 0;
                voteList.clear();
            }
        }
    }

}
