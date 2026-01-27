package net.tysontheember.emberstextapi.immersivemessages.api;

/**
 * Determines how the obfuscated ("§k") style is removed from text
 * over time to create a deobfuscation animation.
 */
public enum ObfuscateMode {
    NONE,
    LEFT,
    RIGHT,
    CENTER,
    EDGES,
    RANDOM;
}
