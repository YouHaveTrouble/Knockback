package me.youhavetrouble.knockback;

import javax.annotation.Nullable;
import java.sql.Timestamp;
import java.util.UUID;

public class BanRecord {

    private final UUID uuid;
    private final Timestamp timestamp;
    private final String reason;

    public BanRecord(UUID uuid, @Nullable Timestamp endsAt, @Nullable String reason) {
        this.uuid = uuid;
        this.timestamp = endsAt;
        this.reason = reason;
    }

    public UUID getUuid() {
        return uuid;
    }

    public Timestamp getTimestamp() {
        return timestamp;
    }

    public String getReason() {
        return reason;
    }

    /**
     * @return True if ban is permanent
     */
    public boolean isPerm() {
        return timestamp == null;
    }

}
