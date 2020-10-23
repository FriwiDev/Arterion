package me.friwi.arterion.plugin.permissions;

public enum Rank {
    NORMAL,
    PREMIUM,
    LEGENDARY,
    BUILDER,
    DESIGNER,
    SUPPORTER,
    MODERATOR,
    DEVELOPER,
    ADMIN;
    private String rankTranslation;

    private Rank() {
        this.rankTranslation = "permissions.rank." + this.name().toLowerCase();
    }

    public String getRankTranslation() {
        return rankTranslation;
    }

    public boolean isHigherOrEqualThan(Rank other) {
        return other.ordinal() <= ordinal();
    }

    public boolean isLowerOrEqualThan(Rank other) {
        return other.ordinal() >= ordinal();
    }

    public boolean canAssignRanks() {
        return isHigherOrEqualThan(ADMIN);
    }

    public boolean canManageGame() {
        return isHigherOrEqualThan(DEVELOPER);
    }

    public boolean canMute() {
        return isHigherOrEqualThan(SUPPORTER);
    }

    public boolean canKick() {
        return isHigherOrEqualThan(MODERATOR);
    }

    public boolean canBan() {
        return isHigherOrEqualThan(MODERATOR);
    }

    public boolean isTeam() {
        return isHigherOrEqualThan(SUPPORTER);
    }

    public boolean isHigherTeam() {
        return isHigherOrEqualThan(MODERATOR);
    }

    public boolean isPremium() {
        return isHigherOrEqualThan(PREMIUM);
    }
}
