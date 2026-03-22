package net.tysontheember.emberstextapi.platform;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Platform abstraction for configuration.
 */
public interface ConfigHelper {
    /**
     * Get the singleton instance.
     */
    static ConfigHelper getInstance() {
        return ConfigHelperImpl.INSTANCE;
    }

    /**
     * Register the mod configuration.
     * Called during mod initialization.
     */
    void register();

    /**
     * Check if the welcome message is enabled.
     */
    boolean isWelcomeMessageEnabled();

    /**
     * Check if immersive messages are enabled globally.
     */
    default boolean isImmersiveMessagesEnabled() {
        return true;
    }

    /**
     * Get the list of globally disabled effect names.
     */
    default List<String> getDisabledEffects() {
        return Collections.emptyList();
    }

    /**
     * Check if a specific effect is disabled (by explicit disable list or reduceMotion).
     */
    default boolean isEffectDisabled(String effectName) {
        if (getDisabledEffects().stream().anyMatch(e -> e.equalsIgnoreCase(effectName))) {
            return true;
        }
        return isReduceMotionEnabled() && isMotionEffect(effectName);
    }

    /**
     * Check if the given effect name is a positional/motion effect.
     */
    static boolean isMotionEffect(String effectName) {
        Set<String> motionEffects = Set.of(
            "wave", "bounce", "swing", "shake", "circle",
            "wiggle", "pend", "pendulum", "turb", "turbulence", "scroll"
        );
        return motionEffects.contains(effectName.toLowerCase());
    }

    /**
     * Get the markup permission mode: "NONE", "WHITELIST", or "BLACKLIST".
     * NONE = no restrictions, WHITELIST = only listed players can use markup,
     * BLACKLIST = listed players cannot use markup.
     */
    default String getMarkupPermissionMode() {
        return "NONE";
    }

    /**
     * Get the list of player UUIDs for the markup whitelist/blacklist.
     */
    default List<String> getMarkupPlayerList() {
        return Collections.emptyList();
    }

    /**
     * Check if a player is allowed to use markup in chat.
     */
    default boolean isPlayerAllowedMarkup(UUID playerUuid) {
        String mode = getMarkupPermissionMode();
        if ("NONE".equalsIgnoreCase(mode)) {
            return true;
        }
        String uuidStr = playerUuid.toString();
        boolean inList = getMarkupPlayerList().stream()
            .anyMatch(e -> e.equalsIgnoreCase(uuidStr));
        if ("WHITELIST".equalsIgnoreCase(mode)) {
            return inList;
        }
        if ("BLACKLIST".equalsIgnoreCase(mode)) {
            return !inList;
        }
        return true;
    }

    /**
     * Get the maximum immersive message duration in ticks. 0 = unlimited.
     */
    default int getMaxMessageDuration() {
        return 0;
    }

    /**
     * Get the maximum number of simultaneous active messages per player. 0 = unlimited.
     */
    default int getMaxActiveMessages() {
        return 0;
    }

    /**
     * Get the maximum number of characters allowed when renaming an item in an anvil.
     * Vanilla default is 50.
     */
    default int getAnvilNameMaxLength() {
        return 50;
    }

    // === Server-side message limits (COMMON) ===

    /**
     * Server-enforced max message duration in ticks. 0 = unlimited.
     * Prevents permanent screen-filling messages.
     */
    default int getMaxServerMessageDuration() {
        return 1200;
    }

    /**
     * Server-enforced max simultaneous messages per player. 0 = unlimited.
     */
    default int getMaxServerActiveMessages() {
        return 10;
    }

    /**
     * Max pending steps per queue channel. 0 = unlimited.
     */
    default int getMaxQueueSize() {
        return 50;
    }

    /**
     * If non-empty, only listed effects are allowed in server-sent messages.
     * Empty = all allowed.
     */
    default List<String> getAllowedEffects() {
        return Collections.emptyList();
    }

    // === Markup (COMMON) ===

    /**
     * Tags stripped from player-authored chat markup.
     * Useful for banning specific effects from chat without disabling them from API use.
     */
    default List<String> getDisallowedMarkupTags() {
        return Collections.emptyList();
    }

    // === Accessibility (CLIENT) ===

    /**
     * Disables all positional/motion effects (wave, bounce, swing, shake, etc.).
     * Color effects still work.
     */
    default boolean isReduceMotionEnabled() {
        return false;
    }

    /**
     * Caps neon/glow quality. 1=fast(6 samples), 2=balanced(12), 3=quality(20).
     */
    default int getMaxNeonQuality() {
        return 3;
    }

    // === Performance (CLIENT) ===

    /**
     * Max entries in TextLayoutCache LRU. Higher = more memory, fewer recomputations.
     */
    default int getTextLayoutCacheSize() {
        return 256;
    }

    /**
     * Master toggle for SDF font rendering. When false, SDF fonts fall back to vanilla bitmap.
     */
    default boolean isSdfEnabled() {
        return true;
    }
}
