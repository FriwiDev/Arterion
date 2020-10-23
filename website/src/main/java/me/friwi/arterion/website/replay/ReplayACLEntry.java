package me.friwi.arterion.website.replay;

import java.util.UUID;

public class ReplayACLEntry {
    private ReplayACLEntryType type;
    private UUID object;
    private boolean allowed;

    public ReplayACLEntry(ReplayACLEntryType type, UUID object, boolean allowed) {
        this.type = type;
        this.object = object;
        this.allowed = allowed;
    }

    public ReplayACLEntryType getType() {
        return type;
    }

    public UUID getObject() {
        return object;
    }

    public boolean isAllowed() {
        return allowed;
    }
}
