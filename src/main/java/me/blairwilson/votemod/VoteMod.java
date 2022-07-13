package me.blairwilson.votemod;

import me.blairwilson.votemod.commands.BaseCommand;
import me.blairwilson.votemod.config.ConfigHandler;
import me.blairwilson.votemod.data.ConfigVote;
import me.blairwilson.votemod.data.Vote;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.text.ChatType;
import net.minecraft.util.text.Color;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.Style;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ExtensionPoint;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.server.FMLServerStartingEvent;
import net.minecraftforge.fml.network.FMLNetworkConstants;
import net.minecraftforge.fml.server.ServerLifecycleHooks;
import org.apache.commons.lang3.tuple.Pair;

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
        ModLoadingContext.get().registerExtensionPoint(ExtensionPoint.DISPLAYTEST, () -> Pair.of(() -> FMLNetworkConstants.IGNORESERVERONLY, (a, b) -> true));
    }

    @SubscribeEvent
    public void onServerStarting(FMLServerStartingEvent event) {
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
                    ServerLifecycleHooks.getCurrentServer().getPlayerList().broadcastMessage(new StringTextComponent("The vote started by " + vote.startedBy + " has been successful.").withStyle(Style.EMPTY.withColor(Color.fromRgb(9633635))), ChatType.CHAT, UUID.randomUUID());
                    ServerLifecycleHooks.getCurrentServer().getPlayerList().getPlayers().forEach(serverPlayer -> serverPlayer.playNotifySound(SoundEvents.NOTE_BLOCK_PLING, SoundCategory.MASTER,0.5f, 1f));
                    vote.runnable.run();
                } else {
                    ServerLifecycleHooks.getCurrentServer().getPlayerList().broadcastMessage(new StringTextComponent("The vote started by " + vote.startedBy + " has failed.").withStyle(Style.EMPTY.withColor(Color.fromRgb(15218733))), ChatType.CHAT, UUID.randomUUID());
                }

                voteCounter = 0;
                voteList.clear();
            }
        }
    }

}
