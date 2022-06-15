package me.blairwilson.votemod.data;

import com.google.gson.annotations.Expose;

public class ConfigVote {

    @Expose
    private String command;
    @Expose
    private String alias;
    @Expose
    private String desc;

    public String getCommand() {
        return command;
    }

    public void setCommand(String command) {
        this.command = command;
    }

    public String getAlias() {
        return alias;
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }
}
