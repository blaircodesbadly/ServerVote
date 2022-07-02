package me.blairwilson.votemod.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import me.blairwilson.votemod.VoteMod;
import me.blairwilson.votemod.data.ConfigVote;
import me.blairwilson.votemod.data.Vote;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.*;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraftforge.server.ServerLifecycleHooks;

import java.util.UUID;

public class BaseCommand {

    public static void register(CommandDispatcher<CommandSourceStack> cmdDisp) {
        LiteralArgumentBuilder<CommandSourceStack> parentCommand = Commands.literal(VoteMod.CONFIG.getCommandAlias());

        LiteralArgumentBuilder<CommandSourceStack> weatherCommand = Commands.literal("weather"); /* /alias weather */

        parentCommand.executes(context -> {
            //does nothing
            return 1;
        });

        weatherCommand.executes(context -> {
            if (VoteMod.voteList.isEmpty()) {
                ServerPlayer p = context.getSource().getPlayerOrException();
                BaseCommand.handleVote(p, "change weather to clear");
                VoteMod.voteList.add(new Vote(p.getName().getString(), () -> ServerLifecycleHooks.getCurrentServer().overworld().setWeatherParameters(72000, 0, false, false)));
            }
            return 1;
        });

        parentCommand.then(weatherCommand);

        parentCommand.then(Commands.literal("yes").executes(context -> {
            if (!VoteMod.voteList.isEmpty()) {
                ServerPlayer p = context.getSource().getPlayerOrException();
                if(!VoteMod.voteList.get(0).votes.containsKey(p.getUUID())){
                    p.sendSystemMessage(Component.literal("You have voted YES").withStyle(Style.EMPTY.withColor(9633635)));
                    VoteMod.voteList.get(0).votes.putIfAbsent(p.getUUID(), true);
                } else
                    p.sendSystemMessage(Component.literal("You have already voted!").withStyle(Style.EMPTY.withColor(15218733)));
            }
            return 1;
        }));

        parentCommand.then(Commands.literal("no").executes(context -> {
            if (!VoteMod.voteList.isEmpty()) {
                ServerPlayer p = context.getSource().getPlayerOrException();
                if(!VoteMod.voteList.get(0).votes.containsKey(p.getUUID())){
                    p.sendSystemMessage(Component.literal("You have voted NO").withStyle(Style.EMPTY.withColor(15218733)));
                    VoteMod.voteList.get(0).votes.putIfAbsent(p.getUUID(), false);
                } else
                    p.sendSystemMessage(Component.literal("You have already voted!").withStyle(Style.EMPTY.withColor(15218733)));
            }
            return 1;
        }));

        for (ConfigVote cfgCmd : VoteMod.configVoteList) { /* handle config votes */
            LiteralArgumentBuilder<CommandSourceStack> command = Commands.literal(cfgCmd.getAlias());
            command.executes(context -> {
                if (VoteMod.voteList.isEmpty()) {
                    BaseCommand.handleVote(context.getSource().getPlayerOrException(), cfgCmd.getDesc());
                    Vote vote = new Vote(context.getSource().getPlayerOrException().getName().getString(), () -> {
                        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
                        server.getCommands().performCommand(server.createCommandSourceStack(), cfgCmd.getCommand());
                    });
                    VoteMod.voteList.add(vote);
                }
                return 1;
            });
            parentCommand.then(command); /* add as child of parentCommand eg. /vote ALIAS */
        }

        cmdDisp.register(parentCommand);


    }

    public static void handleVote(ServerPlayer p, String desc) { /* code to run on each vote */
        ServerLifecycleHooks.getCurrentServer().getPlayerList().getPlayers().forEach(serverPlayer -> serverPlayer.playNotifySound(SoundEvents.NOTE_BLOCK_PLING, SoundSource.MASTER, 1f, 1f));
        MutableComponent initial = Component.literal(p.getName().getString() + " has initiated a vote to " + desc + ".");
        initial.withStyle(Style.EMPTY.withColor(TextColor.fromLegacyFormat(ChatFormatting.GOLD)));
        ServerLifecycleHooks.getCurrentServer().getPlayerList().broadcastSystemMessage(initial, ChatType.SYSTEM);

        MutableComponent yes = Component.literal("[YES] ");
        MutableComponent no = Component.literal(" [NO]");

        yes.setStyle(yes.getStyle().withColor(9633635).withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/"+VoteMod.CONFIG.getCommandAlias()+" yes")).withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Component.literal("Click me to vote YES").withStyle(Style.EMPTY.withColor(9633635)))));
        no.setStyle(no.getStyle().withColor(15218733).withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/"+VoteMod.CONFIG.getCommandAlias()+" no")).withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Component.literal("Click me to vote NO").withStyle(Style.EMPTY.withColor(15218733)))));
        yes.append(no);
        ServerLifecycleHooks.getCurrentServer().getPlayerList().broadcastSystemMessage(yes, ChatType.SYSTEM);
    }

}
