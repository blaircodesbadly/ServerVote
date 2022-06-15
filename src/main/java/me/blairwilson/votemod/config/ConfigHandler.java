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
    private List<ConfigVote> configVotes = new ArrayList<>();

    public ConfigHandler(File saveFile) {
        this.saveFile = saveFile;
    }

    public static File getSaveFile() {
        return new File(ServerLifecycleHooks.getCurrentServer().getServerDirectory(), "config/VoteMod.json");
    }

    public static ConfigHandler sync(File saveFile) {
        ConfigHandler cfg;
        try (Reader r = new FileReader(saveFile)) {
            cfg = GSON.fromJson(r, ConfigHandler.class);
        } catch (Exception e) {
            cfg = new ConfigHandler(getSaveFile());
        }
        cfg.saveFile = getSaveFile();

        List<ConfigVote> test = new ArrayList<>();
        ConfigVote testVote = new ConfigVote();
        testVote.setCommand("/say hello from console");
        testVote.setAlias("test");
        testVote.setDesc("say test from console");
        test.add(testVote);

        cfg.setConfigVotes(test);
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
}
