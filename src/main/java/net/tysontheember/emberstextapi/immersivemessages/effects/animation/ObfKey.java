package net.tysontheember.emberstextapi.immersivemessages.effects.animation;

/**
 * Composite key for obfuscate tracks: base object per render plus span index.
 * Using a non-mixin package avoids mixin classloading issues.
 */
public record ObfKey(Object base, int spanIndex) {}
