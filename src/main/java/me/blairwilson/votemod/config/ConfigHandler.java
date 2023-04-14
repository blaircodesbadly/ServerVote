package me.blairwilson.votemod.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.Expose;
import me.blairwilson.votemod.VoteMod;
import me.blairwilson.votemod.data.ConfigVote;
import net.minecraftforge.server.ServerLifecycleHooks;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class ConfigHandler {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().enableComplexMapKeySerialization().excludeFieldsWithoutExposeAnnotation().create();
    private File saveFile;
    @Expose
    private String commandAlias;
    @Expose
    private List<ConfigVote> configVotes = new ArrayList<>();

    public ConfigHandler(File saveFile) {
        this.saveFile = saveFile;
        this.commandAlias="vote";
    }

    public static File getSaveFile() {
        return new File(ServerLifecycleHooks.getCurrentServer().getServerDirectory(), "config/VoteMod.json");
    }

    public static ConfigHandler sync(File saveFile) {
        ConfigHandler cfg;
        if(saveFile.exists()){
            try (Reader r = new FileReader(saveFile)) {
                cfg = GSON.fromJson(r, ConfigHandler.class);
            } catch (Exception e) {
                cfg = new ConfigHandler(getSaveFile());
            }
        } else {
            cfg = new ConfigHandler(getSaveFile());

            List<ConfigVote> test = new ArrayList<>();
            ConfigVote testVote = new ConfigVote();
            testVote.setCommand("say hello from console");
            testVote.setAlias("test");
            testVote.setDesc("Test say command");
            test.add(testVote);

            cfg.setConfigVotes(test);
        }

        cfg.saveFile = getSaveFile();

        if(cfg.getCommandAlias()==null || cfg.getCommandAlias().isEmpty())
            cfg.setCommandAlias("vote");

        cfg.save();

        return cfg;
    }

    public void save() {
        try (Writer w = new FileWriter(saveFile)) {
            GSON.toJson(this, w);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void handleConfigVotes() {
        VoteMod.configVoteList.addAll(configVotes);
    }

    public List<ConfigVote> getConfigVotes() {
        return configVotes;
    }

    public void setConfigVotes(List<ConfigVote> configVotes) {
        this.configVotes = configVotes;
    }

    public String getCommandAlias() {
        return commandAlias;
    }

    public void setCommandAlias(String commandAlias) {
        this.commandAlias = commandAlias;
    }
}
