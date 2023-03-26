package de.greensurvivors.findme.dataObjects;

import org.jetbrains.annotations.NotNull;

/**
 * record for easy broadcasting the players and their scores of an objective,
 * binds a player name to their score and makes a List of this record sortable by implementing compareTo
 * as an Integer.compareTo for the score, but reversed it, so it's naturally ordered highest to lowest
 * @param name name of the player
 * @param score their score
 */
public record PlayerScore(String name, Integer score) implements Comparable<PlayerScore> {
    /**
     * Compares this object with the specified object for order. Returns a
     * negative integer, zero, or a positive integer as this object is greater
     * than, equal to, or less than the specified object.
     *
     * @param other the PlayerScore to be compared.
     * @return a negative integer, zero, or a positive integer as the PlayerScore.score
     * is greater than, equal to, or less than the specified PlayerScore.score.
     * @throws NullPointerException if the specified object is null
     * @throws ClassCastException   if the specified object's type prevents it
     *                              from being compared to this object.
     */
    @Override
    public int compareTo(@NotNull PlayerScore other) {
        return -this.score.compareTo(other.score);
    }
}
