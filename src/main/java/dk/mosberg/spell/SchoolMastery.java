package dk.mosberg.spell;

/**
 * School Mastery/Prestige System: rewards dedicated players for reaching high proficiency in a
 * school.
 *
 * Features: - Mastery tracks total spells cast from each school - 4 mastery levels: Novice (0-100),
 * Expert (100-500), Master (500-2000), Grandmaster (2000+) - Each level unlocks permanent bonuses
 * to spells of that school - Mastery XP decays if player neglects a school (encourages rotation)
 *
 * Bonuses by level: - Novice: +0% (baseline) - Expert: +5% damage, +5% range - Master: +10% damage,
 * +10% range, -5% mana cost - Grandmaster: +15% damage, +15% range, -10% mana cost, +1 crit chance
 */
public class SchoolMastery {

    public enum MasteryLevel {
        NOVICE(0, 100, 0.0f, 0.0f, 0.0f), // threshold, max XP, damage bonus, range bonus, mana
                                          // discount
        EXPERT(100, 500, 0.05f, 0.05f, 0.0f), MASTER(500, 2000, 0.10f, 0.10f,
                0.05f), GRANDMASTER(2000, Integer.MAX_VALUE, 0.15f, 0.15f, 0.10f);

        private final int minXp;
        private final int maxXp;
        private final float damageBonus;
        private final float rangeBonus;
        private final float manaDiscount;

        MasteryLevel(int minXp, int maxXp, float damageBonus, float rangeBonus,
                float manaDiscount) {
            this.minXp = minXp;
            this.maxXp = maxXp;
            this.damageBonus = damageBonus;
            this.rangeBonus = rangeBonus;
            this.manaDiscount = manaDiscount;
        }

        public int getMinXp() {
            return minXp;
        }

        public int getMaxXp() {
            return maxXp;
        }

        public float getDamageBonus() {
            return damageBonus;
        }

        public float getRangeBonus() {
            return rangeBonus;
        }

        public float getManaDiscount() {
            return manaDiscount;
        }
    }

    /**
     * Calculate the mastery level from XP amount.
     *
     * @param xp Total mastery XP earned
     * @return The mastery level achieved
     */
    public static MasteryLevel getLevelFromXp(int xp) {
        if (xp >= MasteryLevel.GRANDMASTER.getMinXp()) {
            return MasteryLevel.GRANDMASTER;
        } else if (xp >= MasteryLevel.MASTER.getMinXp()) {
            return MasteryLevel.MASTER;
        } else if (xp >= MasteryLevel.EXPERT.getMinXp()) {
            return MasteryLevel.EXPERT;
        }
        return MasteryLevel.NOVICE;
    }

    /**
     * Get the percentage of the way to the next mastery level.
     *
     * @param xp Current mastery XP
     * @return Percentage (0-100) towards next level
     */
    public static float getProgressToNextLevel(int xp) {
        MasteryLevel current = getLevelFromXp(xp);
        if (current == MasteryLevel.GRANDMASTER) {
            return 100.0f; // Max level
        }

        // Find next level
        MasteryLevel next = switch (current) {
            case NOVICE -> MasteryLevel.EXPERT;
            case EXPERT -> MasteryLevel.MASTER;
            case MASTER -> MasteryLevel.GRANDMASTER;
            case GRANDMASTER -> MasteryLevel.GRANDMASTER; // No next level
        };

        int xpInCurrentLevel = xp - current.getMinXp();
        int xpNeededForNextLevel = next.getMinXp() - current.getMinXp();

        if (xpNeededForNextLevel <= 0) {
            return 100.0f;
        }

        return (xpInCurrentLevel / (float) xpNeededForNextLevel) * 100.0f;
    }

    /**
     * Grant mastery XP when a player casts a spell of a particular school.
     *
     * Amount scales with spell tier and difficulty: - Tier 1: 1 XP - Tier 2: 3 XP - Tier 3: 7 XP -
     * Tier 4: 15 XP
     *
     * @param spellTier The tier of the spell cast (1-4)
     * @return XP to grant
     */
    public static int getXpForSpellCast(int spellTier) {
        return switch (Math.max(1, Math.min(4, spellTier))) {
            case 1 -> 1;
            case 2 -> 3;
            case 3 -> 7;
            case 4 -> 15;
            default -> 1;
        };
    }

    /**
     * Apply mastery bonuses to spell values.
     *
     * @param baseValue Base damage, range, or mana cost
     * @param masteryLevel The player's mastery level in the school
     * @param bonusType Type of bonus: "damage", "range", "mana"
     * @return Modified value with mastery bonuses applied
     */
    public static float applyMasteryBonus(float baseValue, MasteryLevel masteryLevel,
            String bonusType) {
        return switch (bonusType.toLowerCase()) {
            case "damage" -> baseValue * (1.0f + masteryLevel.getDamageBonus());
            case "range" -> baseValue * (1.0f + masteryLevel.getRangeBonus());
            case "mana" -> baseValue * (1.0f - masteryLevel.getManaDiscount()); // Discount reduces
                                                                                // cost
            default -> baseValue;
        };
    }

    /**
     * Apply XP decay for a school not used recently (encourages school rotation).
     *
     * Decay is 5% per day without casting in that school.
     *
     * @param currentXp Current mastery XP
     * @param daysSinceLastCast Days since last spell from this school was cast
     * @return XP after decay
     */
    public static int applyXpDecay(int currentXp, int daysSinceLastCast) {
        if (daysSinceLastCast <= 0) {
            return currentXp; // No decay if recently used
        }

        float decayPerDay = 0.05f; // 5% per day
        float decayFactor = (float) Math.pow(1.0f - decayPerDay, Math.min(daysSinceLastCast, 30));

        return Math.round(currentXp * decayFactor);
    }

    /**
     * Get XP threshold to reach the next mastery level from current XP.
     *
     * @param currentXp Current mastery XP
     * @return XP needed to reach next level (0 if at max)
     */
    public static int getXpToNextLevel(int currentXp) {
        MasteryLevel current = getLevelFromXp(currentXp);
        if (current == MasteryLevel.GRANDMASTER) {
            return 0;
        }

        MasteryLevel next = switch (current) {
            case NOVICE -> MasteryLevel.EXPERT;
            case EXPERT -> MasteryLevel.MASTER;
            case MASTER -> MasteryLevel.GRANDMASTER;
            case GRANDMASTER -> MasteryLevel.GRANDMASTER;
        };

        return next.getMinXp() - currentXp;
    }
}
