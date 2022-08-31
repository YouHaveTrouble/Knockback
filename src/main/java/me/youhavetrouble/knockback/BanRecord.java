package me.youhavetrouble.knockback;

import java.sql.Timestamp;
import java.util.UUID;

public record BanRecord(UUID uuid, Timestamp endsAt, String reason) {

    /**
     * @return True if ban is permanent
     */
    public boolean isPerm() {
        return endsAt == null;
    }

}
