package dev.wotq.keepinventory.bridge;

import net.minecraft.entity.player.PlayerEntity;

import java.util.Optional;

/**
 * Bridge interface for PlayerEntity that exposes a getter and setter for the
 * per-player keepInventory value.
 */
public interface PlayerEntityBridge {
    /**
     * Cast the PlayerEntity to a PlayerEntityBridge.
     *
     * As long as the Mixin has been mixed in, this should never fail. It's here for convenience and can be used as
     * bridge(player).$wotq_*.
     *
     * @param player the player
     * @return the player as a PlayerEntityBridge
     */
    static PlayerEntityBridge bridge(PlayerEntity player) {
        return (PlayerEntityBridge) player;
    }

    Optional<Boolean> $wotq_getKeepInventory();
    void $wotq_setKeepInventory(Optional<Boolean> value);
}
