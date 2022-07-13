package me.blairwilson.votemod.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import me.blairwilson.votemod.VoteMod;
import me.blairwilson.votemod.data.ConfigVote;
import me.blairwilson.votemod.data.Vote;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.text.*;
import net.minecraft.util.text.event.ClickEvent;
import net.minecraft.util.text.event.HoverEvent;
import net.minecraftforge.fml.server.ServerLifecycleHooks;

import java.util.UUID;

public class BaseCommand {

    public static void register(CommandDispatcher<CommandSource> cmdDisp) {
        LiteralArgumentBuilder<CommandSource> parentCommand = Commands.literal(VoteMod.CONFIG.getCommandAlias());

        LiteralArgumentBuilder<CommandSource> weatherCommand = Commands.literal("weather"); /* /alias weather */

        parentCommand.executes(context -> {
            //does nothing
            return 1;
        });

        weatherCommand.executes(context -> {
            if (VoteMod.voteList.isEmpty()) {
                ServerPlayerEntity p = context.getSource().getPlayerOrException();
                BaseCommand.handleVote(p, "change weather to clear");
                VoteMod.voteList.add(new Vote(p.getName().getString(), () -> ServerLifecycleHooks.getCurrentServer().overworld().setWeatherParameters(72000, 0, false, false)));
            }
            return 1;
        });

        parentCommand.then(weatherCommand);

        parentCommand.then(Commands.literal("yes").executes(context -> {
            if (!VoteMod.voteList.isEmpty()) {
                ServerPlayerEntity p = context.getSource().getPlayerOrException();
                if(!VoteMod.voteList.get(0).votes.containsKey(p.getUUID())){
                    p.sendMessage(new StringTextComponent("You have voted YES").withStyle(Style.EMPTY.withColor(Color.fromRgb(9633635))), UUID.randomUUID());
                    VoteMod.voteList.get(0).votes.putIfAbsent(p.getUUID(), true);
                } else
                    p.sendMessage(new StringTextComponent("You have already voted!").withStyle(Style.EMPTY.withColor(Color.fromRgb(15218733))), UUID.randomUUID());
            }
            return 1;
        }));

        parentCommand.then(Commands.literal("no").executes(context -> {
            if (!VoteMod.voteList.isEmpty()) {
                ServerPlayerEntity p = context.getSource().getPlayerOrException();
                if(!VoteMod.voteList.get(0).votes.containsKey(p.getUUID())){
                    p.sendMessage(new StringTextComponent("You have voted NO").withStyle(Style.EMPTY.withColor(Color.fromRgb(15218733))), UUID.randomUUID());
                    VoteMod.voteList.get(0).votes.putIfAbsent(p.getUUID(), false);
                } else
                    p.sendMessage(new StringTextComponent("You have already voted!").withStyle(Style.EMPTY.withColor(Color.fromRgb(15218733))), UUID.randomUUID());
            }
            return 1;
        }));

        for (ConfigVote cfgCmd : VoteMod.configVoteList) { /* handle config votes */
            LiteralArgumentBuilder<CommandSource> command = Commands.literal(cfgCmd.getAlias());
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

    public static void handleVote(ServerPlayerEntity p, String desc) { /* code to run on each vote */
        ServerLifecycleHooks.getCurrentServer().getPlayerList().getPlayers().forEach(ServerPlayerEntity -> ServerPlayerEntity.playNotifySound(SoundEvents.NOTE_BLOCK_PLING, SoundCategory.MASTER, 1f, 1f));
        StringTextComponent initial = new StringTextComponent(p.getName().getString() + " has initiated a vote to " + desc + ".");
        initial.withStyle(Style.EMPTY.withColor(TextFormatting.GOLD));
        ServerLifecycleHooks.getCurrentServer().getPlayerList().broadcastMessage(initial, ChatType.CHAT, UUID.randomUUID());

        StringTextComponent yes = new StringTextComponent("[YES] ");
        StringTextComponent no = new StringTextComponent(" [NO]");

        yes.setStyle(yes.getStyle().withColor(Color.fromRgb(9633635)).withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/"+VoteMod.CONFIG.getCommandAlias()+" yes")).withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new StringTextComponent("Click me to vote YES").withStyle(Style.EMPTY.withColor(Color.fromRgb(9633635))))));
        no.setStyle(no.getStyle().withColor(Color.fromRgb(15218733)).withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/"+VoteMod.CONFIG.getCommandAlias()+" no")).withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new StringTextComponent("Click me to vote NO").withStyle(Style.EMPTY.withColor(Color.fromRgb(15218733))))));
        yes.append(no);
        ServerLifecycleHooks.getCurrentServer().getPlayerList().broadcastMessage(yes, ChatType.CHAT, UUID.randomUUID());
    }

}
