package me.youhavetrouble.knockback;

import java.util.UUID;

public record BanRecord(UUID uuid, Long endsAt, String reason) {

    /**
     * @return True if ban is permanent
     */
    public boolean isPerm() {
        return endsAt == null;
    }

}
