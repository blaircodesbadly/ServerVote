package me.blairwilson.votemod.data;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class Vote {

    public String startedBy;
    public Map<UUID, Boolean> votes;

    public Runnable runnable;

    public Vote(String startedBy, Runnable runnable) {
        this.startedBy = startedBy;
        this.runnable = runnable;
        this.votes = new HashMap<>();
    }
}
