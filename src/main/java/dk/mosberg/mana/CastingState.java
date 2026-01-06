package dk.mosberg.mana;

/**
 * Represents the current casting state of a player.
 * Used to track spell casting phases and transitions.
 */
public enum CastingState {
    /**
     * Player is not casting anything.
     */
    IDLE,
    
    /**
     * Player is preparing/channeling a spell (charge-up phase).
     */
    CHANNELING,
    
    /**
     * Player is actively casting a spell (execution phase).
     */
    CASTING,
    
    /**
     * Player is in cooldown after casting a spell.
     */
    COOLDOWN;
    
    /**
     * Check if the current state allows starting a new cast.
     */
    public boolean canStartCast() {
        return this == IDLE;
    }
    
    /**
     * Check if the current state can be interrupted.
     */
    public boolean isInterruptible() {
        return this == CHANNELING || this == CASTING;
    }
    
    /**
     * Check if the player is currently in an active casting phase.
     */
    public boolean isActivelyCasting() {
        return this == CHANNELING || this == CASTING;
    }
}
