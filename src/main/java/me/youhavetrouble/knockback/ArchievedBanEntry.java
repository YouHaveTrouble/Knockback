package me.youhavetrouble.knockback;

import javax.annotation.Nullable;
import java.sql.Timestamp;
import java.util.UUID;

public class ArchievedBanEntry {
    private final Action action;
    private final UUID targetId, sourceId;
    private final String extraData;
    private final Timestamp timestamp;

    public ArchievedBanEntry(
            Action action,
            UUID targetId,
            @Nullable UUID sourceId,
            @Nullable String extraData,
            @Nullable Timestamp timestamp
    ) {
        this.action = action;
        this.targetId = targetId;
        this.sourceId = sourceId;
        this.extraData = extraData;
        this.timestamp = timestamp;
    }

    public Action getAction() {
        return action;
    }

    public UUID getTargetId() {
        return targetId;
    }

    public UUID getSourceId() {
        return sourceId;
    }

    public String getExtraData() {
        return extraData;
    }

    public Timestamp getTimestamp() {
        return timestamp;
    }

    enum Action {
        BAN, UNBAN, KICK
    }

}
